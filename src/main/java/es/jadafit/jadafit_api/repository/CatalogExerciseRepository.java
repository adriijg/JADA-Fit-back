package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.CatalogExercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogExerciseRepository extends JpaRepository<CatalogExercise, Long> {
    List<CatalogExercise> findByNameContainingIgnoreCase(String name);
}
