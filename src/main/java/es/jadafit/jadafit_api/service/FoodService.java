package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.FoodResponseDTO;
import es.jadafit.jadafit_api.dto.OpenFoodFactsResponseDTO;
import es.jadafit.jadafit_api.exception.NotFoundException;
import es.jadafit.jadafit_api.model.CatalogFood;
import es.jadafit.jadafit_api.repository.CatalogFoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Service
public class FoodService {

    private static final String OPEN_FOOD_FACTS_URL =
            "https://world.openfoodfacts.org/api/v2/product/{barcode}.json" +
                    "?fields=code,product_name,product_name_es,product_name_en,nutriments";

    private final CatalogFoodRepository catalogFoodRepository;
    private final RestClient restClient;

    public FoodService(CatalogFoodRepository catalogFoodRepository) {
        this.catalogFoodRepository = catalogFoodRepository;
        this.restClient = RestClient.builder()
                .defaultHeader("User-Agent", "JadaFit/1.0 (contact: dev@jadafit.local)")
                .build();
    }

    public FoodResponseDTO getFoodByBarcode(String barcode) {
        return catalogFoodRepository.findByExternalId(barcode)
                .map(this::toResponse)
                .orElseGet(() -> fetchFromOpenFoodFactsAndSave(barcode));
    }

    private FoodResponseDTO fetchFromOpenFoodFactsAndSave(String barcode) {
        OpenFoodFactsResponseDTO response;

        try {
            response = restClient.get()
                    .uri(OPEN_FOOD_FACTS_URL, barcode)
                    .retrieve()
                    .body(OpenFoodFactsResponseDTO.class);
        } catch (Exception ex) {
            throw new NotFoundException("No se pudo consultar Open Food Facts");
        }

        if (response == null || response.status() == null || response.status() != 1 || response.product() == null) {
            throw new NotFoundException("No se encontró ningún alimento con ese código");
        }

        var product = response.product();
        var nutriments = product.nutriments();

        String name = getBestProductName(product);

        if (name == null || name.isBlank()) {
            throw new NotFoundException("El alimento existe, pero no tiene nombre disponible");
        }

        BigDecimal calories = BigDecimal.ZERO;
        BigDecimal proteins = BigDecimal.ZERO;
        BigDecimal carbs = BigDecimal.ZERO;
        BigDecimal fats = BigDecimal.ZERO;

        if (nutriments != null) {
            calories = firstNonNull(nutriments.energyKcal100g(), nutriments.energyKcalValue(), BigDecimal.ZERO);
            proteins = firstNonNull(nutriments.proteins100g(), BigDecimal.ZERO);
            carbs = firstNonNull(nutriments.carbohydrates100g(), BigDecimal.ZERO);
            fats = firstNonNull(nutriments.fat100g(), BigDecimal.ZERO);
        }

        CatalogFood food = CatalogFood.builder()
                .externalId(barcode)
                .name(name)
                .caloriesPer100g(calories)
                .proteinPer100g(proteins)
                .carbsPer100g(carbs)
                .fatsPer100g(fats)
                .isCustom(false)
                .build();

        CatalogFood savedFood = catalogFoodRepository.save(food);

        return toResponse(savedFood);
    }

    private String getBestProductName(OpenFoodFactsResponseDTO.OpenFoodFactsProductDTO product) {
        if (product.productNameEs() != null && !product.productNameEs().isBlank()) {
            return product.productNameEs();
        }

        if (product.productName() != null && !product.productName().isBlank()) {
            return product.productName();
        }

        if (product.productNameEn() != null && !product.productNameEn().isBlank()) {
            return product.productNameEn();
        }

        return null;
    }

    private BigDecimal firstNonNull(BigDecimal... values) {
        for (BigDecimal value : values) {
            if (value != null) {
                return value;
            }
        }

        return BigDecimal.ZERO;
    }

    private FoodResponseDTO toResponse(CatalogFood food) {
        return new FoodResponseDTO(
                food.getId(),
                food.getExternalId(),
                food.getName(),
                food.getCaloriesPer100g(),
                food.getProteinPer100g(),
                food.getCarbsPer100g(),
                food.getFatsPer100g(),
                food.getIsCustom()
        );
    }
}