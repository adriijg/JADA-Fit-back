package es.jadafit.jadafit_api.controller;

import es.jadafit.jadafit_api.dto.NutritionDaySummaryResponseDTO;
import es.jadafit.jadafit_api.dto.NutritionMealCreateDTO;
import es.jadafit.jadafit_api.dto.NutritionMealResponseDTO;
import es.jadafit.jadafit_api.dto.RecipeToMealDTO;
import es.jadafit.jadafit_api.exception.UnauthorizedException;
import es.jadafit.jadafit_api.service.NutritionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/nutrition")
public class NutritionController {

    private final NutritionService nutritionService;

    public NutritionController(NutritionService nutritionService) {
        this.nutritionService = nutritionService;
    }

    @PostMapping("/meals")
    public ResponseEntity<NutritionMealResponseDTO> createMeal(
            Authentication authentication,
            @Valid @RequestBody NutritionMealCreateDTO dto
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        NutritionMealResponseDTO response = nutritionService.createMeal(userId, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/day")
    public ResponseEntity<NutritionDaySummaryResponseDTO> getDaySummary(
            Authentication authentication,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        NutritionDaySummaryResponseDTO response = nutritionService.getDaySummary(userId, date);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/meals/{mealId}")
    public ResponseEntity<Void> deleteMeal(
            Authentication authentication,
            @PathVariable UUID mealId
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        nutritionService.deleteMeal(userId, mealId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/meals/from-recipe")
    public ResponseEntity<List<NutritionMealResponseDTO>> createMealsFromRecipe(
            Authentication authentication,
            @Valid @RequestBody RecipeToMealDTO dto
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        List<NutritionMealResponseDTO> response = nutritionService.createMealsFromRecipe(userId, dto);

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