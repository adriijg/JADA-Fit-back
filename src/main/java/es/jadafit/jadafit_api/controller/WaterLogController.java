package es.jadafit.jadafit_api.controller;

import es.jadafit.jadafit_api.dto.WaterLogCreateDTO;
import es.jadafit.jadafit_api.dto.WaterLogResponseDTO;
import es.jadafit.jadafit_api.dto.WaterDaySummaryDTO;
import es.jadafit.jadafit_api.exception.UnauthorizedException;
import es.jadafit.jadafit_api.service.WaterLogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/nutrition/water")
public class WaterLogController {

    private final WaterLogService waterLogService;

    public WaterLogController(WaterLogService waterLogService) {
        this.waterLogService = waterLogService;
    }

    @GetMapping("/today")
    public ResponseEntity<WaterDaySummaryDTO> getTodaySummary(
            Authentication authentication
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);
        WaterDaySummaryDTO response = waterLogService.getTodaySummary(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<WaterLogResponseDTO> createLog(
            Authentication authentication,
            @Valid @RequestBody WaterLogCreateDTO dto
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);
        WaterLogResponseDTO response = waterLogService.createLog(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> deleteLog(
            Authentication authentication,
            @PathVariable UUID logId
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);
        waterLogService.deleteLog(userId, logId);
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
