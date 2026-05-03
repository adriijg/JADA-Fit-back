package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.FitnessProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FitnessProfileRepository extends JpaRepository<FitnessProfile, UUID> {

    Optional<FitnessProfile> findByUserId(UUID userId);
}