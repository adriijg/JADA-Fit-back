package es.jadafit.jadafit_api.controller;

import es.jadafit.jadafit_api.dto.OnboardingRequestDTO;
import es.jadafit.jadafit_api.dto.OnboardingResponseDTO;
import es.jadafit.jadafit_api.exception.UnauthorizedException;
import es.jadafit.jadafit_api.service.OnboardingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping("/complete")
    public ResponseEntity<OnboardingResponseDTO> completeOnboarding(
            Authentication authentication,
            @Valid @RequestBody OnboardingRequestDTO dto
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        OnboardingResponseDTO response = onboardingService.completeOnboarding(userId, dto);

        return ResponseEntity.ok(response);
    }

    private UUID getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("No autorizado");
        }

        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException("Token invalido");
        }
    }
}