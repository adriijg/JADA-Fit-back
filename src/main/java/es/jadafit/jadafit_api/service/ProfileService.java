package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.ProfileProgressResponseDTO;
import es.jadafit.jadafit_api.dto.ProfileResponseDTO;
import es.jadafit.jadafit_api.dto.ProfileUpdateDTO;
import es.jadafit.jadafit_api.model.ProfileProgressLog;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.model.UserProfile;
import es.jadafit.jadafit_api.repository.ProfileProgressRepository;
import es.jadafit.jadafit_api.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProfileService {

    private final UserService userService;
    private final UserProfileRepository userProfileRepository;
    private final ProfileProgressRepository profileProgressRepository;

    public ProfileService(
            UserService userService,
            UserProfileRepository userProfileRepository,
            ProfileProgressRepository profileProgressRepository
    ) {
        this.userService = userService;
        this.userProfileRepository = userProfileRepository;
        this.profileProgressRepository = profileProgressRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponseDTO getMyProfile(UUID userId) {
        User user = userService.getUserById(userId);

        return userProfileRepository.findByUserId(userId)
                .map(profile -> toResponse(user, profile))
                .orElseGet(() -> toResponseWithoutProfile(user));
    }

    @Transactional
    public ProfileResponseDTO updateMyProfile(UUID userId, ProfileUpdateDTO dto) {
        User user = userService.getUserById(userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> UserProfile.builder()
                        .user(user)
                        .build());

        profile.setWeight(dto.weight());
        profile.setHeight(dto.height());
        profile.setAge(dto.age());
        profile.setGender(dto.gender());

        if (dto.goal() == null || dto.goal().isBlank()) {
            profile.setGoal("mantenimiento");
        } else {
            profile.setGoal(normalizeText(dto.goal()));
        }

        profile.setBodyFat(dto.bodyFat());
        profile.setMuscleMass(dto.muscleMass());
        profile.setUpdatedAt(LocalDateTime.now());

        UserProfile savedProfile = userProfileRepository.save(profile);

        saveProgressLog(user, dto);

        return toResponse(user, savedProfile);
    }

    @Transactional(readOnly = true)
    public List<ProfileProgressResponseDTO> getMyProgress(UUID userId) {
        return profileProgressRepository.findByUserIdOrderByLoggedAtAsc(userId)
                .stream()
                .map(this::toProgressResponse)
                .toList();
    }

    private void saveProgressLog(User user, ProfileUpdateDTO dto) {
        boolean hasProgressData =
                dto.weight() != null ||
                        dto.bodyFat() != null ||
                        dto.muscleMass() != null;

        if (!hasProgressData) {
            return;
        }

        var lastLogOptional =
                profileProgressRepository.findFirstByUserIdOrderByLoggedAtDesc(user.getId());

        if (lastLogOptional.isPresent()) {
            ProfileProgressLog lastLog = lastLogOptional.get();

            boolean sameWeight = areEqual(lastLog.getWeight(), dto.weight());
            boolean sameBodyFat = areEqual(lastLog.getBodyFat(), dto.bodyFat());
            boolean sameMuscleMass = areEqual(lastLog.getMuscleMass(), dto.muscleMass());

            if (sameWeight && sameBodyFat && sameMuscleMass) {
                return;
            }
        }

        ProfileProgressLog progressLog = ProfileProgressLog.builder()
                .user(user)
                .weight(dto.weight())
                .bodyFat(dto.bodyFat())
                .muscleMass(dto.muscleMass())
                .loggedAt(LocalDateTime.now())
                .build();

        profileProgressRepository.save(progressLog);
    }

    private boolean areEqual(BigDecimal first, BigDecimal second) {
        if (first == null && second == null) {
            return true;
        }

        if (first == null || second == null) {
            return false;
        }

        return first.compareTo(second) == 0;
    }

    private ProfileProgressResponseDTO toProgressResponse(ProfileProgressLog log) {
        return new ProfileProgressResponseDTO(
                log.getId(),
                log.getWeight(),
                log.getBodyFat(),
                log.getMuscleMass(),
                log.getLoggedAt()
        );
    }

    private ProfileResponseDTO toResponse(User user, UserProfile profile) {
        return new ProfileResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                profile.getWeight(),
                profile.getHeight(),
                profile.getAge(),
                profile.getGender(),
                profile.getGoal(),
                profile.getBodyFat(),
                profile.getMuscleMass(),
                profile.getUpdatedAt()
        );
    }

    private ProfileResponseDTO toResponseWithoutProfile(User user) {
        return new ProfileResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }
}