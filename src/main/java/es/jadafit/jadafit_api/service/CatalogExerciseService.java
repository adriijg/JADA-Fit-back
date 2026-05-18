package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.model.CatalogExercise;
import es.jadafit.jadafit_api.repository.CatalogExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogExerciseService {

    private final CatalogExerciseRepository catalogExerciseRepository;

    public List<CatalogExercise> getAllExercises() {
        return catalogExerciseRepository.findAll();
    }

    public List<CatalogExercise> searchExercises(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllExercises();
        }
        return catalogExerciseRepository.findByNameContainingIgnoreCase(name);
    }
}
