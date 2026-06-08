package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.ChallengeCreateDTO;
import es.jadafit.jadafit_api.dto.ChallengeProgressCreateDTO;
import es.jadafit.jadafit_api.dto.ChallengeProgressEntryDTO;
import es.jadafit.jadafit_api.dto.ChallengeResponseDTO;
import es.jadafit.jadafit_api.dto.UserExerciseRecordDTO;
import es.jadafit.jadafit_api.dto.UserSummaryDTO;
import es.jadafit.jadafit_api.exception.ConflictException;
import es.jadafit.jadafit_api.exception.NotFoundException;
import es.jadafit.jadafit_api.model.*;
import es.jadafit.jadafit_api.repository.ChallengeProgressEntryRepository;
import es.jadafit.jadafit_api.repository.ChallengeRepository;
import es.jadafit.jadafit_api.repository.UserExerciseRecordRepository;
import es.jadafit.jadafit_api.repository.UserFollowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeProgressEntryRepository progressEntryRepository;
    private final UserExerciseRecordRepository recordRepository;
    private final UserFollowRepository followRepository;
    private final UserService userService;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public ChallengeResponseDTO createChallenge(UUID challengerId, ChallengeCreateDTO dto) {
        User challenger = userService.getUserById(challengerId);
        User challenged = userService.getUserById(dto.challengedId());

        if (challengerId.equals(dto.challengedId())) {
            throw new ConflictException("No puedes desafiarte a ti mismo");
        }

        // Check mutual following
        boolean challengerFollows = followRepository.existsByFollowerAndFollowing(challenger, challenged);
        boolean challengedFollows = followRepository.existsByFollowerAndFollowing(challenged, challenger);

        if (!challengerFollows || !challengedFollows) {
            throw new ConflictException("Solo puedes desafiar a usuarios que te sigan y que tú sigas (amigos)");
        }

        // Get current weight for the exercise if it exists
        BigDecimal challengerWeight = recordRepository.findByUserAndExerciseName(challenger, dto.exerciseName())
                .map(UserExerciseRecord::getMaxWeight)
                .orElse(BigDecimal.ZERO);

        BigDecimal challengedWeight = recordRepository.findByUserAndExerciseName(challenged, dto.exerciseName())
                .map(UserExerciseRecord::getMaxWeight)
                .orElse(BigDecimal.ZERO);
        BigDecimal highestStartWeight = challengerWeight.max(challengedWeight);
        BigDecimal targetWeight = getValidCreateTargetWeight(dto.targetWeightKg(), highestStartWeight);

        Challenge challenge = Challenge.builder()
                .challenger(challenger)
                .challenged(challenged)
                .exerciseName(dto.exerciseName())
                .status(ChallengeStatus.PENDING)
                .challengerWeight(challengerWeight)
                .challengedWeight(challengedWeight)
                .challengerStartWeight(challengerWeight)
                .challengedStartWeight(challengedWeight)
                .targetIncreaseKg(BigDecimal.TEN)
                .targetWeightKg(targetWeight)
                .build();

        return toResponseDTO(challengeRepository.save(challenge));
    }

    @Transactional
    public ChallengeResponseDTO acceptChallenge(UUID userId, UUID challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new NotFoundException("Desafío no encontrado"));

        if (!challenge.getChallenged().getId().equals(userId)) {
            throw new ConflictException("No tienes permiso para aceptar este desafío");
        }

        if (challenge.getStatus() != ChallengeStatus.PENDING) {
            throw new ConflictException("El desafío ya no está pendiente");
        }

        challenge.setStatus(ChallengeStatus.ACCEPTED);
        return toResponseDTO(challengeRepository.save(challenge));
    }

    @Transactional
    public ChallengeResponseDTO rejectChallenge(UUID userId, UUID challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new NotFoundException("Desafío no encontrado"));

        if (!challenge.getChallenged().getId().equals(userId)) {
            throw new ConflictException("No tienes permiso para rechazar este desafío");
        }

        challenge.setStatus(ChallengeStatus.REJECTED);
        return toResponseDTO(challengeRepository.save(challenge));
    }

    @Transactional
    public ChallengeResponseDTO addProgress(UUID userId, UUID challengeId, ChallengeProgressCreateDTO dto) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new NotFoundException("Desafío no encontrado"));
        User user = userService.getUserById(userId);

        if (!isParticipant(challenge, userId)) {
            throw new ConflictException("No participas en este pique");
        }

        if (challenge.getStatus() != ChallengeStatus.ACCEPTED) {
            throw new ConflictException("Solo puedes registrar avances en piques activos");
        }

        BigDecimal weight = dto.weight();
        if (weight == null || weight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ConflictException("El peso debe ser mayor que cero");
        }

        LocalDate entryDate = dto.entryDate() != null ? dto.entryDate() : LocalDate.now();
        ChallengeProgressEntry entry = progressEntryRepository
                .findByChallengeAndUserAndEntryDate(challenge, user, entryDate)
                .orElseGet(() -> ChallengeProgressEntry.builder()
                        .challenge(challenge)
                        .user(user)
                        .entryDate(entryDate)
                        .build());

        entry.setWeight(weight);
        progressEntryRepository.save(entry);

        if (challenge.getChallenger().getId().equals(userId)) {
            if (challenge.getChallengerWeight() == null || weight.compareTo(challenge.getChallengerWeight()) > 0) {
                challenge.setChallengerWeight(weight);
            }
        } else if (challenge.getChallengedWeight() == null || weight.compareTo(challenge.getChallengedWeight()) > 0) {
            challenge.setChallengedWeight(weight);
        }

        updateExerciseRecordIfHigher(user, challenge.getExerciseName(), weight);
        updateWinnerIfCompleted(challenge);

        return toResponseDTO(challengeRepository.save(challenge));
    }

    @Transactional
    public void updateExerciseRecord(UUID userId, UserExerciseRecordDTO dto) {
        backfillNullVersionsForChallengeUpdate();
        User user = userService.getUserById(userId);
        updateExerciseRecordIfHigher(user, dto.exerciseName(), dto.maxWeight());
    }

    private void backfillNullVersionsForChallengeUpdate() {
        jdbcTemplate.update("UPDATE users SET version = 0 WHERE version IS NULL");
        jdbcTemplate.update("UPDATE user_exercise_records SET version = 0 WHERE version IS NULL");
        jdbcTemplate.update("UPDATE challenges SET version = 0 WHERE version IS NULL");
    }

    private void updateExerciseRecordIfHigher(User user, String exerciseName, BigDecimal maxWeight) {
        UserExerciseRecord record = recordRepository.findByUserAndExerciseName(user, exerciseName)
                .orElseGet(() -> UserExerciseRecord.builder()
                        .user(user)
                        .exerciseName(exerciseName)
                        .build());

        // Update record if the new weight is higher
        if (record.getMaxWeight() == null || maxWeight.compareTo(record.getMaxWeight()) > 0) {
            normalizeVersion(record);
            record.setMaxWeight(maxWeight);
            record.setUpdatedAt(LocalDateTime.now());
            recordRepository.save(record);

            // Also update any active challenges involving this user and exercise
            updateActiveChallenges(user, exerciseName, maxWeight);
        }
    }

    private void updateActiveChallenges(User user, String exerciseName, BigDecimal newWeight) {
        List<Challenge> challenges = challengeRepository.findAllByUser(user);
        for (Challenge challenge : challenges) {
            if (challenge.getExerciseName().equalsIgnoreCase(exerciseName) && 
                challenge.getStatus() == ChallengeStatus.ACCEPTED) {
                
                if (challenge.getChallenger().getId().equals(user.getId())) {
                    challenge.setChallengerWeight(newWeight);
                } else {
                    challenge.setChallengedWeight(newWeight);
                }
                updateWinnerIfCompleted(challenge);
                normalizeVersion(challenge);
                challengeRepository.save(challenge);
            }
        }
    }

    private void normalizeVersion(UserExerciseRecord record) {
        if (record.getId() != null && record.getVersion() == null) {
            record.setVersion(0L);
        }
    }

    private void normalizeVersion(Challenge challenge) {
        if (challenge.getId() != null && challenge.getVersion() == null) {
            challenge.setVersion(0L);
        }
    }

    @Transactional
    public List<ChallengeResponseDTO> getMyChallenges(UUID userId) {
        User user = userService.getUserById(userId);
        List<Challenge> challenges = challengeRepository.findAllByUser(user);

        for (Challenge challenge : challenges) {
            if (challenge.getStatus() == ChallengeStatus.ACCEPTED && challenge.getWinner() == null) {
                updateWinnerIfCompleted(challenge);
                if (challenge.getWinner() != null) {
                    normalizeVersion(challenge);
                    challengeRepository.save(challenge);
                }
            }
        }

        return challenges.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Scheduled(fixedRate = 3600000) // Every hour
    public void autoExpireChallenges() {
        List<Challenge> expired = challengeRepository.findByStatusInAndExpiresAtBefore(
                List.of(ChallengeStatus.PENDING, ChallengeStatus.ACCEPTED),
                LocalDateTime.now()
        );

        if (!expired.isEmpty()) {
            expired.forEach(c -> c.setStatus(ChallengeStatus.EXPIRED));
            challengeRepository.saveAll(expired);
            log.info("Auto-expired {} challenges", expired.size());
        }
    }

    @Transactional(readOnly = true)
    public List<UserExerciseRecord> getMyRecords(UUID userId) {
        User user = userService.getUserById(userId);
        return recordRepository.findByUser(user);
    }

    private ChallengeResponseDTO toResponseDTO(Challenge challenge) {
        normalizeChallengeDefaults(challenge);

        List<ChallengeProgressEntryDTO> progressEntries = progressEntryRepository
                .findByChallengeOrderByEntryDateAscCreatedAtAsc(challenge)
                .stream()
                .map(this::toProgressDTO)
                .collect(Collectors.toList());

        return new ChallengeResponseDTO(
                challenge.getId(),
                toSummaryDTO(challenge.getChallenger()),
                toSummaryDTO(challenge.getChallenged()),
                challenge.getExerciseName(),
                challenge.getStatus(),
                challenge.getChallengerWeight(),
                challenge.getChallengedWeight(),
                getTargetIncrease(challenge),
                getTargetWeight(challenge),
                calculateProgressPercent(challenge.getChallengerWeight(), getStartingWeight(challenge, challenge.getChallenger()), getTargetWeight(challenge)),
                calculateProgressPercent(challenge.getChallengedWeight(), getStartingWeight(challenge, challenge.getChallenged()), getTargetWeight(challenge)),
                challenge.getWinner() != null ? toSummaryDTO(challenge.getWinner()) : null,
                challenge.getCompletedAt(),
                progressEntries,
                challenge.getCreatedAt(),
                challenge.getExpiresAt()
        );
    }

    private UserSummaryDTO toSummaryDTO(User user) {
        return new UserSummaryDTO(user.getId(), user.getUsername(), user.getProfilePictureUrl());
    }

    private void normalizeChallengeDefaults(Challenge challenge) {
        if (challenge.getChallengerWeight() == null) {
            challenge.setChallengerWeight(BigDecimal.ZERO);
        }
        if (challenge.getChallengedWeight() == null) {
            challenge.setChallengedWeight(BigDecimal.ZERO);
        }
        if (challenge.getChallengerStartWeight() == null) {
            challenge.setChallengerStartWeight(challenge.getChallengerWeight());
        }
        if (challenge.getChallengedStartWeight() == null) {
            challenge.setChallengedStartWeight(challenge.getChallengedWeight());
        }
        if (challenge.getTargetIncreaseKg() == null || challenge.getTargetIncreaseKg().compareTo(BigDecimal.ZERO) <= 0) {
            challenge.setTargetIncreaseKg(BigDecimal.TEN);
        }
        if (challenge.getTargetWeightKg() == null || challenge.getTargetWeightKg().compareTo(BigDecimal.ZERO) <= 0) {
            BigDecimal highestStartWeight = getStartingWeight(challenge, challenge.getChallenger())
                    .max(getStartingWeight(challenge, challenge.getChallenged()));
            challenge.setTargetWeightKg(highestStartWeight.add(getTargetIncrease(challenge)));
        }
    }

    private ChallengeProgressEntryDTO toProgressDTO(ChallengeProgressEntry entry) {
        return new ChallengeProgressEntryDTO(
                entry.getId(),
                toSummaryDTO(entry.getUser()),
                entry.getEntryDate(),
                entry.getWeight(),
                entry.getCreatedAt()
        );
    }

    private boolean isParticipant(Challenge challenge, UUID userId) {
        return challenge.getChallenger().getId().equals(userId) || challenge.getChallenged().getId().equals(userId);
    }

    private BigDecimal getTargetIncrease(Challenge challenge) {
        return challenge.getTargetIncreaseKg() != null && challenge.getTargetIncreaseKg().compareTo(BigDecimal.ZERO) > 0
                ? challenge.getTargetIncreaseKg()
                : BigDecimal.TEN;
    }

    private BigDecimal getTargetWeight(Challenge challenge) {
        if (challenge.getTargetWeightKg() != null && challenge.getTargetWeightKg().compareTo(BigDecimal.ZERO) > 0) {
            return challenge.getTargetWeightKg();
        }
        BigDecimal highestStartWeight = getStartingWeight(challenge, challenge.getChallenger())
                .max(getStartingWeight(challenge, challenge.getChallenged()));
        return highestStartWeight.add(getTargetIncrease(challenge));
    }

    private BigDecimal getValidCreateTargetWeight(BigDecimal requestedTargetWeight, BigDecimal highestStartWeight) {
        if (requestedTargetWeight == null || requestedTargetWeight.compareTo(BigDecimal.ZERO) <= 0) {
            return highestStartWeight.add(BigDecimal.TEN);
        }
        if (requestedTargetWeight.compareTo(highestStartWeight) <= 0) {
            throw new ConflictException("La marca objetivo debe ser mayor que las marcas actuales de ambos usuarios");
        }
        return requestedTargetWeight;
    }

    private BigDecimal getStartingWeight(Challenge challenge, User user) {
        if (challenge.getChallenger().getId().equals(user.getId())) {
            if (challenge.getChallengerStartWeight() != null) {
                return challenge.getChallengerStartWeight();
            }
            return challenge.getChallengerWeight() != null ? challenge.getChallengerWeight() : BigDecimal.ZERO;
        }
        if (challenge.getChallengedStartWeight() != null) {
            return challenge.getChallengedStartWeight();
        }
        return challenge.getChallengedWeight() != null ? challenge.getChallengedWeight() : BigDecimal.ZERO;
    }

    private double calculateProgressPercent(BigDecimal currentWeight, BigDecimal startingWeight, BigDecimal targetWeight) {
        if (currentWeight == null || startingWeight == null || targetWeight == null || targetWeight.compareTo(startingWeight) <= 0) {
            return 0;
        }
        BigDecimal improvement = currentWeight.subtract(startingWeight);
        if (improvement.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        BigDecimal requiredImprovement = targetWeight.subtract(startingWeight);
        BigDecimal percent = improvement
                .multiply(BigDecimal.valueOf(100))
                .divide(requiredImprovement, 2, RoundingMode.HALF_UP);
        return Math.min(100, percent.doubleValue());
    }

    private void updateWinnerIfCompleted(Challenge challenge) {
        if (challenge.getWinner() != null) {
            return;
        }

        BigDecimal target = getTargetWeight(challenge);
        double challengerProgress = calculateProgressPercent(
                challenge.getChallengerWeight(),
                getStartingWeight(challenge, challenge.getChallenger()),
                target
        );
        double challengedProgress = calculateProgressPercent(
                challenge.getChallengedWeight(),
                getStartingWeight(challenge, challenge.getChallenged()),
                target
        );

        if (challengerProgress >= 100) {
            challenge.setWinner(challenge.getChallenger());
        } else if (challengedProgress >= 100) {
            challenge.setWinner(challenge.getChallenged());
        }

        if (challenge.getWinner() != null) {
            challenge.setStatus(ChallengeStatus.FINISHED);
            challenge.setCompletedAt(LocalDateTime.now());
        }
    }
}
