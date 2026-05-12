package es.jadafit.jadafit_api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String username,
        String email,
        String bio,
        String profilePictureUrl,
        Boolean onboardingCompleted,
        LocalDateTime createdAt,
        Boolean shareProgress
) {
}