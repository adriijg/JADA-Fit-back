package es.jadafit.jadafit_api.dto;

import es.jadafit.jadafit_api.model.ChallengeStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChallengeResponseDTO(
    UUID id,
    UserSummaryDTO challenger,
    UserSummaryDTO challenged,
    String exerciseName,
    ChallengeStatus status,
    BigDecimal challengerWeight,
    BigDecimal challengedWeight,
    LocalDateTime createdAt,
    LocalDateTime expiresAt
) {}
