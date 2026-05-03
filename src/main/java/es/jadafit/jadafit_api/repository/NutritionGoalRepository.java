package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.NutritionGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NutritionGoalRepository extends JpaRepository<NutritionGoal, UUID> {

    Optional<NutritionGoal> findByUserId(UUID userId);
}