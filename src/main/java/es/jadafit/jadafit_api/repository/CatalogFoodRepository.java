package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.CatalogFood;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CatalogFoodRepository extends JpaRepository<CatalogFood, UUID> {

    Optional<CatalogFood> findByExternalId(String externalId);
}