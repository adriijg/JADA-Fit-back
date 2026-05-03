package es.jadafit.jadafit_api.controller;

import es.jadafit.jadafit_api.dto.FitnessProfileResponseDTO;
import es.jadafit.jadafit_api.dto.FitnessProfileUpdateDTO;
import es.jadafit.jadafit_api.exception.UnauthorizedException;
import es.jadafit.jadafit_api.service.FitnessProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/fitness-profile")
public class FitnessProfileController {

    private final FitnessProfileService fitnessProfileService;

    public FitnessProfileController(FitnessProfileService fitnessProfileService) {
        this.fitnessProfileService = fitnessProfileService;
    }

    @GetMapping("/me")
    public ResponseEntity<FitnessProfileResponseDTO> getMyFitnessProfile(
            Authentication authentication
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        FitnessProfileResponseDTO response =
                fitnessProfileService.getMyFitnessProfile(userId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<FitnessProfileResponseDTO> updateMyFitnessProfile(
            Authentication authentication,
            @Valid @RequestBody FitnessProfileUpdateDTO dto
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        FitnessProfileResponseDTO response =
                fitnessProfileService.updateMyFitnessProfile(userId, dto);

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