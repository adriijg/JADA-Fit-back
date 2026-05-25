package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.PostCommentCreateDTO;
import es.jadafit.jadafit_api.dto.PostCommentDTO;
import es.jadafit.jadafit_api.dto.UserSummaryDTO;
import es.jadafit.jadafit_api.exception.NotFoundException;
import es.jadafit.jadafit_api.model.Post;
import es.jadafit.jadafit_api.model.PostComment;
import es.jadafit.jadafit_api.model.PostLike;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.repository.PostCommentRepository;
import es.jadafit.jadafit_api.repository.PostLikeRepository;
import es.jadafit.jadafit_api.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostInteractionService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;
    private final UserService userService;

    public PostInteractionService(
            PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            PostCommentRepository postCommentRepository,
            UserService userService
    ) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.postCommentRepository = postCommentRepository;
        this.userService = userService;
    }

    // ── Likes ──────────────────────────────────────────────────────────────────

    @Transactional
    public void likePost(UUID postId, UUID userId) {
        Post post = getPostOrThrow(postId);
        User user = userService.getUserById(userId);

        if (!postLikeRepository.existsByPostAndUser(post, user)) {
            PostLike like = PostLike.builder().post(post).user(user).build();
            postLikeRepository.save(like);
        }
    }

    @Transactional
    public void unlikePost(UUID postId, UUID userId) {
        Post post = getPostOrThrow(postId);
        User user = userService.getUserById(userId);

        postLikeRepository.findByPostAndUser(post, user)
                .ifPresent(postLikeRepository::delete);
    }

    @Transactional(readOnly = true)
    public long getLikesCount(UUID postId) {
        Post post = getPostOrThrow(postId);
        return postLikeRepository.countByPost(post);
    }

    @Transactional(readOnly = true)
    public boolean isLikedByUser(UUID postId, UUID userId) {
        Post post = getPostOrThrow(postId);
        User user = userService.getUserById(userId);
        return postLikeRepository.existsByPostAndUser(post, user);
    }

    // ── Comments ───────────────────────────────────────────────────────────────

    @Transactional
    public PostCommentDTO addComment(UUID postId, UUID authorId, PostCommentCreateDTO createDTO) {
        Post post = getPostOrThrow(postId);
        User author = userService.getUserById(authorId);

        PostComment comment = PostComment.builder()
                .post(post)
                .author(author)
                .content(createDTO.content())
                .build();

        comment = postCommentRepository.save(comment);
        return mapCommentToDTO(comment);
    }

    @Transactional(readOnly = true)
    public List<PostCommentDTO> getComments(UUID postId) {
        Post post = getPostOrThrow(postId);
        return postCommentRepository.findByPostOrderByCreatedAtAsc(post)
                .stream()
                .map(this::mapCommentToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getCommentsCount(UUID postId) {
        Post post = getPostOrThrow(postId);
        return postCommentRepository.countByPost(post);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Post getPostOrThrow(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post no encontrado"));
    }

    private PostCommentDTO mapCommentToDTO(PostComment comment) {
        UserSummaryDTO authorDTO = new UserSummaryDTO(
                comment.getAuthor().getId(),
                comment.getAuthor().getUsername(),
                comment.getAuthor().getProfilePictureUrl()
        );
        return new PostCommentDTO(
                comment.getId(),
                authorDTO,
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
