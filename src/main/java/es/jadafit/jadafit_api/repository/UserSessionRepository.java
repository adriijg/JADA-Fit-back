package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    Optional<UserSession> findBySessionIdAndIsActiveTrue(String sessionId);
    List<UserSession> findByUserIdAndIsActiveTrue(UUID userId);
    long countByUserIdAndIsActiveTrue(UUID userId);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.id = :id")
    void deactivateById(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.sessionId = :sessionId AND s.isActive = true")
    void deactivateBySessionId(@Param("sessionId") String sessionId);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.user.id = :userId AND s.isActive = true")
    void deactivateAllByUserId(@Param("userId") UUID userId);

    @Query("SELECT s.id FROM UserSession s WHERE s.user.id = :userId AND s.isActive = true ORDER BY s.createdAt ASC")
    List<UUID> findActiveSessionIdsByUserIdOrderByCreatedAtAsc(@Param("userId") UUID userId);
}
