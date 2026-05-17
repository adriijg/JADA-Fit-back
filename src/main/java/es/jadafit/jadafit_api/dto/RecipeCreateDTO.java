package es.jadafit.jadafit_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RecipeCreateDTO(
        @NotBlank(message = "El nombre de la receta es obligatorio")
        String name,

        Integer servings,

        @NotEmpty(message = "La receta debe tener al menos un ingrediente")
        @Valid
        List<RecipeIngredientDTO> ingredients
) {
}
