package es.jadafit.jadafit_api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostDTO(
        UUID id,
        UserSummaryDTO author,
        String imageUrl,
        String caption,
        LocalDateTime createdAt
) {
}
