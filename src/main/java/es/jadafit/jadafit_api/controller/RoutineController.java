package es.jadafit.jadafit_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.jadafit.jadafit_api.model.Routine;
import es.jadafit.jadafit_api.service.RoutineService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/routines")
@RequiredArgsConstructor
public class RoutineController {
    private final RoutineService routineService;

    @GetMapping
    public ResponseEntity<List<Routine>> getAll() {
        return ResponseEntity.ok(routineService.getAllRoutines());
    }

    @PostMapping
    public ResponseEntity<Routine> create(@RequestBody Routine routine) {
        return ResponseEntity.ok(routineService.saveRoutine(routine));
    }
}
