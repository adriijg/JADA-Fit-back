package es.jadafit.jadafit_api.controller;

import es.jadafit.jadafit_api.dto.AuthResponseDTO;
import es.jadafit.jadafit_api.dto.ForgotPasswordRequestDTO;
import es.jadafit.jadafit_api.dto.LoginDTO;
import es.jadafit.jadafit_api.dto.UserRegistrationDTO;
import es.jadafit.jadafit_api.dto.UserResponseDTO;
import es.jadafit.jadafit_api.dto.ResetPasswordRequestDTO;
import es.jadafit.jadafit_api.dto.GenericMessageDTO;
import es.jadafit.jadafit_api.exception.UnauthorizedException;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.security.JwtUtils;
import es.jadafit.jadafit_api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
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
    public ResponseEntity<AuthResponseDTO> register(
            @Valid @RequestBody UserRegistrationDTO dto
    ) {
        User user = userService.registerUser(dto);
        String sessionId = userService.createSession(user.getId());

        String token = jwtUtils.generateToken(user.getId().toString(), sessionId);

        AuthResponseDTO response = new AuthResponseDTO(
                token,
                user.getUsername(),
                user.getEmail(),
                user.getOnboardingCompleted()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginDTO loginDto
    ) {
        User user = userService.loginUser(loginDto);
        String sessionId = userService.createSession(user.getId());

        String token = jwtUtils.generateToken(user.getId().toString(), sessionId);

        AuthResponseDTO response = new AuthResponseDTO(
                token,
                user.getUsername(),
                user.getEmail(),
                user.getOnboardingCompleted()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<GenericMessageDTO> logout(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok(new GenericMessageDTO("Sesion cerrada"));
        }
        UUID userId = getUserIdFromAuthentication(authentication);
        userService.revokeAllSessions(userId);
        return ResponseEntity.ok(new GenericMessageDTO("Sesion cerrada correctamente"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me(Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);

        User user = userService.getUserById(userId);

        UserResponseDTO response = new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getProfilePictureUrl(),
                user.getOnboardingCompleted(),
                user.getCreatedAt(),
                user.getShareProgress()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me/privacy")
    public ResponseEntity<UserResponseDTO> updatePrivacy(
            Authentication authentication,
            @Valid @RequestBody es.jadafit.jadafit_api.dto.PrivacySettingsUpdateDTO dto
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);
        User user = userService.updatePrivacySettings(userId, dto.shareProgress());
        
        UserResponseDTO response = new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getProfilePictureUrl(),
                user.getOnboardingCompleted(),
                user.getCreatedAt(),
                user.getShareProgress()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me/profile")
    public ResponseEntity<UserResponseDTO> updateProfile(
            Authentication authentication,
            @Valid @RequestBody es.jadafit.jadafit_api.dto.ProfileUpdateDTO dto
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);
        User user = userService.updateProfile(userId, dto);
        
        UserResponseDTO response = new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getProfilePictureUrl(),
                user.getOnboardingCompleted(),
                user.getCreatedAt(),
                user.getShareProgress()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<GenericMessageDTO> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDTO dto
    ) {
        userService.forgotPassword(dto);
        return ResponseEntity.ok(new GenericMessageDTO("Si el email existe, recibiras un correo con las instrucciones"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<GenericMessageDTO> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO dto
    ) {
        userService.resetPassword(dto);
        return ResponseEntity.ok(new GenericMessageDTO("Contrasena actualizada correctamente"));
    }

    private UUID getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("No autorizado");
        }

        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException("Token invalido");
        }
    }
}