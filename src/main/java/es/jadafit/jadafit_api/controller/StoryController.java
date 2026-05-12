package es.jadafit.jadafit_api.controller;

import es.jadafit.jadafit_api.dto.StoryCreateDTO;
import es.jadafit.jadafit_api.dto.StoryDTO;
import es.jadafit.jadafit_api.exception.UnauthorizedException;
import es.jadafit.jadafit_api.service.StoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/social/stories")
public class StoryController {

    private final StoryService storyService;

    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    @PostMapping
    public ResponseEntity<StoryDTO> createStory(
            Authentication authentication,
            @Valid @RequestBody StoryCreateDTO createDTO
    ) {
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        return new ResponseEntity<>(storyService.createStory(currentUserId, createDTO), HttpStatus.CREATED);
    }

    @GetMapping("/feed")
    public ResponseEntity<List<StoryDTO>> getFeedStories(Authentication authentication) {
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(storyService.getFeedStories(currentUserId));
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
