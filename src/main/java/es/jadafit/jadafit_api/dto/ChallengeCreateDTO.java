package es.jadafit.jadafit_api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ChallengeCreateDTO(
    UUID challengedId,
    String exerciseName,
    BigDecimal targetWeightKg
) {}
