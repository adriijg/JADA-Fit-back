package es.jadafit.jadafit_api.controller;

import es.jadafit.jadafit_api.dto.PostCommentCreateDTO;
import es.jadafit.jadafit_api.dto.PostCommentDTO;
import es.jadafit.jadafit_api.exception.UnauthorizedException;
import es.jadafit.jadafit_api.service.PostInteractionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/social/posts/{postId}")
public class PostInteractionController {

    private final PostInteractionService interactionService;

    public PostInteractionController(PostInteractionService interactionService) {
        this.interactionService = interactionService;
    }

    // ── Likes ──────────────────────────────────────────────────────────────────

    @PostMapping("/like")
    public ResponseEntity<Void> likePost(
            @PathVariable UUID postId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        interactionService.likePost(postId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/like")
    public ResponseEntity<Void> unlikePost(
            @PathVariable UUID postId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        interactionService.unlikePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    // ── Comments ───────────────────────────────────────────────────────────────

    @GetMapping("/comments")
    public ResponseEntity<List<PostCommentDTO>> getComments(@PathVariable UUID postId) {
        return ResponseEntity.ok(interactionService.getComments(postId));
    }

    @PostMapping("/comments")
    public ResponseEntity<PostCommentDTO> addComment(
            @PathVariable UUID postId,
            Authentication authentication,
            @Valid @RequestBody PostCommentCreateDTO createDTO
    ) {
        UUID userId = getUserId(authentication);
        PostCommentDTO comment = interactionService.addComment(postId, userId, createDTO);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    private UUID getUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("No autorizado");
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Token inválido");
        }
    }
}
