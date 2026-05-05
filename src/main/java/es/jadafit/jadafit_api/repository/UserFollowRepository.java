package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.model.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {
    
    boolean existsByFollowerAndFollowing(User follower, User following);
    
    void deleteByFollowerAndFollowing(User follower, User following);
    
    List<UserFollow> findByFollower(User follower);
    
    List<UserFollow> findByFollowing(User following);
    
    long countByFollower(User follower);
    
    long countByFollowing(User following);

    Optional<UserFollow> findByFollowerAndFollowing(User follower, User following);
}
