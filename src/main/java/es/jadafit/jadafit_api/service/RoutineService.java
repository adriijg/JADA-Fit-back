package es.jadafit.jadafit_api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.jadafit.jadafit_api.exception.NotFoundException;
import es.jadafit.jadafit_api.exception.UnauthorizedException;
import es.jadafit.jadafit_api.model.Exercise;
import es.jadafit.jadafit_api.model.Routine;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.repository.RoutineRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoutineService {
    private final RoutineRepository routineRepository;

    public List<Routine> getRoutinesByUser(UUID userId) {
        return routineRepository.findByUserId(userId);
    }

    public Routine getRoutineById(Long id) {
        return routineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rutina no encontrada"));
    }

    public Routine saveRoutine(Routine routine, UUID userId) {
        User userRef = new User();
        userRef.setId(userId);
        routine.setUser(userRef);
        if (routine.getExercises() != null) {
            for (Exercise exercise : routine.getExercises()) {
                exercise.setRoutine(routine);
            }
        }
        return routineRepository.save(routine);
    }

    public void deleteRoutine(Long id, UUID userId) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rutina no encontrada"));

        if (!routine.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para eliminar esta rutina");
        }

        routineRepository.deleteById(id);
    }

    @Transactional
    public Routine completeRoutine(Long id, UUID userId) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rutina no encontrada"));

        if (!routine.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para modificar esta rutina");
        }

        routine.setIsCompleted(true);
        routine.setCompletedAt(LocalDateTime.now());

        if (routine.getExercises() != null) {
            for (Exercise exercise : routine.getExercises()) {
                exercise.setIsCompleted(true);
            }
        }

        return routineRepository.save(routine);
    }

    @Transactional
    public Routine completeExercise(Long routineId, Long exerciseId, boolean completed, UUID userId) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new NotFoundException("Rutina no encontrada"));

        if (routine.getUser() == null || !routine.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para modificar esta rutina");
        }

        if (routine.getExercises() != null) {
            for (Exercise exercise : routine.getExercises()) {
                if (exercise.getId().equals(exerciseId)) {
                    exercise.setIsCompleted(completed);
                    break;
                }
            }
        }

        boolean allCompleted = routine.getExercises() != null
                && routine.getExercises().stream().allMatch(Exercise::getIsCompleted);

        if (allCompleted) {
            routine.setIsCompleted(true);
            routine.setCompletedAt(LocalDateTime.now());
        } else {
            routine.setIsCompleted(false);
            routine.setCompletedAt(null);
        }

        return routineRepository.save(routine);
    }
}
