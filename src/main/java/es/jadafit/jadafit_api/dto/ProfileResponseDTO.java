package es.jadafit.jadafit_api.dto;

import es.jadafit.jadafit_api.model.Gender;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProfileResponseDTO(
        UUID userId,
        String username,
        String email,
        BigDecimal weight,
        Integer height,
        Integer age,
        Gender gender,
        String goal,
        BigDecimal bodyFat,
        BigDecimal muscleMass,
        LocalDateTime updatedAt
) {
}