package es.jadafit.jadafit_api.dto;

public record AuthResponseDTO(
        String token,
        String username,
        String email,
        Boolean onboardingCompleted
) {
}