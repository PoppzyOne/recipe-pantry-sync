package com.recipesync.resource;

import com.recipesync.dto.CreateShoppingListItemDto;
import com.recipesync.dto.ShoppingListItemResponseDto;
import com.recipesync.dto.UpdateShoppingListItemDto;
import com.recipesync.service.ShoppingListService;
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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/shopping-list")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Shopping List", description = "Operations related to the shopping list and offline-friendly sync")
public class ShoppingListResource {

    private final ShoppingListService shoppingListService;

    @Inject
    public ShoppingListResource(ShoppingListService shoppingListService) {
        this.shoppingListService = shoppingListService;
    }

    @GET
    @Operation(summary = "List shopping list items", description = "Retrieves all items in the shopping list ordered with unchecked first")
    @APIResponse(
            responseCode = "200",
            description = "List of shopping list items",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = ShoppingListItemResponseDto.class))
    )
    public List<ShoppingListItemResponseDto> list() {
        return shoppingListService.listAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get shopping list item by ID", description = "Retrieves a single shopping list item")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Shopping list item found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ShoppingListItemResponseDto.class))),
            @APIResponse(responseCode = "404", description = "Item not found")
    })
    public ShoppingListItemResponseDto getById(@PathParam("id") Long id) {
        return shoppingListService.getById(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Add item to shopping list", description = "Creates a new item or increments amount if unchecked identical item exists")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Item added",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ShoppingListItemResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid input")
    })
    public Response create(@Valid CreateShoppingListItemDto dto) {
        ShoppingListItemResponseDto created = shoppingListService.create(dto);
        return Response.created(URI.create("/api/shopping-list/" + created.id()))
                .entity(created)
                .build();
    }

    @POST
    @Path("/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Batch add items to shopping list", description = "Adds multiple items to the shopping list efficiently")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Items added",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = SchemaType.ARRAY, implementation = ShoppingListItemResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid input")
    })
    public Response createBatch(List<@Valid CreateShoppingListItemDto> dtos) {
        List<ShoppingListItemResponseDto> results = shoppingListService.createBatch(dtos);
        return Response.status(Response.Status.CREATED)
                .entity(results)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update shopping list item", description = "Updates details or checked state of a shopping list item")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Item updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ShoppingListItemResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid input"),
            @APIResponse(responseCode = "404", description = "Item not found")
    })
    public ShoppingListItemResponseDto update(@PathParam("id") Long id, @Valid UpdateShoppingListItemDto dto) {
        return shoppingListService.update(id, dto);
    }

    @PATCH
    @Path("/{id}/toggle")
    @Operation(summary = "Toggle checked status", description = "Toggles the checked state of a shopping list item")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Item toggled",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ShoppingListItemResponseDto.class))),
            @APIResponse(responseCode = "404", description = "Item not found")
    })
    public ShoppingListItemResponseDto toggleChecked(@PathParam("id") Long id) {
        return shoppingListService.toggleChecked(id);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete shopping list item", description = "Deletes a specific item from the shopping list")
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Item deleted"),
            @APIResponse(responseCode = "404", description = "Item not found")
    })
    public Response delete(@PathParam("id") Long id) {
        shoppingListService.delete(id);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/completed")
    @Operation(summary = "Clear completed items", description = "Removes all checked items from the shopping list")
    @APIResponse(responseCode = "200", description = "Completed items cleared")
    public Response clearCompleted() {
        long count = shoppingListService.clearCompleted();
        return Response.ok(Map.of("deletedCount", count)).build();
    }

    @POST
    @Path("/sync-to-pantry")
    @Operation(summary = "Sync purchased items to pantry", description = "Transfers checked items to the pantry inventory and clears them from the shopping list")
    @APIResponse(responseCode = "200", description = "Checked items synced to pantry")
    public Response syncToPantry() {
        int synced = shoppingListService.syncCheckedToPantry();
        return Response.ok(Map.of("syncedCount", synced)).build();
    }
}
