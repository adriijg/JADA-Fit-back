package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.LoginDTO;
import es.jadafit.jadafit_api.dto.UserRegistrationDTO;
import es.jadafit.jadafit_api.exception.ConflictException;
import es.jadafit.jadafit_api.exception.UnauthorizedException;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.service.EmailService;
import es.jadafit.jadafit_api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final EmailService emailService = mock(EmailService.class);
    private final UserService userService = new UserService(userRepository, passwordEncoder, emailService);

    @Test
    void registerUserNormalizesEmailAndUsername() {
        when(passwordEncoder.encode("password123")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.registerUser(new UserRegistrationDTO(
                " TestUser ",
                " Test@Email.com ",
                "password123"
        ));

        assertEquals("testuser", user.getUsername());
        assertEquals("test@email.com", user.getEmail());
    }

    @Test
    void registerUserThrowsConflictWhenEmailExistsIgnoringCase() {
        when(userRepository.findByEmailIgnoreCase("test@email.com"))
                .thenReturn(Optional.of(new User()));

        assertThrows(ConflictException.class, () -> userService.registerUser(new UserRegistrationDTO(
                "testuser",
                "Test@Email.com",
                "password123"
        )));
    }

    @Test
    void registerUserConvertsDatabaseDuplicateIntoConflict() {
        when(passwordEncoder.encode("password123")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(ConflictException.class, () -> userService.registerUser(new UserRegistrationDTO(
                "testuser",
                "test@email.com",
                "password123"
        )));
    }

    @Test
    void loginUserUsesGenericUnauthorizedForMissingUser() {
        assertThrows(UnauthorizedException.class, () -> userService.loginUser(new LoginDTO(
                "missing@email.com",
                "password123"
        )));

        verify(userRepository).findByEmailIgnoreCase("missing@email.com");
    }

    @Test
    void loginUserCanAuthenticateWithEmail() {
        User user = User.builder()
                .email("test@email.com")
                .username("testuser")
                .passwordHash("hash")
                .build();

        when(userRepository.findByEmailIgnoreCase("test@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);

        User authenticatedUser = userService.loginUser(new LoginDTO(
                " Test@Email.com ",
                "password123"
        ));

        assertEquals(user, authenticatedUser);
    }

    @Test
    void loginUserCanAuthenticateWithUsername() {
        User user = User.builder()
                .email("test@email.com")
                .username("testuser")
                .passwordHash("hash")
                .build();

        when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);

        User authenticatedUser = userService.loginUser(new LoginDTO(
                " TestUser ",
                "password123"
        ));

        assertEquals(user, authenticatedUser);
    }
}
