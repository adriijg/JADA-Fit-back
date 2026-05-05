package es.jadafit.jadafit_api.controller;

import es.jadafit.jadafit_api.dto.UserProfileDTO;
import es.jadafit.jadafit_api.dto.UserSummaryDTO;
import es.jadafit.jadafit_api.exception.UnauthorizedException;
import es.jadafit.jadafit_api.service.SocialService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/social")
public class SocialController {

    private final SocialService socialService;

    public SocialController(SocialService socialService) {
        this.socialService = socialService;
    }

    @PostMapping("/follow/{userId}")
    public ResponseEntity<Void> followUser(
            Authentication authentication,
            @PathVariable UUID userId
    ) {
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        socialService.followUser(currentUserId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/unfollow/{userId}")
    public ResponseEntity<Void> unfollowUser(
            Authentication authentication,
            @PathVariable UUID userId
    ) {
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        socialService.unfollowUser(currentUserId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<UserSummaryDTO>> getFollowers(@PathVariable UUID userId) {
        return ResponseEntity.ok(socialService.getFollowers(userId));
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<List<UserSummaryDTO>> getFollowing(@PathVariable UUID userId) {
        return ResponseEntity.ok(socialService.getFollowing(userId));
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<UserSummaryDTO>> searchUsers(@RequestParam String q) {
        return ResponseEntity.ok(socialService.searchUsers(q));
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfileDTO> getUserProfile(
            Authentication authentication,
            @PathVariable UUID userId
    ) {
        UUID currentUserId = null;
        if (authentication != null && authentication.getName() != null) {
            try {
                currentUserId = UUID.fromString(authentication.getName());
            } catch (IllegalArgumentException ignored) {
            }
        }
        
        return ResponseEntity.ok(socialService.getUserProfile(userId, currentUserId));
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
