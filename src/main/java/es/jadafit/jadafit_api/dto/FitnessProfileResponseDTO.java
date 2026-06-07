package es.jadafit.jadafit_api.dto;

import es.jadafit.jadafit_api.model.FitnessGoal;
import es.jadafit.jadafit_api.model.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record FitnessProfileResponseDTO(
        UUID userId,
        String username,
        String email,
        BigDecimal weight,
        Integer height,
        LocalDate dateOfBirth,
        Integer age,
        Gender gender,
        FitnessGoal goal,
        BigDecimal bodyFat,
        BigDecimal muscleMass,
        LocalDateTime updatedAt
) {
}
