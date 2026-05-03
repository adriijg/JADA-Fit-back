package es.jadafit.jadafit_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record NutritionGoalUpdateDTO(
        @NotNull(message = "El objetivo de calorías es obligatorio")
        @DecimalMin(value = "1.0", message = "Las calorías deben ser mayores que 0")
        BigDecimal caloriesTarget,

        @NotNull(message = "El objetivo de proteína es obligatorio")
        @DecimalMin(value = "0.0", message = "La proteína no puede ser negativa")
        BigDecimal proteinTarget,

        @NotNull(message = "El objetivo de hidratos es obligatorio")
        @DecimalMin(value = "0.0", message = "Los hidratos no pueden ser negativos")
        BigDecimal carbsTarget,

        @NotNull(message = "El objetivo de grasas es obligatorio")
        @DecimalMin(value = "0.0", message = "Las grasas no pueden ser negativas")
        BigDecimal fatsTarget
) {
}