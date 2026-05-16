package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.NutritionGoalResponseDTO;
import es.jadafit.jadafit_api.dto.NutritionGoalUpdateDTO;
import es.jadafit.jadafit_api.exception.NotFoundException;
import es.jadafit.jadafit_api.model.FitnessGoal;
import es.jadafit.jadafit_api.model.FitnessProfile;
import es.jadafit.jadafit_api.model.Gender;
import es.jadafit.jadafit_api.model.NutritionGoal;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.repository.FitnessProfileRepository;
import es.jadafit.jadafit_api.repository.NutritionGoalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NutritionGoalService {

    private static final BigDecimal ACTIVITY_MULTIPLIER = BigDecimal.valueOf(1.45);

    private final UserService userService;
    private final FitnessProfileRepository fitnessProfileRepository;
    private final NutritionGoalRepository nutritionGoalRepository;

    public NutritionGoalService(
            UserService userService,
            FitnessProfileRepository fitnessProfileRepository,
            NutritionGoalRepository nutritionGoalRepository
    ) {
        this.userService = userService;
        this.fitnessProfileRepository = fitnessProfileRepository;
        this.nutritionGoalRepository = nutritionGoalRepository;
    }

    @Transactional
    public NutritionGoalResponseDTO getMyNutritionGoal(UUID userId) {
        return nutritionGoalRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElseGet(() -> recalculateMyNutritionGoal(userId));
    }

    @Transactional
    public NutritionGoalResponseDTO recalculateMyNutritionGoal(UUID userId) {
        User user = userService.getUserById(userId);

        FitnessProfile profile = fitnessProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Completa tu perfil físico antes de calcular objetivos nutricionales"));

        validateFitnessProfile(profile);

        CalculatedNutritionGoal calculatedGoal = calculateGoal(profile);

        NutritionGoal nutritionGoal = nutritionGoalRepository.findByUserId(userId)
                .orElseGet(() -> NutritionGoal.builder()
                        .user(user)
                        .build());

        nutritionGoal.setCaloriesTarget(calculatedGoal.caloriesTarget());
        nutritionGoal.setProteinTarget(calculatedGoal.proteinTarget());
        nutritionGoal.setCarbsTarget(calculatedGoal.carbsTarget());
        nutritionGoal.setFatsTarget(calculatedGoal.fatsTarget());
        nutritionGoal.setUpdatedAt(LocalDateTime.now());

        NutritionGoal savedGoal = nutritionGoalRepository.save(nutritionGoal);

        return toResponse(savedGoal);
    }

    @Transactional
    public NutritionGoalResponseDTO updateMyNutritionGoalManually(
            UUID userId,
            NutritionGoalUpdateDTO dto
    ) {
        User user = userService.getUserById(userId);

        NutritionGoal nutritionGoal = nutritionGoalRepository.findByUserId(userId)
                .orElseGet(() -> NutritionGoal.builder()
                        .user(user)
                        .build());

        nutritionGoal.setCaloriesTarget(scale(dto.caloriesTarget(), 0));
        nutritionGoal.setProteinTarget(scale(dto.proteinTarget(), 1));
        nutritionGoal.setCarbsTarget(scale(dto.carbsTarget(), 1));
        nutritionGoal.setFatsTarget(scale(dto.fatsTarget(), 1));
        nutritionGoal.setUpdatedAt(LocalDateTime.now());

        NutritionGoal savedGoal = nutritionGoalRepository.save(nutritionGoal);

        return toResponse(savedGoal);
    }

    private void validateFitnessProfile(FitnessProfile profile) {
        if (profile.getWeight() == null) {
            throw new NotFoundException("El peso es obligatorio para calcular objetivos nutricionales");
        }

        if (profile.getHeight() == null) {
            throw new NotFoundException("La altura es obligatoria para calcular objetivos nutricionales");
        }

        if (profile.getAge() == null) {
            throw new NotFoundException("La edad es obligatoria para calcular objetivos nutricionales");
        }

        if (profile.getGender() == null) {
            throw new NotFoundException("El género es obligatorio para calcular objetivos nutricionales");
        }

        if (profile.getGoal() == null) {
            throw new NotFoundException("El objetivo físico es obligatorio para calcular objetivos nutricionales");
        }
    }

    private CalculatedNutritionGoal calculateGoal(FitnessProfile profile) {
        BigDecimal weight = profile.getWeight();
        BigDecimal height = BigDecimal.valueOf(profile.getHeight());
        BigDecimal age = BigDecimal.valueOf(profile.getAge());

        BigDecimal bmr = calculateBmr(
                weight,
                height,
                age,
                profile.getGender()
        );

        BigDecimal maintenanceCalories = bmr.multiply(ACTIVITY_MULTIPLIER);

        BigDecimal caloriesTarget;
        BigDecimal proteinPerKg;
        BigDecimal fatsPerKg;

        FitnessGoal goal = profile.getGoal();

        switch (goal) {
            case GANAR_MUSCULO -> {
                caloriesTarget = maintenanceCalories.multiply(BigDecimal.valueOf(1.10));
                proteinPerKg = BigDecimal.valueOf(2.0);
                fatsPerKg = BigDecimal.valueOf(0.9);
            }
            case PERDER_GRASA -> {
                caloriesTarget = maintenanceCalories.multiply(BigDecimal.valueOf(0.85));
                proteinPerKg = BigDecimal.valueOf(2.2);
                fatsPerKg = BigDecimal.valueOf(0.8);
            }
            case MANTENERSE_ATLETICO -> {
                caloriesTarget = maintenanceCalories;
                proteinPerKg = BigDecimal.valueOf(1.8);
                fatsPerKg = BigDecimal.valueOf(0.9);
            }
            case MEJORAR_RENDIMIENTO -> {
                caloriesTarget = maintenanceCalories.multiply(BigDecimal.valueOf(1.05));
                proteinPerKg = BigDecimal.valueOf(1.8);
                fatsPerKg = BigDecimal.valueOf(0.8);
            }
            case RECOMPOSICION_CORPORAL -> {
                caloriesTarget = maintenanceCalories;
                proteinPerKg = BigDecimal.valueOf(2.3);
                fatsPerKg = BigDecimal.valueOf(0.8);
            }
            default -> {
                caloriesTarget = maintenanceCalories;
                proteinPerKg = BigDecimal.valueOf(1.8);
                fatsPerKg = BigDecimal.valueOf(0.9);
            }
        }

        BigDecimal proteinTarget = weight.multiply(proteinPerKg);
        BigDecimal fatsTarget = weight.multiply(fatsPerKg);

        BigDecimal caloriesFromProtein = proteinTarget.multiply(BigDecimal.valueOf(4));
        BigDecimal caloriesFromFats = fatsTarget.multiply(BigDecimal.valueOf(9));

        BigDecimal remainingCaloriesForCarbs = caloriesTarget
                .subtract(caloriesFromProtein)
                .subtract(caloriesFromFats);

        BigDecimal carbsTarget = remainingCaloriesForCarbs
                .divide(BigDecimal.valueOf(4), 1, RoundingMode.HALF_UP);

        if (carbsTarget.compareTo(BigDecimal.ZERO) < 0) {
            carbsTarget = BigDecimal.ZERO;
        }

        return new CalculatedNutritionGoal(
                scale(caloriesTarget, 0),
                scale(proteinTarget, 1),
                scale(carbsTarget, 1),
                scale(fatsTarget, 1)
        );
    }

    private BigDecimal calculateBmr(
            BigDecimal weight,
            BigDecimal height,
            BigDecimal age,
            Gender gender
    ) {
        BigDecimal bmr = weight.multiply(BigDecimal.TEN)
                .add(height.multiply(BigDecimal.valueOf(6.25)))
                .subtract(age.multiply(BigDecimal.valueOf(5)));

        if (gender == Gender.HOMBRE) {
            return bmr.add(BigDecimal.valueOf(5));
        }

        if (gender == Gender.MUJER) {
            return bmr.subtract(BigDecimal.valueOf(161));
        }

        return bmr;
    }

    private BigDecimal scale(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP);
    }

    private NutritionGoalResponseDTO toResponse(NutritionGoal goal) {
        return new NutritionGoalResponseDTO(
                goal.getId(),
                goal.getCaloriesTarget(),
                goal.getProteinTarget(),
                goal.getCarbsTarget(),
                goal.getFatsTarget(),
                goal.getUpdatedAt()
        );
    }

    private record CalculatedNutritionGoal(
            BigDecimal caloriesTarget,
            BigDecimal proteinTarget,
            BigDecimal carbsTarget,
            BigDecimal fatsTarget
    ) {
    }
}