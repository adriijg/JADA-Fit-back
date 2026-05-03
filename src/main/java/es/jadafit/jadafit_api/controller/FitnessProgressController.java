package es.jadafit.jadafit_api.controller;

import es.jadafit.jadafit_api.dto.FitnessProgressCreateDTO;
import es.jadafit.jadafit_api.dto.FitnessProgressResponseDTO;
import es.jadafit.jadafit_api.exception.UnauthorizedException;
import es.jadafit.jadafit_api.service.FitnessProgressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fitness-progress")
public class FitnessProgressController {

    private final FitnessProgressService fitnessProgressService;

    public FitnessProgressController(FitnessProgressService fitnessProgressService) {
        this.fitnessProgressService = fitnessProgressService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<FitnessProgressResponseDTO>> getMyFitnessProgress(
            Authentication authentication
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        List<FitnessProgressResponseDTO> response =
                fitnessProgressService.getMyFitnessProgress(userId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/me")
    public ResponseEntity<FitnessProgressResponseDTO> createMyFitnessProgress(
            Authentication authentication,
            @Valid @RequestBody FitnessProgressCreateDTO dto
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        FitnessProgressResponseDTO response =
                fitnessProgressService.createMyFitnessProgress(userId, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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