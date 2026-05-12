package es.jadafit.jadafit_api.dto;

import java.util.UUID;

public record UserProfileDTO(
        UUID id,
        String username,
        String bio,
        String profilePictureUrl,
        long followersCount,
        long followingCount,
        boolean isFollowing,
        boolean shareProgress
) {
}
