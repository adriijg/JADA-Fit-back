package es.jadafit.jadafit_api.dto;

import es.jadafit.jadafit_api.model.MealType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record NutritionMealResponseDTO(
        UUID id,
        String externalFoodId,
        String foodName,
        MealType mealType,
        BigDecimal quantityGrams,
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbs,
        BigDecimal fats,
        LocalDateTime loggedAt
) {
}