package es.jadafit.jadafit_api.dto;

import jakarta.validation.constraints.NotBlank;

public record PostCreateDTO(
        @NotBlank(message = "Image URL cannot be empty")
        String imageUrl,
        String caption
) {
}
