package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.model.UserExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserExerciseRecordRepository extends JpaRepository<UserExerciseRecord, UUID> {
    Optional<UserExerciseRecord> findByUserAndExerciseName(User user, String exerciseName);
    List<UserExerciseRecord> findByUser(User user);
}
