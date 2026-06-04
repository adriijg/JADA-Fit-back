package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    Optional<UserSession> findBySessionIdAndIsActiveTrue(String sessionId);
    List<UserSession> findByUserIdAndIsActiveTrue(UUID userId);
    long countByUserIdAndIsActiveTrue(UUID userId);
}
