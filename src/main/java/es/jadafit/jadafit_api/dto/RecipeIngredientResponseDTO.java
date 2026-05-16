package es.jadafit.jadafit_api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RecipeIngredientResponseDTO(
        UUID id,
        String foodName,
        BigDecimal quantityGrams,
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbs,
        BigDecimal fats
) {
}
