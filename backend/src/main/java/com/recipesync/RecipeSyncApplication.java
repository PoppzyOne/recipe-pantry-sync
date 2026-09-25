package com.recipesync;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

@OpenAPIDefinition(
    info = @Info(
        title = "Recipe & Pantry Sync API",
        version = "1.0.0",
        description = "REST API for managing recipes, meal plans, and synchronized grocery shopping lists."
    )
)
public class RecipeSyncApplication extends Application {
}
