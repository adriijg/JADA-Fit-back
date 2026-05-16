package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.WaterLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WaterLogRepository extends JpaRepository<WaterLog, UUID> {

    List<WaterLog> findByUserIdAndLoggedAtBetweenOrderByLoggedAtAsc(
            UUID userId, LocalDateTime start, LocalDateTime end);

    Optional<WaterLog> findByIdAndUserId(UUID id, UUID userId);
}
