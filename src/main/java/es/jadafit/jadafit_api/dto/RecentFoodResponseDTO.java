package es.jadafit.jadafit_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecentFoodResponseDTO(
        UUID id,
        String foodName,
        BigDecimal caloriesPer100g,
        BigDecimal proteinPer100g,
        BigDecimal carbsPer100g,
        BigDecimal fatsPer100g,
        LocalDateTime lastLoggedAt,
        String foodSource
) {
}
