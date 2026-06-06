package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.PostCreateDTO;
import es.jadafit.jadafit_api.dto.PostDTO;
import es.jadafit.jadafit_api.dto.UserSummaryDTO;
import es.jadafit.jadafit_api.model.Post;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.model.UserFollow;
import es.jadafit.jadafit_api.repository.PostCommentRepository;
import es.jadafit.jadafit_api.repository.PostLikeRepository;
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
    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;

    public PostService(
            PostRepository postRepository,
            UserService userService,
            UserFollowRepository userFollowRepository,
            PostLikeRepository postLikeRepository,
            PostCommentRepository postCommentRepository
    ) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.userFollowRepository = userFollowRepository;
        this.postLikeRepository = postLikeRepository;
        this.postCommentRepository = postCommentRepository;
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
        return mapToDTO(post, authorId);
    }

    @Transactional(readOnly = true)
    public List<PostDTO> getFeed(UUID currentUserId) {
        User currentUser = userService.getUserById(currentUserId);

        List<User> followedUsers = userFollowRepository.findByFollower(currentUser)
                .stream()
                .map(UserFollow::getFollowing)
                .collect(Collectors.toList());

        // Include own posts in the feed
        followedUsers.add(currentUser);

        return postRepository.findByAuthorInOrderByCreatedAtDesc(followedUsers)
                .stream()
                .map(p -> mapToDTO(p, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostDTO> getExplore() {
        return postRepository.findByAuthor_ShareProgressTrueOrderByCreatedAtDesc()
                .stream()
                .map(p -> mapToDTO(p, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostDTO> getUserPosts(UUID userId) {
        User user = userService.getUserById(userId);
        return postRepository.findByAuthorOrderByCreatedAtDesc(user)
                .stream()
                .map(p -> mapToDTO(p, userId))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePost(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("No puedes eliminar un post que no te pertenece");
        }

        postLikeRepository.deleteAll(postLikeRepository.findByPost(post));
        postCommentRepository.deleteAll(postCommentRepository.findByPostOrderByCreatedAtAsc(post));
        postRepository.delete(post);
    }

    private PostDTO mapToDTO(Post post, UUID viewerUserId) {
        UserSummaryDTO authorDTO = new UserSummaryDTO(
                post.getAuthor().getId(),
                post.getAuthor().getUsername(),
                post.getAuthor().getProfilePictureUrl()
        );

        long likesCount = postLikeRepository.countByPost(post);
        long commentsCount = postCommentRepository.countByPost(post);
        boolean likedByMe = false;

        if (viewerUserId != null) {
            try {
                User viewer = userService.getUserById(viewerUserId);
                likedByMe = postLikeRepository.existsByPostAndUser(post, viewer);
            } catch (Exception ignored) {}
        }

        return new PostDTO(
                post.getId(),
                authorDTO,
                post.getImageUrl(),
                post.getCaption(),
                post.getCreatedAt(),
                likesCount,
                commentsCount,
                likedByMe
        );
    }
}
