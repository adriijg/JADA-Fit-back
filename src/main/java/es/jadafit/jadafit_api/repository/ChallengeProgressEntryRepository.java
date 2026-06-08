package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.Challenge;
import es.jadafit.jadafit_api.model.ChallengeProgressEntry;
import es.jadafit.jadafit_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChallengeProgressEntryRepository extends JpaRepository<ChallengeProgressEntry, UUID> {

    List<ChallengeProgressEntry> findByChallengeOrderByEntryDateAscCreatedAtAsc(Challenge challenge);

    List<ChallengeProgressEntry> findByChallengeAndUserOrderByEntryDateAscCreatedAtAsc(Challenge challenge, User user);

    Optional<ChallengeProgressEntry> findByChallengeAndUserAndEntryDate(Challenge challenge, User user, LocalDate entryDate);
}
