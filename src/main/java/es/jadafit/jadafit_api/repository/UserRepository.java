package es.jadafit.jadafit_api.repository;

import es.jadafit.jadafit_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByUsernameIgnoreCase(String username);
    List<User> findByUsernameContainingIgnoreCase(String username);
}
