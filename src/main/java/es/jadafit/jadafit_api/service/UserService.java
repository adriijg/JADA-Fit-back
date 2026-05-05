package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.LoginDTO;
import es.jadafit.jadafit_api.dto.UserRegistrationDTO;
import es.jadafit.jadafit_api.exception.ConflictException;
import es.jadafit.jadafit_api.exception.NotFoundException;
import es.jadafit.jadafit_api.exception.UnauthorizedException;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(UserRegistrationDTO dto) {
        String email = normalize(dto.email());
        String username = normalize(dto.username());

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ConflictException("El email ya esta registrado");
        }

        if (userRepository.findByUsernameIgnoreCase(username).isPresent()) {
            throw new ConflictException("El nombre de usuario ya esta registrado");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(dto.password()))
                .build();

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("El email o nombre de usuario ya esta registrado");
        }
    }

    public User loginUser(LoginDTO loginDto) {
        User user = findByLoginIdentifier(loginDto.identifier())
                .orElseThrow(() -> new UnauthorizedException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(loginDto.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Credenciales incorrectas");
        }

        return user;
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(normalize(email))
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private Optional<User> findByLoginIdentifier(String identifier) {
        String normalizedIdentifier = normalize(identifier);

        if (normalizedIdentifier.contains("@")) {
            return userRepository.findByEmailIgnoreCase(normalizedIdentifier);
        }

        return userRepository.findByUsernameIgnoreCase(normalizedIdentifier);
    }

    public User updatePrivacySettings(UUID userId, Boolean shareProgress) {
        User user = getUserById(userId);
        user.setShareProgress(shareProgress);
        return userRepository.save(user);
    }
}
