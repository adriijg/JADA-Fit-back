package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.ForgotPasswordRequestDTO;
import es.jadafit.jadafit_api.dto.LoginDTO;
import es.jadafit.jadafit_api.dto.ResetPasswordRequestDTO;
import es.jadafit.jadafit_api.dto.UserRegistrationDTO;
import es.jadafit.jadafit_api.exception.BadRequestException;
import es.jadafit.jadafit_api.exception.ConflictException;
import es.jadafit.jadafit_api.exception.NotFoundException;
import es.jadafit.jadafit_api.exception.UnauthorizedException;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.model.UserSession;
import es.jadafit.jadafit_api.repository.UserRepository;
import es.jadafit.jadafit_api.repository.UserSessionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private static final int MAX_SESSIONS_PER_USER = 10;

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(
            UserRepository userRepository,
            UserSessionRepository sessionRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
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

    public String createSession(UUID userId) {
        return createSession(userId, null);
    }

    public String createSession(UUID userId, String deviceInfo) {
        getUserById(userId);

        long activeSessions = sessionRepository.countByUserIdAndIsActiveTrue(userId);
        if (activeSessions >= MAX_SESSIONS_PER_USER) {
            List<UserSession> oldest = sessionRepository.findByUserIdAndIsActiveTrue(userId);
            oldest.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
            oldest.get(0).setIsActive(false);
            sessionRepository.save(oldest.get(0));
        }

        String sessionId = UUID.randomUUID().toString();
        UserSession session = UserSession.builder()
                .userId(userId)
                .sessionId(sessionId)
                .deviceInfo(deviceInfo)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .build();
        sessionRepository.save(session);
        return sessionId;
    }

    public boolean isValidSession(String userId, String sessionId) {
        if (sessionId == null) return false;
        Optional<UserSession> session = sessionRepository.findBySessionIdAndIsActiveTrue(sessionId);
        if (session.isEmpty()) return false;
        if (!session.get().getUserId().toString().equals(userId)) return false;
        if (session.get().getExpiresAt().isBefore(LocalDateTime.now())) {
            session.get().setIsActive(false);
            sessionRepository.save(session.get());
            return false;
        }
        return true;
    }

    public void revokeSession(String sessionId) {
        sessionRepository.findBySessionIdAndIsActiveTrue(sessionId).ifPresent(session -> {
            session.setIsActive(false);
            sessionRepository.save(session);
        });
    }

    public void revokeAllSessions(UUID userId) {
        List<UserSession> active = sessionRepository.findByUserIdAndIsActiveTrue(userId);
        for (UserSession s : active) {
            s.setIsActive(false);
        }
        sessionRepository.saveAll(active);
    }

    public User updateProfile(UUID userId, es.jadafit.jadafit_api.dto.ProfileUpdateDTO dto) {
        User user = getUserById(userId);
        if (dto.bio() != null) {
            user.setBio(dto.bio());
        }
        if (dto.profilePictureUrl() != null) {
            user.setProfilePictureUrl(dto.profilePictureUrl());
        }
        return userRepository.save(user);
    }

    public void forgotPassword(ForgotPasswordRequestDTO dto) {
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(normalize(dto.email()));
        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        emailService.sendPasswordResetToken(user.getEmail(), token);
    }

    public void resetPassword(ResetPasswordRequestDTO dto) {
        User user = userRepository.findByPasswordResetToken(dto.token())
                .orElseThrow(() -> new BadRequestException("Token invalido"));

        if (user.getPasswordResetTokenExpiry() == null
                || user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El token ha expirado");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.newPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
        revokeAllSessions(user.getId());
    }
}
