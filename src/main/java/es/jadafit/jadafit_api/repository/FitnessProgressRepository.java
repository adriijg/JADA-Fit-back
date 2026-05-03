package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.FitnessProgressLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FitnessProgressRepository extends JpaRepository<FitnessProgressLog, UUID> {

    List<FitnessProgressLog> findByUserIdOrderByLoggedAtAsc(UUID userId);

    Optional<FitnessProgressLog> findFirstByUserIdOrderByLoggedAtDesc(UUID userId);

    List<FitnessProgressLog> findByUserIdAndLoggedAtBetweenOrderByLoggedAtAsc(
            UUID userId,
            LocalDateTime start,
            LocalDateTime end
    );
}