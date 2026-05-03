package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.NutritionDaySummaryResponseDTO;
import es.jadafit.jadafit_api.dto.NutritionGoalResponseDTO;
import es.jadafit.jadafit_api.dto.NutritionMealCreateDTO;
import es.jadafit.jadafit_api.dto.NutritionMealResponseDTO;
import es.jadafit.jadafit_api.exception.NotFoundException;
import es.jadafit.jadafit_api.model.NutritionMealLog;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.repository.NutritionMealRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NutritionService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final UserService userService;
    private final NutritionMealRepository nutritionMealRepository;
    private final NutritionGoalService nutritionGoalService;

    public NutritionService(
            UserService userService,
            NutritionMealRepository nutritionMealRepository,
            NutritionGoalService nutritionGoalService
    ) {
        this.userService = userService;
        this.nutritionMealRepository = nutritionMealRepository;
        this.nutritionGoalService = nutritionGoalService;
    }

    @Transactional
    public NutritionMealResponseDTO createMeal(UUID userId, NutritionMealCreateDTO dto) {
        User user = userService.getUserById(userId);

        LocalDateTime loggedAt = dto.loggedAt() != null
                ? dto.loggedAt()
                : LocalDateTime.now();

        NutritionMealLog mealLog = NutritionMealLog.builder()
                .user(user)
                .externalFoodId(normalizeText(dto.externalFoodId()))
                .foodName(normalizeText(dto.foodName()))
                .mealType(dto.mealType())
                .quantityGrams(dto.quantityGrams())
                .calories(calculateForQuantity(dto.caloriesPer100g(), dto.quantityGrams()))
                .protein(calculateForQuantity(dto.proteinPer100g(), dto.quantityGrams()))
                .carbs(calculateForQuantity(dto.carbsPer100g(), dto.quantityGrams()))
                .fats(calculateForQuantity(dto.fatsPer100g(), dto.quantityGrams()))
                .loggedAt(loggedAt)
                .build();

        NutritionMealLog savedMeal = nutritionMealRepository.save(mealLog);

        return toResponse(savedMeal);
    }

    @Transactional(readOnly = true)
    public NutritionDaySummaryResponseDTO getDaySummary(UUID userId, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();

        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.plusDays(1).atStartOfDay();

        List<NutritionMealResponseDTO> meals = nutritionMealRepository
                .findByUserIdAndLoggedAtBetweenOrderByLoggedAtAsc(userId, start, end)
                .stream()
                .map(this::toResponse)
                .toList();

        BigDecimal totalCalories = sum(meals.stream().map(NutritionMealResponseDTO::calories).toList());
        BigDecimal totalProtein = sum(meals.stream().map(NutritionMealResponseDTO::protein).toList());
        BigDecimal totalCarbs = sum(meals.stream().map(NutritionMealResponseDTO::carbs).toList());
        BigDecimal totalFats = sum(meals.stream().map(NutritionMealResponseDTO::fats).toList());

        NutritionGoalResponseDTO goal = nutritionGoalService.getMyNutritionGoal(userId);

        return new NutritionDaySummaryResponseDTO(
                targetDate,

                totalCalories,
                totalProtein,
                totalCarbs,
                totalFats,

                goal.caloriesTarget(),
                goal.proteinTarget(),
                goal.carbsTarget(),
                goal.fatsTarget(),

                meals
        );
    }

    @Transactional
    public void deleteMeal(UUID userId, UUID mealId) {
        NutritionMealLog mealLog = nutritionMealRepository.findByIdAndUserId(mealId, userId)
                .orElseThrow(() -> new NotFoundException("Registro de comida no encontrado"));

        nutritionMealRepository.delete(mealLog);
    }

    private BigDecimal calculateForQuantity(BigDecimal valuePer100g, BigDecimal quantityGrams) {
        return valuePer100g
                .multiply(quantityGrams)
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<BigDecimal> values) {
        return values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private NutritionMealResponseDTO toResponse(NutritionMealLog mealLog) {
        return new NutritionMealResponseDTO(
                mealLog.getId(),
                mealLog.getExternalFoodId(),
                mealLog.getFoodName(),
                mealLog.getMealType(),
                mealLog.getQuantityGrams(),
                mealLog.getCalories(),
                mealLog.getProtein(),
                mealLog.getCarbs(),
                mealLog.getFats(),
                mealLog.getLoggedAt()
        );
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }
}