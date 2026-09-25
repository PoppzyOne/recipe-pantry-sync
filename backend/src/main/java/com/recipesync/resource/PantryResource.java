package com.recipesync.resource;

import com.recipesync.dto.CreatePantryItemDto;
import com.recipesync.dto.PantryItemResponseDto;
import com.recipesync.dto.UpdatePantryItemDto;
import com.recipesync.service.PantryService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;

@Path("/api/pantry")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Pantry", description = "Operations related to the pantry and inventory sync")
public class PantryResource {

    private final PantryService pantryService;

    @Inject
    public PantryResource(PantryService pantryService) {
        this.pantryService = pantryService;
    }

    @GET
    @Operation(summary = "List pantry items", description = "Retrieves all pantry items, optionally filtered by inStock status")
    @APIResponse(responseCode = "200", description = "List of pantry items",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = PantryItemResponseDto.class)))
    public List<PantryItemResponseDto> list(@QueryParam("inStock") Boolean inStock) {
        if (Boolean.TRUE.equals(inStock)) {
            return pantryService.listInStock();
        }
        return pantryService.listAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get pantry item by ID", description = "Retrieves a single pantry item by its ID")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Pantry item found"),
            @APIResponse(responseCode = "404", description = "Pantry item not found")
    })
    public PantryItemResponseDto getById(@PathParam("id") Long id) {
        return pantryService.getById(id);
    }

    @POST
    @Operation(summary = "Add or update pantry item", description = "Adds an item to the pantry or updates it if already present")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Pantry item added/updated"),
            @APIResponse(responseCode = "400", description = "Invalid input")
    })
    public Response addOrUpdate(@Valid CreatePantryItemDto dto) {
        PantryItemResponseDto created = pantryService.addOrUpdateItem(dto);
        return Response.created(URI.create("/api/pantry/" + created.id()))
                .entity(created)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update pantry item", description = "Updates quantity, unit or stock status of a pantry item")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Pantry item updated"),
            @APIResponse(responseCode = "404", description = "Pantry item not found")
    })
    public PantryItemResponseDto update(
            @Parameter(description = "ID of the pantry item", required = true) @PathParam("id") Long id,
            @Valid UpdatePantryItemDto dto
    ) {
        return pantryService.updateItem(id, dto);
    }

    @PATCH
    @Path("/{id}/toggle")
    @Operation(summary = "Toggle in-stock status", description = "Toggles whether an item is currently in stock or out of stock")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Stock status toggled"),
            @APIResponse(responseCode = "404", description = "Pantry item not found")
    })
    public PantryItemResponseDto toggleStock(@PathParam("id") Long id) {
        return pantryService.toggleInStock(id);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete pantry item", description = "Removes an item from the pantry")
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Pantry item deleted"),
            @APIResponse(responseCode = "404", description = "Pantry item not found")
    })
    public Response delete(@PathParam("id") Long id) {
        pantryService.deleteItem(id);
        return Response.noContent().build();
    }
}
