package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.PostCreateDTO;
import es.jadafit.jadafit_api.dto.PostDTO;
import es.jadafit.jadafit_api.dto.UserSummaryDTO;
import es.jadafit.jadafit_api.model.Post;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.model.UserFollow;
import es.jadafit.jadafit_api.repository.PostRepository;
import es.jadafit.jadafit_api.repository.UserFollowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final UserFollowRepository userFollowRepository;

    public PostService(PostRepository postRepository, UserService userService, UserFollowRepository userFollowRepository) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.userFollowRepository = userFollowRepository;
    }

    @Transactional
    public PostDTO createPost(UUID authorId, PostCreateDTO createDTO) {
        User author = userService.getUserById(authorId);

        Post post = Post.builder()
                .author(author)
                .imageUrl(createDTO.imageUrl())
                .caption(createDTO.caption())
                .build();

        post = postRepository.save(post);
        return mapToDTO(post);
    }

    public List<PostDTO> getFeed(UUID currentUserId) {
        User currentUser = userService.getUserById(currentUserId);
        
        List<User> followedUsers = userFollowRepository.findByFollower(currentUser)
                .stream()
                .map(UserFollow::getFollowing)
                .collect(Collectors.toList());
        
        // Add current user to see their own posts in the feed (optional, but typical)
        followedUsers.add(currentUser);

        return postRepository.findByAuthorInOrderByCreatedAtDesc(followedUsers)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<PostDTO> getExplore() {
        return postRepository.findByAuthor_ShareProgressTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<PostDTO> getUserPosts(UUID userId) {
        User user = userService.getUserById(userId);
        return postRepository.findByAuthorOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private PostDTO mapToDTO(Post post) {
        UserSummaryDTO authorDTO = new UserSummaryDTO(
                post.getAuthor().getId(),
                post.getAuthor().getUsername(),
                post.getAuthor().getProfilePictureUrl()
        );

        return new PostDTO(
                post.getId(),
                authorDTO,
                post.getImageUrl(),
                post.getCaption(),
                post.getCreatedAt()
        );
    }
}
