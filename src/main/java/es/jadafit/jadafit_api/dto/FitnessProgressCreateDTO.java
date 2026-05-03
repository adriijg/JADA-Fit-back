package es.jadafit.jadafit_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FitnessProgressCreateDTO(
        @NotNull(message = "El peso es obligatorio")
        @DecimalMin(value = "1.0", message = "El peso debe ser mayor que 0")
        BigDecimal weight,

        @DecimalMin(value = "0.0", message = "El porcentaje de grasa no puede ser negativo")
        BigDecimal bodyFat,

        @DecimalMin(value = "0.0", message = "La masa muscular no puede ser negativa")
        BigDecimal muscleMass,

        @PastOrPresent(message = "La fecha del registro no puede estar en el futuro")
        LocalDateTime loggedAt
) {
}