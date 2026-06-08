package es.jadafit.jadafit_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChallengeProgressEntryDTO(
        UUID id,
        UserSummaryDTO user,
        LocalDate entryDate,
        BigDecimal weight,
        LocalDateTime createdAt
) {
}
