package es.jadafit.jadafit_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ChallengeProgressCreateDTO(
        BigDecimal weight,
        LocalDate entryDate
) {
}
