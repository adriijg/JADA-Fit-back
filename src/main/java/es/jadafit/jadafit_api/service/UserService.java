package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.LoginDTO;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        String hashedPwd = passwordEncoder.encode(user.getPassword_hash());
        user.setPassword_hash(hashedPwd);

        return userRepository.save(user);
    }

    public String loginUser(LoginDTO loginDto) {
        User user = userRepository.findByEmail(loginDto.email())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(loginDto.password(), user.getPassword_hash())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        return user.getUsername();
    }
}