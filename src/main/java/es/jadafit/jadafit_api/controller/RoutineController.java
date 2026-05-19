package es.jadafit.jadafit_api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.jadafit.jadafit_api.exception.UnauthorizedException;
import es.jadafit.jadafit_api.model.Routine;
import es.jadafit.jadafit_api.service.RoutineService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/routines")
@RequiredArgsConstructor
public class RoutineController {
    private final RoutineService routineService;

    @GetMapping
    public ResponseEntity<List<Routine>> getAll(Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(routineService.getRoutinesByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Routine> getById(@PathVariable Long id) {
        return ResponseEntity.ok(routineService.getRoutineById(id));
    }

    @PostMapping
    public ResponseEntity<Routine> create(@RequestBody Routine routine, Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(routineService.saveRoutine(routine, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);
        routineService.deleteRoutine(id, userId);
        return ResponseEntity.noContent().build();
    }

    private UUID getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("No autorizado");
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException("Token invalido");
        }
    }
}
