package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.Post;
import es.jadafit.jadafit_api.model.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {

    List<PostComment> findByPostOrderByCreatedAtAsc(Post post);

    long countByPost(Post post);
}
