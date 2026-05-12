package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.Post;
import es.jadafit.jadafit_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    List<Post> findByAuthorInOrderByCreatedAtDesc(List<User> authors);
    List<Post> findByAuthor_ShareProgressTrueOrderByCreatedAtDesc();
    List<Post> findByAuthorOrderByCreatedAtDesc(User author);
}
