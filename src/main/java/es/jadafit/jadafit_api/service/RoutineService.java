package es.jadafit.jadafit_api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import es.jadafit.jadafit_api.model.Routine;
import es.jadafit.jadafit_api.repository.RoutineRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoutineService {
    private final RoutineRepository routineRepository;

    public List<Routine> getAllRoutines() {
        return routineRepository.findAll();
    }

    public Routine getRoutineById(Long id) {
        return routineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada con id: " + id));
    }

    public Routine saveRoutine(Routine routine) {
        return routineRepository.save(routine);
    }

    public void deleteRoutine(Long id) {
        if (!routineRepository.existsById(id)) {
            throw new RuntimeException("Rutina no encontrada con id: " + id);
        }
        routineRepository.deleteById(id);
    }
}
