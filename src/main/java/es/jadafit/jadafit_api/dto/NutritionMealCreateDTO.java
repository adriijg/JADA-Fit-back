package es.jadafit.jadafit_api.dto;

import es.jadafit.jadafit_api.model.MealType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NutritionMealCreateDTO(
        String externalFoodId,

        @NotBlank(message = "El nombre del alimento es obligatorio")
        String foodName,

        @NotNull(message = "El tipo de comida es obligatorio")
        MealType mealType,

        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "1.0", message = "La cantidad debe ser mayor que 0")
        BigDecimal quantityGrams,

        @NotNull(message = "Las calorías por 100g son obligatorias")
        @DecimalMin(value = "0.0", message = "Las calorías no pueden ser negativas")
        BigDecimal caloriesPer100g,

        @NotNull(message = "La proteína por 100g es obligatoria")
        @DecimalMin(value = "0.0", message = "La proteína no puede ser negativa")
        BigDecimal proteinPer100g,

        @NotNull(message = "Los carbohidratos por 100g son obligatorios")
        @DecimalMin(value = "0.0", message = "Los carbohidratos no pueden ser negativos")
        BigDecimal carbsPer100g,

        @NotNull(message = "Las grasas por 100g son obligatorias")
        @DecimalMin(value = "0.0", message = "Las grasas no pueden ser negativas")
        BigDecimal fatsPer100g,

        @PastOrPresent(message = "La fecha del registro no puede estar en el futuro")
        LocalDateTime loggedAt
) {
}