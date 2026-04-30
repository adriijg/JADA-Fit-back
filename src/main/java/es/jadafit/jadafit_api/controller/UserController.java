package es.jadafit.jadafit_api.controller;

import es.jadafit.jadafit_api.dto.AuthResponseDTO;
import es.jadafit.jadafit_api.dto.LoginDTO;
import es.jadafit.jadafit_api.dto.UserRegistrationDTO;
import es.jadafit.jadafit_api.dto.UserResponseDTO;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.security.JwtUtils;
import es.jadafit.jadafit_api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    public UserController(
            UserService userService,
            JwtUtils jwtUtils
    ) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(
            @Valid @RequestBody UserRegistrationDTO dto
    ) {
        User user = userService.registerUser(dto);

        UserResponseDTO response = new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginDTO loginDto
    ) {
        User user = userService.loginUser(loginDto);

        String token = jwtUtils.generateToken(user.getId().toString());

        AuthResponseDTO response = new AuthResponseDTO(
                token,
                user.getUsername(),
                user.getEmail()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());

        User user = userService.getUserById(userId);

        UserResponseDTO response = new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }
}