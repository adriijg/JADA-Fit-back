package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecipeRepository extends JpaRepository<Recipe, UUID> {

    List<Recipe> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Recipe> findByIdAndUserId(UUID id, UUID userId);
}
