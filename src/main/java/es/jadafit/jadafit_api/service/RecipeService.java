package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.*;
import es.jadafit.jadafit_api.exception.NotFoundException;
import es.jadafit.jadafit_api.model.Recipe;
import es.jadafit.jadafit_api.model.RecipeIngredient;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class RecipeService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final UserService userService;
    private final RecipeRepository recipeRepository;

    public RecipeService(UserService userService, RecipeRepository recipeRepository) {
        this.userService = userService;
        this.recipeRepository = recipeRepository;
    }

    @Transactional
    public RecipeResponseDTO createRecipe(UUID userId, RecipeCreateDTO dto) {
        User user = userService.getUserById(userId);

        Recipe recipe = Recipe.builder()
                .user(user)
                .name(dto.name().trim())
                .servings(dto.servings())
                .build();

        for (RecipeIngredientDTO ingredientDTO : dto.ingredients()) {
            RecipeIngredient ingredient = RecipeIngredient.builder()
                    .recipe(recipe)
                    .foodName(ingredientDTO.foodName().trim())
                    .quantityGrams(ingredientDTO.quantityGrams())
                    .caloriesPer100g(ingredientDTO.caloriesPer100g())
                    .proteinPer100g(ingredientDTO.proteinPer100g())
                    .carbsPer100g(ingredientDTO.carbsPer100g())
                    .fatsPer100g(ingredientDTO.fatsPer100g())
                    .build();

            recipe.getIngredients().add(ingredient);
        }

        Recipe savedRecipe = recipeRepository.save(recipe);

        return toResponse(savedRecipe);
    }

    @Transactional(readOnly = true)
    public List<RecipeResponseDTO> getMyRecipes(UUID userId) {
        return recipeRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecipeResponseDTO getRecipeById(UUID userId, UUID recipeId) {
        Recipe recipe = recipeRepository.findByIdAndUserId(recipeId, userId)
                .orElseThrow(() -> new NotFoundException("Receta no encontrada"));

        return toResponse(recipe);
    }

    @Transactional
    public void deleteRecipe(UUID userId, UUID recipeId) {
        Recipe recipe = recipeRepository.findByIdAndUserId(recipeId, userId)
                .orElseThrow(() -> new NotFoundException("Receta no encontrada"));

        recipeRepository.delete(recipe);
    }

    @Transactional
    public RecipeResponseDTO updateRecipe(UUID userId, UUID recipeId, RecipeCreateDTO dto) {
        Recipe recipe = recipeRepository.findByIdAndUserId(recipeId, userId)
                .orElseThrow(() -> new NotFoundException("Receta no encontrada"));

        recipe.setName(dto.name().trim());
        recipe.setServings(dto.servings());

        recipe.getIngredients().clear();

        for (RecipeIngredientDTO ingredientDTO : dto.ingredients()) {
            RecipeIngredient ingredient = RecipeIngredient.builder()
                    .recipe(recipe)
                    .foodName(ingredientDTO.foodName().trim())
                    .quantityGrams(ingredientDTO.quantityGrams())
                    .caloriesPer100g(ingredientDTO.caloriesPer100g())
                    .proteinPer100g(ingredientDTO.proteinPer100g())
                    .carbsPer100g(ingredientDTO.carbsPer100g())
                    .fatsPer100g(ingredientDTO.fatsPer100g())
                    .build();

            recipe.getIngredients().add(ingredient);
        }

        Recipe savedRecipe = recipeRepository.save(recipe);

        return toResponse(savedRecipe);
    }

    private RecipeResponseDTO toResponse(Recipe recipe) {
        List<RecipeIngredientResponseDTO> ingredientDTOs = recipe.getIngredients().stream()
                .map(this::toIngredientResponse)
                .toList();

        BigDecimal totalCalories = sum(ingredientDTOs.stream().map(RecipeIngredientResponseDTO::calories).toList());
        BigDecimal totalProtein = sum(ingredientDTOs.stream().map(RecipeIngredientResponseDTO::protein).toList());
        BigDecimal totalCarbs = sum(ingredientDTOs.stream().map(RecipeIngredientResponseDTO::carbs).toList());
        BigDecimal totalFats = sum(ingredientDTOs.stream().map(RecipeIngredientResponseDTO::fats).toList());

        return new RecipeResponseDTO(
                recipe.getId(),
                recipe.getName(),
                recipe.getServings(),
                totalCalories,
                totalProtein,
                totalCarbs,
                totalFats,
                ingredientDTOs,
                recipe.getCreatedAt()
        );
    }

    private RecipeIngredientResponseDTO toIngredientResponse(RecipeIngredient ingredient) {
        return new RecipeIngredientResponseDTO(
                ingredient.getId(),
                ingredient.getFoodName(),
                ingredient.getQuantityGrams(),
                calculateForQuantity(ingredient.getCaloriesPer100g(), ingredient.getQuantityGrams()),
                calculateForQuantity(ingredient.getProteinPer100g(), ingredient.getQuantityGrams()),
                calculateForQuantity(ingredient.getCarbsPer100g(), ingredient.getQuantityGrams()),
                calculateForQuantity(ingredient.getFatsPer100g(), ingredient.getQuantityGrams())
        );
    }

    private BigDecimal calculateForQuantity(BigDecimal valuePer100g, BigDecimal quantityGrams) {
        return valuePer100g
                .multiply(quantityGrams)
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<BigDecimal> values) {
        return values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
