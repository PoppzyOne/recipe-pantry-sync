package com.recipesync.resource;

import com.recipesync.dto.CreateRecipeDto;
import com.recipesync.dto.RecipeIngredientDto;
import com.recipesync.dto.RecipeResponseDto;
import com.recipesync.dto.UpdateRecipeDto;
import com.recipesync.service.PantryService;
import com.recipesync.service.RecipeService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
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

@Path("/api/recipes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Recipes", description = "Operations related to recipes")
public class RecipeResource {

    private final RecipeService recipeService;
    private final PantryService pantryService;

    @Inject
    public RecipeResource(RecipeService recipeService, PantryService pantryService) {
        this.recipeService = recipeService;
        this.pantryService = pantryService;
    }

    @GET
    @Operation(summary = "List all recipes", description = "Retrieves all saved recipes")
    @APIResponse(responseCode = "200", description = "List of all recipes",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = RecipeResponseDto.class)))
    public List<RecipeResponseDto> getAll() {
        return recipeService.getAllRecipes();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get recipe by ID", description = "Retrieves a single recipe by its unique identifier")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Recipe found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RecipeResponseDto.class))),
            @APIResponse(responseCode = "404", description = "Recipe not found")
    })
    public RecipeResponseDto getById(@Parameter(description = "ID of the recipe", required = true) @PathParam("id") Long id) {
        return recipeService.getRecipeById(id);
    }

    @GET
    @Path("/{id}/missing-ingredients")
    @Operation(summary = "Check missing ingredients", description = "Compares recipe ingredients against current pantry stock and returns what is missing")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "List of missing ingredients",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = SchemaType.ARRAY, implementation = RecipeIngredientDto.class))),
            @APIResponse(responseCode = "404", description = "Recipe not found")
    })
    public List<RecipeIngredientDto> getMissingIngredients(@Parameter(description = "ID of the recipe", required = true) @PathParam("id") Long id) {
        return pantryService.getMissingIngredientsForRecipe(id);
    }

    @POST
    @Operation(summary = "Create recipe", description = "Creates a new recipe and stores it in the database")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Recipe created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RecipeResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid recipe input / validation failure")
    })
    public Response create(@Valid CreateRecipeDto dto) {
        RecipeResponseDto created = recipeService.createRecipe(dto);
        return Response.created(URI.create("/api/recipes/" + created.id()))
                .entity(created)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update recipe", description = "Updates an existing recipe by its ID")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Recipe updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RecipeResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid input"),
            @APIResponse(responseCode = "404", description = "Recipe not found")
    })
    public RecipeResponseDto update(
            @Parameter(description = "ID of the recipe to update", required = true) @PathParam("id") Long id,
            @Valid UpdateRecipeDto dto) {
        return recipeService.updateRecipe(id, dto);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete recipe", description = "Deletes a recipe by its ID")
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Recipe deleted successfully"),
            @APIResponse(responseCode = "404", description = "Recipe not found")
    })
    public Response delete(@Parameter(description = "ID of the recipe to delete", required = true) @PathParam("id") Long id) {
        recipeService.deleteRecipe(id);
        return Response.noContent().build();
    }
}
