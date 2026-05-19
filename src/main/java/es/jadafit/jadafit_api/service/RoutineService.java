package es.jadafit.jadafit_api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import es.jadafit.jadafit_api.exception.NotFoundException;
import es.jadafit.jadafit_api.exception.UnauthorizedException;
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
}
