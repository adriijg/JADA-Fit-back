package es.jadafit.jadafit_api.controller;

import es.jadafit.jadafit_api.dto.RecipeCreateDTO;
import es.jadafit.jadafit_api.dto.RecipeResponseDTO;
import es.jadafit.jadafit_api.exception.UnauthorizedException;
import es.jadafit.jadafit_api.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping
    public ResponseEntity<RecipeResponseDTO> createRecipe(
            Authentication authentication,
            @Valid @RequestBody RecipeCreateDTO dto
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        RecipeResponseDTO response = recipeService.createRecipe(userId, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RecipeResponseDTO>> getMyRecipes(
            Authentication authentication
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        List<RecipeResponseDTO> response = recipeService.getMyRecipes(userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{recipeId}")
    public ResponseEntity<RecipeResponseDTO> getRecipeById(
            Authentication authentication,
            @PathVariable UUID recipeId
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        RecipeResponseDTO response = recipeService.getRecipeById(userId, recipeId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{recipeId}")
    public ResponseEntity<RecipeResponseDTO> updateRecipe(
            Authentication authentication,
            @PathVariable UUID recipeId,
            @Valid @RequestBody RecipeCreateDTO dto
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        RecipeResponseDTO response = recipeService.updateRecipe(userId, recipeId, dto);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{recipeId}")
    public ResponseEntity<Void> deleteRecipe(
            Authentication authentication,
            @PathVariable UUID recipeId
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        recipeService.deleteRecipe(userId, recipeId);

        return ResponseEntity.noContent().build();
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
