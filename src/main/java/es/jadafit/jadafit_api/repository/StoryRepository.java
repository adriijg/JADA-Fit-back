package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.Story;
import es.jadafit.jadafit_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StoryRepository extends JpaRepository<Story, UUID> {
    
    @Query("SELECT s FROM Story s WHERE s.author IN :authors AND s.expiresAt > :now ORDER BY s.createdAt DESC")
    List<Story> findActiveStoriesByAuthors(@Param("authors") List<User> authors, @Param("now") LocalDateTime now);

    @Query("SELECT s FROM Story s WHERE s.author = :author AND s.expiresAt > :now ORDER BY s.createdAt DESC")
    List<Story> findActiveStoriesByAuthor(@Param("author") User author, @Param("now") LocalDateTime now);
}
