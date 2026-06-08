package es.jadafit.jadafit_api.controller;

import es.jadafit.jadafit_api.dto.ChallengeCreateDTO;
import es.jadafit.jadafit_api.dto.ChallengeProgressCreateDTO;
import es.jadafit.jadafit_api.dto.ChallengeResponseDTO;
import es.jadafit.jadafit_api.dto.UserExerciseRecordDTO;
import es.jadafit.jadafit_api.exception.UnauthorizedException;
import es.jadafit.jadafit_api.model.UserExerciseRecord;
import es.jadafit.jadafit_api.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @PostMapping
    public ResponseEntity<ChallengeResponseDTO> createChallenge(
            Authentication authentication,
            @RequestBody ChallengeCreateDTO dto
    ) {
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(challengeService.createChallenge(currentUserId, dto));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ChallengeResponseDTO> acceptChallenge(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(challengeService.acceptChallenge(currentUserId, id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ChallengeResponseDTO> rejectChallenge(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(challengeService.rejectChallenge(currentUserId, id));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ChallengeResponseDTO>> getMyChallenges(Authentication authentication) {
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(challengeService.getMyChallenges(currentUserId));
    }

    @PostMapping("/{id}/progress")
    public ResponseEntity<ChallengeResponseDTO> addProgress(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody ChallengeProgressCreateDTO dto
    ) {
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(challengeService.addProgress(currentUserId, id, dto));
    }

    @PostMapping("/records")
    public ResponseEntity<Void> updateRecord(
            Authentication authentication,
            @RequestBody UserExerciseRecordDTO dto
    ) {
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        challengeService.updateExerciseRecord(currentUserId, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/records/my")
    public ResponseEntity<List<UserExerciseRecord>> getMyRecords(Authentication authentication) {
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(challengeService.getMyRecords(currentUserId));
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
