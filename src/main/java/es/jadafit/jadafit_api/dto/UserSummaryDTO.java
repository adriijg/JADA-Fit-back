package es.jadafit.jadafit_api.dto;

import java.util.UUID;

public record UserSummaryDTO(
        UUID id,
        String username,
        String profilePictureUrl
) {
}
