package es.jadafit.jadafit_api.controller;

import es.jadafit.jadafit_api.dto.LoginDTO;
import es.jadafit.jadafit_api.dto.UserRegistrationDTO;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.security.JwtUtils; // Importa tu utilidad
import es.jadafit.jadafit_api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils; // Inyectamos JwtUtils

    @Autowired
    public UserController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegistrationDTO dto) {
        User user = User.builder()
                .username(dto.username())
                .email(dto.email())
                .password_hash(dto.password())
                .build();

        userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado con éxito");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginDTO loginDto) {
        String username = userService.loginUser(loginDto);
        String token = jwtUtils.generateToken(username);
        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }
}