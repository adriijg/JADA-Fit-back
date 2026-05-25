package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.Post;
import es.jadafit.jadafit_api.model.PostLike;
import es.jadafit.jadafit_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {

    boolean existsByPostAndUser(Post post, User user);

    Optional<PostLike> findByPostAndUser(Post post, User user);

    long countByPost(Post post);
}
