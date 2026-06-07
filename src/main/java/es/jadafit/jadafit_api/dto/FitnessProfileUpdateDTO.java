package es.jadafit.jadafit_api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.jadafit.jadafit_api.model.FitnessGoal;
import es.jadafit.jadafit_api.model.Gender;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FitnessProfileUpdateDTO(
        @DecimalMin(value = "1.0", message = "El peso debe ser mayor que 0")
        BigDecimal weight,

        @Min(value = 50, message = "La altura mínima es 50 cm")
        @Max(value = 250, message = "La altura máxima es 250 cm")
        Integer height,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dateOfBirth,

        Gender gender,

        FitnessGoal goal,

        @DecimalMin(value = "0.0", message = "El porcentaje de grasa no puede ser negativo")
        BigDecimal bodyFat,

        @DecimalMin(value = "0.0", message = "La masa muscular no puede ser negativa")
        BigDecimal muscleMass
) {
}
