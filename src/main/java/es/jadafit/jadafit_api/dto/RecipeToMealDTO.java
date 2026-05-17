package es.jadafit.jadafit_api.dto;

import es.jadafit.jadafit_api.model.MealType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDateTime;
import java.util.UUID;

public record RecipeToMealDTO(
        @NotNull(message = "El ID de la receta es obligatorio")
        UUID recipeId,

        @NotNull(message = "El tipo de comida es obligatorio")
        MealType mealType,

        @PastOrPresent(message = "La fecha no puede estar en el futuro")
        LocalDateTime loggedAt
) {
}
