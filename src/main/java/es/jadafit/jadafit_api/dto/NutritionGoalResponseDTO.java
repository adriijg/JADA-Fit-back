package es.jadafit.jadafit_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record NutritionGoalResponseDTO(
        UUID id,
        BigDecimal caloriesTarget,
        BigDecimal proteinTarget,
        BigDecimal carbsTarget,
        BigDecimal fatsTarget,
        LocalDateTime updatedAt
) {
}