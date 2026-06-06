package es.jadafit.jadafit_api.controller;

import es.jadafit.jadafit_api.dto.PostCreateDTO;
import es.jadafit.jadafit_api.dto.PostDTO;
import es.jadafit.jadafit_api.exception.UnauthorizedException;
import es.jadafit.jadafit_api.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/social/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<PostDTO> createPost(
            Authentication authentication,
            @Valid @RequestBody PostCreateDTO createDTO
    ) {
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        return new ResponseEntity<>(postService.createPost(currentUserId, createDTO), HttpStatus.CREATED);
    }

    @GetMapping("/feed")
    public ResponseEntity<List<PostDTO>> getFeed(Authentication authentication) {
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(postService.getFeed(currentUserId));
    }

    @GetMapping("/explore")
    public ResponseEntity<List<PostDTO>> getExplore() {
        return ResponseEntity.ok(postService.getExplore());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostDTO>> getUserPosts(@PathVariable UUID userId) {
        return ResponseEntity.ok(postService.getUserPosts(userId));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            Authentication authentication,
            @PathVariable UUID postId
    ) {
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        postService.deletePost(postId, currentUserId);
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
