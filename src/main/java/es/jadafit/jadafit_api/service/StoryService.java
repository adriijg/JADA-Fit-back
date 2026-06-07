package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.StoryCreateDTO;
import es.jadafit.jadafit_api.dto.StoryDTO;
import es.jadafit.jadafit_api.dto.UserSummaryDTO;
import es.jadafit.jadafit_api.model.Story;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.model.UserFollow;
import es.jadafit.jadafit_api.repository.StoryRepository;
import es.jadafit.jadafit_api.repository.UserFollowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StoryService {

    private final StoryRepository storyRepository;
    private final UserService userService;
    private final UserFollowRepository userFollowRepository;
    private final FileUploadService fileUploadService;

    public StoryService(StoryRepository storyRepository, UserService userService, UserFollowRepository userFollowRepository, FileUploadService fileUploadService) {
        this.storyRepository = storyRepository;
        this.userService = userService;
        this.userFollowRepository = userFollowRepository;
        this.fileUploadService = fileUploadService;
    }

    @Transactional
    public StoryDTO createStory(UUID authorId, StoryCreateDTO createDTO) {
        User author = userService.getUserById(authorId);

        Story story = Story.builder()
                .author(author)
                .imageUrl(createDTO.imageUrl())
                // createdAt and expiresAt (24h) are handled by defaults in builder
                .build();

        story = storyRepository.save(story);
        return mapToDTO(story);
    }

    @Transactional
    public void deleteStory(UUID storyId, UUID userId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Historia no encontrada"));

        if (!story.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("No puedes eliminar una historia que no te pertenece");
        }

        fileUploadService.deleteImage(story.getImageUrl());
        storyRepository.delete(story);
    }

    @Transactional(readOnly = true)
    public List<StoryDTO> getFeedStories(UUID currentUserId) {
        User currentUser = userService.getUserById(currentUserId);
        
        List<User> followedUsers = userFollowRepository.findByFollower(currentUser)
                .stream()
                .map(UserFollow::getFollowing)
                .collect(Collectors.toList());
        
        // Add current user to see their own stories
        followedUsers.add(currentUser);

        return storyRepository.findActiveStoriesByAuthors(followedUsers, LocalDateTime.now())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private StoryDTO mapToDTO(Story story) {
        UserSummaryDTO authorDTO = new UserSummaryDTO(
                story.getAuthor().getId(),
                story.getAuthor().getUsername(),
                story.getAuthor().getProfilePictureUrl()
        );

        return new StoryDTO(
                story.getId(),
                authorDTO,
                story.getImageUrl(),
                story.getCreatedAt(),
                story.getExpiresAt()
        );
    }
}
