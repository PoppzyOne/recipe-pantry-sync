package com.recipesync.resource;

import com.recipesync.dto.CreateMealPlanItemDto;
import com.recipesync.dto.MealPlanItemResponseDto;
import com.recipesync.dto.MealPlanShoppingItemDto;
import com.recipesync.dto.UpdateMealPlanItemDto;
import com.recipesync.service.MealPlanService;
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
import java.time.LocalDate;
import java.util.List;

@Path("/api/meal-plans")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Meal Plans", description = "Operations related to weekly meal planning")
public class MealPlanResource {

    private final MealPlanService mealPlanService;

    @Inject
    public MealPlanResource(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }

    @GET
    @Operation(summary = "List meal plans", description = "Retrieves meal plans optionally filtered by date range")
    @APIResponse(responseCode = "200", description = "List of meal plan items",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = MealPlanItemResponseDto.class)))
    public List<MealPlanItemResponseDto> getMealPlans(
            @Parameter(description = "Start date (ISO format YYYY-MM-DD)") @QueryParam("startDate") LocalDate startDate,
            @Parameter(description = "End date (ISO format YYYY-MM-DD)") @QueryParam("endDate") LocalDate endDate
    ) {
        return mealPlanService.getMealPlans(startDate, endDate);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get meal plan item by ID", description = "Retrieves a single meal plan item")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Meal plan item found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MealPlanItemResponseDto.class))),
            @APIResponse(responseCode = "404", description = "Meal plan item not found")
    })
    public MealPlanItemResponseDto getById(
            @Parameter(description = "ID of the meal plan item", required = true) @PathParam("id") Long id
    ) {
        return mealPlanService.getById(id);
    }

    @POST
    @Operation(summary = "Add meal plan item", description = "Schedules a recipe or custom dish for a given date and meal type")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Meal plan item created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MealPlanItemResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid meal plan input")
    })
    public Response create(@Valid CreateMealPlanItemDto dto) {
        MealPlanItemResponseDto created = mealPlanService.create(dto);
        return Response.created(URI.create("/api/meal-plans/" + created.id()))
                .entity(created)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update meal plan item", description = "Updates date, recipe, servings or notes of a meal plan item")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Meal plan item updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MealPlanItemResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid input"),
            @APIResponse(responseCode = "404", description = "Meal plan item not found")
    })
    public MealPlanItemResponseDto update(
            @Parameter(description = "ID of the meal plan item to update", required = true) @PathParam("id") Long id,
            @Valid UpdateMealPlanItemDto dto
    ) {
        return mealPlanService.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete meal plan item", description = "Removes a meal plan item")
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Meal plan item deleted successfully"),
            @APIResponse(responseCode = "404", description = "Meal plan item not found")
    })
    public Response delete(
            @Parameter(description = "ID of the meal plan item to delete", required = true) @PathParam("id") Long id
    ) {
        mealPlanService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/shopping-list")
    @Operation(summary = "Generate shopping list for meal plans", description = "Aggregates needed ingredients for planned meals in a date range and compares against pantry stock")
    @APIResponse(responseCode = "200", description = "Aggregated shopping list items with stock comparison",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = MealPlanShoppingItemDto.class)))
    public List<MealPlanShoppingItemDto> calculateShoppingList(
            @Parameter(description = "Start date (ISO format YYYY-MM-DD)", required = true) @QueryParam("startDate") LocalDate startDate,
            @Parameter(description = "End date (ISO format YYYY-MM-DD)", required = true) @QueryParam("endDate") LocalDate endDate
    ) {
        return mealPlanService.calculateShoppingList(startDate, endDate);
    }
}
