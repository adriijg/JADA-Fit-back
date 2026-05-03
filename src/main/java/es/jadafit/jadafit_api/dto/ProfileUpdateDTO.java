package es.jadafit.jadafit_api.dto;

import es.jadafit.jadafit_api.model.Gender;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProfileUpdateDTO(
        @DecimalMin(value = "1.0", message = "El peso debe ser mayor que 0")
        BigDecimal weight,

        @Min(value = 50, message = "La altura mínima es 50 cm")
        @Max(value = 250, message = "La altura máxima es 250 cm")
        Integer height,

        @Min(value = 10, message = "La edad mínima es 10 años")
        @Max(value = 120, message = "La edad máxima es 120 años")
        Integer age,

        Gender gender,

        @Size(max = 50, message = "El objetivo no puede superar los 50 caracteres")
        String goal,

        @DecimalMin(value = "0.0", message = "El porcentaje de grasa no puede ser negativo")
        BigDecimal bodyFat,

        @DecimalMin(value = "0.0", message = "La masa muscular no puede ser negativa")
        BigDecimal muscleMass
) {
}