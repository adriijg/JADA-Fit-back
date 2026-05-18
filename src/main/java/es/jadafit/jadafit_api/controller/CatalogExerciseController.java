package es.jadafit.jadafit_api.controller;

import es.jadafit.jadafit_api.model.CatalogExercise;
import es.jadafit.jadafit_api.service.CatalogExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/exercises")
@RequiredArgsConstructor
public class CatalogExerciseController {

    private final CatalogExerciseService catalogExerciseService;

    @GetMapping
    public ResponseEntity<List<CatalogExercise>> getAll() {
        return ResponseEntity.ok(catalogExerciseService.getAllExercises());
    }

    @GetMapping("/search")
    public ResponseEntity<List<CatalogExercise>> search(@RequestParam String q) {
        return ResponseEntity.ok(catalogExerciseService.searchExercises(q));
    }
}
