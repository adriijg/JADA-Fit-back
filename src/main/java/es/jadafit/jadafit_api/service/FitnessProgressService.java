package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.FitnessProgressCreateDTO;
import es.jadafit.jadafit_api.dto.FitnessProgressResponseDTO;
import es.jadafit.jadafit_api.model.FitnessProfile;
import es.jadafit.jadafit_api.model.FitnessProgressLog;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.repository.FitnessProfileRepository;
import es.jadafit.jadafit_api.repository.FitnessProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FitnessProgressService {

    private final UserService userService;
    private final FitnessProfileRepository fitnessProfileRepository;
    private final FitnessProgressRepository fitnessProgressRepository;

    public FitnessProgressService(
            UserService userService,
            FitnessProfileRepository fitnessProfileRepository,
            FitnessProgressRepository fitnessProgressRepository
    ) {
        this.userService = userService;
        this.fitnessProfileRepository = fitnessProfileRepository;
        this.fitnessProgressRepository = fitnessProgressRepository;
    }

    @Transactional(readOnly = true)
    public List<FitnessProgressResponseDTO> getMyFitnessProgress(UUID userId) {
        return fitnessProgressRepository.findByUserIdOrderByLoggedAtAsc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public FitnessProgressResponseDTO createMyFitnessProgress(
            UUID userId,
            FitnessProgressCreateDTO dto
    ) {
        User user = userService.getUserById(userId);

        LocalDateTime loggedAt = dto.loggedAt() != null
                ? dto.loggedAt()
                : LocalDateTime.now();

        boolean shouldUpdateCurrentProfile = shouldUpdateCurrentProfile(
                userId,
                loggedAt
        );

        FitnessProgressLog progressLog = FitnessProgressLog.builder()
                .user(user)
                .weight(dto.weight())
                .bodyFat(dto.bodyFat())
                .muscleMass(dto.muscleMass())
                .loggedAt(loggedAt)
                .build();

        FitnessProgressLog savedLog = fitnessProgressRepository.save(progressLog);

        if (shouldUpdateCurrentProfile) {
            updateCurrentFitnessProfile(user, dto);
        }

        return toResponse(savedLog);
    }

    private boolean shouldUpdateCurrentProfile(UUID userId, LocalDateTime newLoggedAt) {
        return fitnessProgressRepository.findFirstByUserIdOrderByLoggedAtDesc(userId)
                .map(lastLog -> !newLoggedAt.isBefore(lastLog.getLoggedAt()))
                .orElse(true);
    }

    private void updateCurrentFitnessProfile(
            User user,
            FitnessProgressCreateDTO dto
    ) {
        FitnessProfile profile = fitnessProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> FitnessProfile.builder()
                        .user(user)
                        .build());

        profile.setWeight(dto.weight());
        profile.setBodyFat(dto.bodyFat());
        profile.setMuscleMass(dto.muscleMass());
        profile.setUpdatedAt(LocalDateTime.now());

        fitnessProfileRepository.save(profile);
    }

    private FitnessProgressResponseDTO toResponse(FitnessProgressLog log) {
        return new FitnessProgressResponseDTO(
                log.getId(),
                log.getWeight(),
                log.getBodyFat(),
                log.getMuscleMass(),
                log.getLoggedAt()
        );
    }
}