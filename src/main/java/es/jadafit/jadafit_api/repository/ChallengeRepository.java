package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.Challenge;
import es.jadafit.jadafit_api.model.ChallengeStatus;
import es.jadafit.jadafit_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ChallengeRepository extends JpaRepository<Challenge, UUID> {
    
    @Query("SELECT c FROM Challenge c WHERE c.challenger = :user OR c.challenged = :user")
    List<Challenge> findAllByUser(@Param("user") User user);
    
    List<Challenge> findByChallengedAndStatus(User challenged, ChallengeStatus status);
    
    @Query("SELECT c FROM Challenge c WHERE (c.challenger = :user1 AND c.challenged = :user2) OR (c.challenger = :user2 AND c.challenged = :user1)")
    List<Challenge> findBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT c FROM Challenge c WHERE c.status IN :statuses AND c.expiresAt < :now")
    List<Challenge> findByStatusInAndExpiresAtBefore(@Param("statuses") List<ChallengeStatus> statuses, @Param("now") LocalDateTime now);
}
