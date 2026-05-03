package es.jadafit.jadafit_api.controller;

import es.jadafit.jadafit_api.dto.NutritionGoalResponseDTO;
import es.jadafit.jadafit_api.dto.NutritionGoalUpdateDTO;
import es.jadafit.jadafit_api.exception.UnauthorizedException;
import es.jadafit.jadafit_api.service.NutritionGoalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/nutrition/goals")
public class NutritionGoalController {

    private final NutritionGoalService nutritionGoalService;

    public NutritionGoalController(NutritionGoalService nutritionGoalService) {
        this.nutritionGoalService = nutritionGoalService;
    }

    @GetMapping("/me")
    public ResponseEntity<NutritionGoalResponseDTO> getMyNutritionGoal(
            Authentication authentication
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        NutritionGoalResponseDTO response = nutritionGoalService.getMyNutritionGoal(userId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/recalculate")
    public ResponseEntity<NutritionGoalResponseDTO> recalculateMyNutritionGoal(
            Authentication authentication
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        NutritionGoalResponseDTO response = nutritionGoalService.recalculateMyNutritionGoal(userId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<NutritionGoalResponseDTO> updateMyNutritionGoalManually(
            Authentication authentication,
            @Valid @RequestBody NutritionGoalUpdateDTO dto
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        NutritionGoalResponseDTO response =
                nutritionGoalService.updateMyNutritionGoalManually(userId, dto);

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