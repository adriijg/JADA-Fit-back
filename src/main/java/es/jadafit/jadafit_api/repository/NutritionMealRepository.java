package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.NutritionMealLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NutritionMealRepository extends JpaRepository<NutritionMealLog, UUID> {

    List<NutritionMealLog> findByUserIdAndLoggedAtBetweenOrderByLoggedAtAsc(
            UUID userId,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<NutritionMealLog> findByIdAndUserId(UUID id, UUID userId);
}