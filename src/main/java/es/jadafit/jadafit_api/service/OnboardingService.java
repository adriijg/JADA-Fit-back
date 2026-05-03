package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.FitnessProfileResponseDTO;
import es.jadafit.jadafit_api.dto.OnboardingRequestDTO;
import es.jadafit.jadafit_api.dto.OnboardingResponseDTO;
import es.jadafit.jadafit_api.model.FitnessProfile;
import es.jadafit.jadafit_api.model.FitnessProgressLog;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.repository.FitnessProfileRepository;
import es.jadafit.jadafit_api.repository.FitnessProgressRepository;
import es.jadafit.jadafit_api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OnboardingService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final FitnessProfileRepository fitnessProfileRepository;
    private final FitnessProgressRepository fitnessProgressRepository;

    public OnboardingService(
            UserService userService,
            UserRepository userRepository,
            FitnessProfileRepository fitnessProfileRepository,
            FitnessProgressRepository fitnessProgressRepository
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.fitnessProfileRepository = fitnessProfileRepository;
        this.fitnessProgressRepository = fitnessProgressRepository;
    }

    @Transactional
    public OnboardingResponseDTO completeOnboarding(UUID userId, OnboardingRequestDTO dto) {
        User user = userService.getUserById(userId);

        boolean wasAlreadyCompleted = Boolean.TRUE.equals(user.getOnboardingCompleted());

        FitnessProfile profile = fitnessProfileRepository.findByUserId(userId)
                .orElseGet(() -> FitnessProfile.builder()
                        .user(user)
                        .build());

        profile.setWeight(dto.weight());
        profile.setHeight(dto.height());
        profile.setAge(dto.age());
        profile.setGender(dto.gender());
        profile.setGoal(dto.goal());
        profile.setBodyFat(dto.bodyFat());
        profile.setMuscleMass(dto.muscleMass());
        profile.setUpdatedAt(LocalDateTime.now());

        FitnessProfile savedProfile = fitnessProfileRepository.save(profile);

        if (!wasAlreadyCompleted) {
            saveInitialProgressLog(user, dto);
        }

        user.setOnboardingCompleted(true);
        userRepository.save(user);

        FitnessProfileResponseDTO profileResponse = new FitnessProfileResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                savedProfile.getWeight(),
                savedProfile.getHeight(),
                savedProfile.getAge(),
                savedProfile.getGender(),
                savedProfile.getGoal(),
                savedProfile.getBodyFat(),
                savedProfile.getMuscleMass(),
                savedProfile.getUpdatedAt()
        );

        return new OnboardingResponseDTO(
                user.getOnboardingCompleted(),
                profileResponse
        );
    }

    private void saveInitialProgressLog(User user, OnboardingRequestDTO dto) {
        boolean hasProgressData =
                dto.weight() != null ||
                        dto.bodyFat() != null ||
                        dto.muscleMass() != null;

        if (!hasProgressData) {
            return;
        }

        FitnessProgressLog progressLog = FitnessProgressLog.builder()
                .user(user)
                .weight(dto.weight())
                .bodyFat(dto.bodyFat())
                .muscleMass(dto.muscleMass())
                .loggedAt(LocalDateTime.now())
                .build();

        fitnessProgressRepository.save(progressLog);
    }
}