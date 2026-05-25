package es.jadafit.jadafit_api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostCommentDTO(
        UUID id,
        UserSummaryDTO author,
        String content,
        LocalDateTime createdAt
) {
}
