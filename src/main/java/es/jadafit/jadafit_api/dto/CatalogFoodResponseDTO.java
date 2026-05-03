package es.jadafit.jadafit_api.dto;

import es.jadafit.jadafit_api.model.FoodSource;

import java.math.BigDecimal;
import java.util.UUID;

public record CatalogFoodResponseDTO(
        UUID id,
        UUID ownerUserId,
        String externalFoodId,
        String barcode,
        String name,
        String brand,
        FoodSource source,
        BigDecimal caloriesPer100g,
        BigDecimal proteinPer100g,
        BigDecimal carbsPer100g,
        BigDecimal fatsPer100g
) {
}