package es.jadafit.jadafit_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CustomFoodCreateDTO(
        @NotBlank(message = "El nombre del alimento es obligatorio")
        String name,

        String brand,

        String barcode,

        @NotNull(message = "Las calorías por 100g son obligatorias")
        @DecimalMin(value = "0.0", message = "Las calorías no pueden ser negativas")
        BigDecimal caloriesPer100g,

        @NotNull(message = "La proteína por 100g es obligatoria")
        @DecimalMin(value = "0.0", message = "La proteína no puede ser negativa")
        BigDecimal proteinPer100g,

        @NotNull(message = "Los hidratos por 100g son obligatorios")
        @DecimalMin(value = "0.0", message = "Los hidratos no pueden ser negativos")
        BigDecimal carbsPer100g,

        @NotNull(message = "Las grasas por 100g son obligatorias")
        @DecimalMin(value = "0.0", message = "Las grasas no pueden ser negativas")
        BigDecimal fatsPer100g
) {
}