package es.jadafit.jadafit_api.service;

import es.jadafit.jadafit_api.dto.CatalogFoodResponseDTO;
import es.jadafit.jadafit_api.dto.CustomFoodCreateDTO;
import es.jadafit.jadafit_api.dto.CustomFoodUpdateDTO;
import es.jadafit.jadafit_api.exception.NotFoundException;
import es.jadafit.jadafit_api.model.CatalogFood;
import es.jadafit.jadafit_api.model.FoodSource;
import es.jadafit.jadafit_api.model.User;
import es.jadafit.jadafit_api.repository.CatalogFoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CatalogFoodService {

    private final UserService userService;
    private final CatalogFoodRepository catalogFoodRepository;
    private final OpenFoodFactsClient openFoodFactsClient;

    public CatalogFoodService(
            UserService userService,
            CatalogFoodRepository catalogFoodRepository,
            OpenFoodFactsClient openFoodFactsClient
    ) {
        this.userService = userService;
        this.catalogFoodRepository = catalogFoodRepository;
        this.openFoodFactsClient = openFoodFactsClient;
    }

    @Transactional
    public List<CatalogFoodResponseDTO> searchFoods(UUID userId, String query) {
        String normalizedQuery = normalizeText(query);

        if (normalizedQuery == null || normalizedQuery.length() < 2) {
            return List.of();
        }

        List<CatalogFoodResponseDTO> localResultsBeforeExternalSearch =
                catalogFoodRepository
                        .searchAvailableFoods(normalizedQuery, userId)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        List<CatalogFoodResponseDTO> cachedExternalResults =
                openFoodFactsClient
                        .searchByName(normalizedQuery)
                        .stream()
                        .map(this::cacheOpenFoodFactsProduct)
                        .map(this::toResponse)
                        .toList();

        List<CatalogFoodResponseDTO> localResultsAfterExternalSearch =
                catalogFoodRepository
                        .searchAvailableFoods(normalizedQuery, userId)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return mergeWithoutDuplicates(
                localResultsBeforeExternalSearch,
                cachedExternalResults,
                localResultsAfterExternalSearch
        );
    }

    @Transactional
    public CatalogFoodResponseDTO findByBarcode(UUID userId, String barcode) {
        String normalizedBarcode = normalizeText(barcode);

        if (normalizedBarcode == null) {
            throw new NotFoundException("Código de barras no válido");
        }

        return catalogFoodRepository
                .findFirstByBarcodeAndSource(normalizedBarcode, FoodSource.OPEN_FOOD_FACTS)
                .map(this::toResponse)
                .orElseGet(() -> fetchAndCacheByBarcode(normalizedBarcode));
    }

    @Transactional
    public CatalogFoodResponseDTO createCustomFood(UUID userId, CustomFoodCreateDTO dto) {
        User user = userService.getUserById(userId);

        CatalogFood food = CatalogFood.builder()
                .ownerUser(user)
                .externalFoodId(null)
                .barcode(normalizeText(dto.barcode()))
                .name(normalizeText(dto.name()))
                .brand(normalizeText(dto.brand()))
                .source(FoodSource.USER)
                .caloriesPer100g(dto.caloriesPer100g())
                .proteinPer100g(dto.proteinPer100g())
                .carbsPer100g(dto.carbsPer100g())
                .fatsPer100g(dto.fatsPer100g())
                .build();

        CatalogFood savedFood = catalogFoodRepository.save(food);

        return toResponse(savedFood);
    }

    @Transactional(readOnly = true)
    public List<CatalogFoodResponseDTO> getMyCustomFoods(UUID userId) {
        return catalogFoodRepository
                .findByOwnerUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CatalogFoodResponseDTO updateCustomFood(
            UUID userId,
            UUID foodId,
            CustomFoodUpdateDTO dto
    ) {
        CatalogFood food = catalogFoodRepository.findByIdAndOwnerUserId(foodId, userId)
                .orElseThrow(() -> new NotFoundException("Alimento personalizado no encontrado"));

        food.setName(normalizeText(dto.name()));
        food.setBrand(normalizeText(dto.brand()));
        food.setBarcode(normalizeText(dto.barcode()));
        food.setCaloriesPer100g(dto.caloriesPer100g());
        food.setProteinPer100g(dto.proteinPer100g());
        food.setCarbsPer100g(dto.carbsPer100g());
        food.setFatsPer100g(dto.fatsPer100g());

        CatalogFood savedFood = catalogFoodRepository.save(food);

        return toResponse(savedFood);
    }

    @Transactional
    public void deleteCustomFood(UUID userId, UUID foodId) {
        CatalogFood food = catalogFoodRepository.findByIdAndOwnerUserId(foodId, userId)
                .orElseThrow(() -> new NotFoundException("Alimento personalizado no encontrado"));

        catalogFoodRepository.delete(food);
    }

    private CatalogFoodResponseDTO fetchAndCacheByBarcode(String barcode) {
        OpenFoodFactsClient.OpenFoodFactsProduct product =
                openFoodFactsClient.findByBarcode(barcode);

        if (product == null) {
            throw new NotFoundException("Alimento no encontrado");
        }

        CatalogFood savedFood = cacheOpenFoodFactsProduct(product);

        return toResponse(savedFood);
    }

    private CatalogFood cacheOpenFoodFactsProduct(
            OpenFoodFactsClient.OpenFoodFactsProduct product
    ) {
        String externalFoodId = normalizeText(product.externalFoodId());
        String barcode = normalizeText(product.barcode());

        if (barcode != null) {
            var existingByBarcode = catalogFoodRepository.findFirstByBarcodeAndSource(
                    barcode,
                    FoodSource.OPEN_FOOD_FACTS
            );

            if (existingByBarcode.isPresent()) {
                return existingByBarcode.get();
            }
        }

        if (externalFoodId != null) {
            var existingByExternalId = catalogFoodRepository.findFirstByExternalFoodIdAndSource(
                    externalFoodId,
                    FoodSource.OPEN_FOOD_FACTS
            );

            if (existingByExternalId.isPresent()) {
                return existingByExternalId.get();
            }
        }

        CatalogFood food = CatalogFood.builder()
                .ownerUser(null)
                .externalFoodId(externalFoodId)
                .barcode(barcode)
                .name(normalizeText(product.name()))
                .brand(normalizeText(product.brand()))
                .source(FoodSource.OPEN_FOOD_FACTS)
                .caloriesPer100g(product.caloriesPer100g())
                .proteinPer100g(product.proteinPer100g())
                .carbsPer100g(product.carbsPer100g())
                .fatsPer100g(product.fatsPer100g())
                .build();

        return catalogFoodRepository.save(food);
    }

    @SafeVarargs
    private final List<CatalogFoodResponseDTO> mergeWithoutDuplicates(
            List<CatalogFoodResponseDTO>... resultGroups
    ) {
        Map<String, CatalogFoodResponseDTO> merged = new LinkedHashMap<>();

        for (List<CatalogFoodResponseDTO> resultGroup : resultGroups) {
            for (CatalogFoodResponseDTO food : resultGroup) {
                merged.putIfAbsent(uniqueKey(food), food);
            }
        }

        return merged.values()
                .stream()
                .limit(30)
                .toList();
    }

    private String uniqueKey(CatalogFoodResponseDTO food) {
        if (food.barcode() != null && !food.barcode().isBlank()) {
            return "barcode:" + food.barcode();
        }

        if (food.externalFoodId() != null && !food.externalFoodId().isBlank()) {
            return "external:" + food.externalFoodId();
        }

        String brand = food.brand() == null ? "" : food.brand().toLowerCase();
        String name = food.name() == null ? "" : food.name().toLowerCase();

        return "name:" + brand + ":" + name;
    }

    private CatalogFoodResponseDTO toResponse(CatalogFood food) {
        UUID ownerUserId = food.getOwnerUser() != null
                ? food.getOwnerUser().getId()
                : null;

        return new CatalogFoodResponseDTO(
                food.getId(),
                ownerUserId,
                food.getExternalFoodId(),
                food.getBarcode(),
                food.getName(),
                food.getBrand(),
                food.getSource(),
                food.getCaloriesPer100g(),
                food.getProteinPer100g(),
                food.getCarbsPer100g(),
                food.getFatsPer100g()
        );
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }
}