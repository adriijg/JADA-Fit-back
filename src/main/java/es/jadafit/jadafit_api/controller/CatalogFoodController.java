package es.jadafit.jadafit_api.controller;

import es.jadafit.jadafit_api.dto.CatalogFoodResponseDTO;
import es.jadafit.jadafit_api.dto.CustomFoodCreateDTO;
import es.jadafit.jadafit_api.dto.CustomFoodUpdateDTO;
import es.jadafit.jadafit_api.exception.UnauthorizedException;
import es.jadafit.jadafit_api.service.CatalogFoodService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/foods")
public class CatalogFoodController {

    private final CatalogFoodService catalogFoodService;

    public CatalogFoodController(CatalogFoodService catalogFoodService) {
        this.catalogFoodService = catalogFoodService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<CatalogFoodResponseDTO>> searchFoods(
            Authentication authentication,
            @RequestParam String query
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        List<CatalogFoodResponseDTO> response = catalogFoodService.searchFoods(userId, query);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<CatalogFoodResponseDTO> findByBarcode(
            Authentication authentication,
            @PathVariable String barcode
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        CatalogFoodResponseDTO response = catalogFoodService.findByBarcode(userId, barcode);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/custom")
    public ResponseEntity<CatalogFoodResponseDTO> createCustomFood(
            Authentication authentication,
            @Valid @RequestBody CustomFoodCreateDTO dto
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        CatalogFoodResponseDTO response = catalogFoodService.createCustomFood(userId, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-custom")
    public ResponseEntity<List<CatalogFoodResponseDTO>> getMyCustomFoods(
            Authentication authentication
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        List<CatalogFoodResponseDTO> response = catalogFoodService.getMyCustomFoods(userId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/custom/{foodId}")
    public ResponseEntity<CatalogFoodResponseDTO> updateCustomFood(
            Authentication authentication,
            @PathVariable UUID foodId,
            @Valid @RequestBody CustomFoodUpdateDTO dto
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        CatalogFoodResponseDTO response = catalogFoodService.updateCustomFood(userId, foodId, dto);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/custom/{foodId}")
    public ResponseEntity<Void> deleteCustomFood(
            Authentication authentication,
            @PathVariable UUID foodId
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);

        catalogFoodService.deleteCustomFood(userId, foodId);

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