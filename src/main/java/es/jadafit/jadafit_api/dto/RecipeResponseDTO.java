package es.jadafit.jadafit_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RecipeResponseDTO(
        UUID id,
        String name,
        Integer servings,
        BigDecimal totalCalories,
        BigDecimal totalProtein,
        BigDecimal totalCarbs,
        BigDecimal totalFats,
        List<RecipeIngredientResponseDTO> ingredients,
        LocalDateTime createdAt
) {
}
