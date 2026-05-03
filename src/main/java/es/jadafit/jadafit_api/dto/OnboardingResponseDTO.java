package es.jadafit.jadafit_api.dto;

public record OnboardingResponseDTO(
        Boolean onboardingCompleted,
        FitnessProfileResponseDTO profile
) {
}