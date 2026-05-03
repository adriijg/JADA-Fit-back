package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.ProfileProgressLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileProgressRepository extends JpaRepository<ProfileProgressLog, UUID> {

    List<ProfileProgressLog> findByUserIdOrderByLoggedAtAsc(UUID userId);

    Optional<ProfileProgressLog> findFirstByUserIdOrderByLoggedAtDesc(UUID userId);
}