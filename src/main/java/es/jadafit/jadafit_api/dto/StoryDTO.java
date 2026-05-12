package es.jadafit.jadafit_api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record StoryDTO(
        UUID id,
        UserSummaryDTO author,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {
}
