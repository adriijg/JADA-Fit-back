package es.jadafit.jadafit_api.controller;

import es.jadafit.jadafit_api.dto.ProfileProgressResponseDTO;
import es.jadafit.jadafit_api.dto.ProfileResponseDTO;
import es.jadafit.jadafit_api.dto.ProfileUpdateDTO;
import es.jadafit.jadafit_api.exception.UnauthorizedException;
import es.jadafit.jadafit_api.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponseDTO> getMyProfile(Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);

        ProfileResponseDTO response = profileService.getMyProfile(userId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponseDTO> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdateDTO dto
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        ProfileResponseDTO response = profileService.updateMyProfile(userId, dto);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/progress")
    public ResponseEntity<List<ProfileProgressResponseDTO>> getMyProgress(
            Authentication authentication
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        List<ProfileProgressResponseDTO> response = profileService.getMyProgress(userId);

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