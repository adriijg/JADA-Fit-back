package es.jadafit.jadafit_api.dto;

import java.math.BigDecimal;
import java.util.List;

public record WaterDaySummaryDTO(
        BigDecimal totalMl,
        List<WaterLogResponseDTO> logs
) {
}
