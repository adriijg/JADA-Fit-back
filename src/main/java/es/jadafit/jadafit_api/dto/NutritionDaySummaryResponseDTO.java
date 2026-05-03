package es.jadafit.jadafit_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record NutritionDaySummaryResponseDTO(
        LocalDate date,

        BigDecimal totalCalories,
        BigDecimal totalProtein,
        BigDecimal totalCarbs,
        BigDecimal totalFats,

        BigDecimal caloriesTarget,
        BigDecimal proteinTarget,
        BigDecimal carbsTarget,
        BigDecimal fatsTarget,

        List<NutritionMealResponseDTO> meals
) {
}