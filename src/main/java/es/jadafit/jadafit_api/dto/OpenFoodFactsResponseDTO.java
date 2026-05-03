package es.jadafit.jadafit_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenFoodFactsResponseDTO(
        Integer status,
        OpenFoodFactsProductDTO product
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OpenFoodFactsProductDTO(
            String code,

            @JsonProperty("product_name")
            String productName,

            @JsonProperty("product_name_es")
            String productNameEs,

            @JsonProperty("product_name_en")
            String productNameEn,

            OpenFoodFactsNutrimentsDTO nutriments
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OpenFoodFactsNutrimentsDTO(
            @JsonProperty("energy-kcal_100g")
            BigDecimal energyKcal100g,

            @JsonProperty("energy-kcal_value")
            BigDecimal energyKcalValue,

            @JsonProperty("proteins_100g")
            BigDecimal proteins100g,

            @JsonProperty("carbohydrates_100g")
            BigDecimal carbohydrates100g,

            @JsonProperty("fat_100g")
            BigDecimal fat100g
    ) {
    }
}