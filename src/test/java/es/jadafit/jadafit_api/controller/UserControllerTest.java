package es.jadafit.jadafit_api.controller;

import es.jadafit.jadafit_api.exception.UnauthorizedException;
import es.jadafit.jadafit_api.security.JwtUtils;
import es.jadafit.jadafit_api.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class UserControllerTest {

    @Test
    void meRejectsAuthenticatedTokenWithNonUuidSubject() {
        UserController controller = new UserController(
                mock(UserService.class),
                mock(JwtUtils.class)
        );

        assertThrows(UnauthorizedException.class, () -> controller.me(
                new UsernamePasswordAuthenticationToken("not-a-uuid", null)
        ));
    }
}
