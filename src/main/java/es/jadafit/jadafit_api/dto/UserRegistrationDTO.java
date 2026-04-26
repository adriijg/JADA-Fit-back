package es.jadafit.jadafit_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationDTO(
        @NotBlank(message = "El nombre es obligatorio")
        String username,

        @Email(message = "Email no válido")
        String email,

        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password
) {
}
