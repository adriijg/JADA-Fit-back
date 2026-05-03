package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.FitnessProfileResponseDTO;
import es.jadafit.jadafit_api.dto.FitnessProfileUpdateDTO;
import es.jadafit.jadafit_api.model.FitnessProfile;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.repository.FitnessProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FitnessProfileService {

    private final UserService userService;
    private final FitnessProfileRepository fitnessProfileRepository;

    public FitnessProfileService(
            UserService userService,
            FitnessProfileRepository fitnessProfileRepository
    ) {
        this.userService = userService;
        this.fitnessProfileRepository = fitnessProfileRepository;
    }

    @Transactional(readOnly = true)
    public FitnessProfileResponseDTO getMyFitnessProfile(UUID userId) {
        User user = userService.getUserById(userId);

        return fitnessProfileRepository.findByUserId(userId)
                .map(profile -> toFitnessProfileResponse(user, profile))
                .orElseGet(() -> toFitnessProfileResponseWithoutProfile(user));
    }

    @Transactional
    public FitnessProfileResponseDTO updateMyFitnessProfile(
            UUID userId,
            FitnessProfileUpdateDTO dto
    ) {
        User user = userService.getUserById(userId);

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

        return toFitnessProfileResponse(user, savedProfile);
    }

    private FitnessProfileResponseDTO toFitnessProfileResponse(
            User user,
            FitnessProfile profile
    ) {
        return new FitnessProfileResponseDTO(
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

    private FitnessProfileResponseDTO toFitnessProfileResponseWithoutProfile(User user) {
        return new FitnessProfileResponseDTO(
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
}