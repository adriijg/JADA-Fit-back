package es.jadafit.jadafit_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProfileProgressResponseDTO(
        UUID id,
        BigDecimal weight,
        BigDecimal bodyFat,
        BigDecimal muscleMass,
        LocalDateTime loggedAt
) {
}