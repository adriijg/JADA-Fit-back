package es.jadafit.jadafit_api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
        @JsonAlias({"email", "username"})
        @NotBlank(message = "El email o nombre de usuario es obligatorio")
        String identifier,

        @NotBlank(message = "La contrasena es obligatoria")
        String password
) {}
