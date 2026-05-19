package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.NutritionMealLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
           SELECT m FROM NutritionMealLog m
           WHERE m.user.id = :userId
             AND NOT EXISTS (
               SELECT 1 FROM NutritionMealLog m2
               WHERE m2.user.id = m.user.id
                 AND m2.foodName = m.foodName
                 AND m2.loggedAt > m.loggedAt
             )
           ORDER BY m.loggedAt DESC""")
    List<NutritionMealLog> findRecentDistinctFoods(@Param("userId") UUID userId);
}