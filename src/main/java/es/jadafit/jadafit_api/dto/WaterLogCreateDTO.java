package es.jadafit.jadafit_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WaterLogCreateDTO(
        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "1", message = "La cantidad debe ser mayor que 0")
        BigDecimal amountMl,

        LocalDateTime loggedAt
) {
}
