package es.jadafit.jadafit_api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FoodResponseDTO(
        UUID id,
        String externalId,
        String name,
        BigDecimal caloriesPer100g,
        BigDecimal proteinPer100g,
        BigDecimal carbsPer100g,
        BigDecimal fatsPer100g,
        Boolean isCustom
) {
}