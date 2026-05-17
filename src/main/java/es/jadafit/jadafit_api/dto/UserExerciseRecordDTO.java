package es.jadafit.jadafit_api.dto;

import java.math.BigDecimal;

public record UserExerciseRecordDTO(
    String exerciseName,
    BigDecimal maxWeight
) {}
