package es.jadafit.jadafit_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.jadafit.jadafit_api.model.Routine;

@Repository
public interface RoutineRepository extends JpaRepository<Routine, Long> {
}
