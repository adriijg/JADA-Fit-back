package es.jadafit.jadafit_api.dto;

import jakarta.validation.constraints.NotBlank;

public record StoryCreateDTO(
        @NotBlank(message = "Image URL cannot be empty")
        String imageUrl
) {
}
