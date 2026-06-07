package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.ChallengeCreateDTO;
import es.jadafit.jadafit_api.dto.ChallengeResponseDTO;
import es.jadafit.jadafit_api.dto.UserExerciseRecordDTO;
import es.jadafit.jadafit_api.dto.UserSummaryDTO;
import es.jadafit.jadafit_api.exception.ConflictException;
import es.jadafit.jadafit_api.exception.NotFoundException;
import es.jadafit.jadafit_api.model.*;
import es.jadafit.jadafit_api.repository.ChallengeRepository;
import es.jadafit.jadafit_api.repository.UserExerciseRecordRepository;
import es.jadafit.jadafit_api.repository.UserFollowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserExerciseRecordRepository recordRepository;
    private final UserFollowRepository followRepository;
    private final UserService userService;

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

        Challenge challenge = Challenge.builder()
                .challenger(challenger)
                .challenged(challenged)
                .exerciseName(dto.exerciseName())
                .status(ChallengeStatus.PENDING)
                .challengerWeight(challengerWeight)
                .challengedWeight(challengedWeight)
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
    public void updateExerciseRecord(UUID userId, UserExerciseRecordDTO dto) {
        User user = userService.getUserById(userId);

        UserExerciseRecord record = recordRepository.findByUserAndExerciseName(user, dto.exerciseName())
                .orElseGet(() -> UserExerciseRecord.builder()
                        .user(user)
                        .exerciseName(dto.exerciseName())
                        .build());

        // Update record if the new weight is higher
        if (record.getMaxWeight() == null || dto.maxWeight().compareTo(record.getMaxWeight()) > 0) {
            record.setMaxWeight(dto.maxWeight());
            record.setUpdatedAt(LocalDateTime.now());
            recordRepository.save(record);

            // Also update any active challenges involving this user and exercise
            updateActiveChallenges(user, dto.exerciseName(), dto.maxWeight());
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
                challengeRepository.save(challenge);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<ChallengeResponseDTO> getMyChallenges(UUID userId) {
        User user = userService.getUserById(userId);
        return challengeRepository.findAllByUser(user).stream()
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
        return new ChallengeResponseDTO(
                challenge.getId(),
                toSummaryDTO(challenge.getChallenger()),
                toSummaryDTO(challenge.getChallenged()),
                challenge.getExerciseName(),
                challenge.getStatus(),
                challenge.getChallengerWeight(),
                challenge.getChallengedWeight(),
                challenge.getCreatedAt(),
                challenge.getExpiresAt()
        );
    }

    private UserSummaryDTO toSummaryDTO(User user) {
        return new UserSummaryDTO(user.getId(), user.getUsername(), user.getProfilePictureUrl());
    }
}
