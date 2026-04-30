package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.LoginDTO;
import es.jadafit.jadafit_api.dto.UserRegistrationDTO;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        if (userRepository.findByUsername(dto.username()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya está registrado");
        }

        User user = User.builder()
                .username(dto.username())
                .email(dto.email())
                .passwordHash(passwordEncoder.encode(dto.password()))
                .build();

        return userRepository.save(user);
    }

    public User loginUser(LoginDTO loginDto) {
        User user = userRepository.findByEmail(loginDto.email())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(loginDto.password(), user.getPasswordHash())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        return user;
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}