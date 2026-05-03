package es.jadafit.jadafit_api.service;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class OpenFoodFactsClient {

    private static final String PRODUCT_FIELDS =
            "code,product_name,product_name_es,product_name_en,generic_name,brands,nutriments";

    private final RestClient restClient;

    public OpenFoodFactsClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://world.openfoodfacts.org")
                .defaultHeader(
                        "User-Agent",
                        "JadaFit/1.0 (contact: dev@jadafit.local)"
                )
                .build();
    }

    public OpenFoodFactsProduct findByBarcode(String barcode) {
        try {
            Map<?, ?> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v2/product/{barcode}.json")
                            .queryParam("fields", PRODUCT_FIELDS)
                            .build(barcode))
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                return null;
            }

            Object status = response.get("status");

            if (status instanceof Number number && number.intValue() == 0) {
                return null;
            }

            Object productObject = response.get("product");

            if (!(productObject instanceof Map<?, ?> product)) {
                return null;
            }

            return mapProduct(product);
        } catch (RestClientException ex) {
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    public List<OpenFoodFactsProduct> searchByName(String query) {
        String normalizedQuery = normalizeText(query);

        if (normalizedQuery == null || normalizedQuery.length() < 2) {
            return List.of();
        }

        try {
            Map<?, ?> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/cgi/search.pl")
                            .queryParam("action", "process")
                            .queryParam("search_terms", normalizedQuery)
                            .queryParam("json", 1)
                            .queryParam("page_size", 20)
                            .queryParam("fields", PRODUCT_FIELDS)
                            .build())
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                return List.of();
            }

            Object productsObject = response.get("products");

            if (!(productsObject instanceof List<?> products)) {
                return List.of();
            }

            List<OpenFoodFactsProduct> result = new ArrayList<>();

            for (Object item : products) {
                if (item instanceof Map<?, ?> product) {
                    OpenFoodFactsProduct mappedProduct = mapProduct(product);

                    if (mappedProduct != null && matchesQuery(mappedProduct, normalizedQuery)) {
                        result.add(mappedProduct);
                    }
                }
            }

            return result;
        } catch (RestClientException ex) {
            return List.of();
        } catch (Exception ex) {
            return List.of();
        }
    }

    private OpenFoodFactsProduct mapProduct(Map<?, ?> product) {
        String code = getString(product, "code");

        String name = firstString(
                product,
                "product_name",
                "product_name_es",
                "product_name_en",
                "generic_name"
        );

        String brand = getString(product, "brands");

        if (code == null || code.isBlank()) {
            return null;
        }

        if (name == null || name.isBlank()) {
            return null;
        }

        Object nutrimentsObject = product.get("nutriments");

        if (!(nutrimentsObject instanceof Map<?, ?> nutriments)) {
            return null;
        }

        BigDecimal calories = firstDecimal(
                nutriments,
                "energy-kcal_100g",
                "energy-kcal_value"
        );

        BigDecimal protein = firstDecimal(
                nutriments,
                "proteins_100g",
                "proteins_value"
        );

        BigDecimal carbs = firstDecimal(
                nutriments,
                "carbohydrates_100g",
                "carbohydrates_value"
        );

        BigDecimal fats = firstDecimal(
                nutriments,
                "fat_100g",
                "fat_value"
        );

        if (calories == null) {
            calories = BigDecimal.ZERO;
        }

        if (protein == null) {
            protein = BigDecimal.ZERO;
        }

        if (carbs == null) {
            carbs = BigDecimal.ZERO;
        }

        if (fats == null) {
            fats = BigDecimal.ZERO;
        }

        return new OpenFoodFactsProduct(
                code,
                code,
                name,
                brand,
                calories,
                protein,
                carbs,
                fats
        );
    }

    private boolean matchesQuery(OpenFoodFactsProduct product, String query) {
        String normalizedQuery = query.toLowerCase();

        String name = product.name() == null
                ? ""
                : product.name().toLowerCase();

        String brand = product.brand() == null
                ? ""
                : product.brand().toLowerCase();

        return name.contains(normalizedQuery) || brand.contains(normalizedQuery);
    }

    private String firstString(Map<?, ?> map, String... keys) {
        for (String key : keys) {
            String value = getString(map, key);

            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }

    private String getString(Map<?, ?> map, String key) {
        Object value = map.get(key);

        if (value == null) {
            return null;
        }

        String text = value.toString().trim();

        return text.isEmpty() ? null : text;
    }

    private BigDecimal firstDecimal(Map<?, ?> map, String... keys) {
        for (String key : keys) {
            BigDecimal value = getDecimal(map, key);

            if (value != null) {
                return value;
            }
        }

        return null;
    }

    private BigDecimal getDecimal(Map<?, ?> map, String key) {
        Object value = map.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }

        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }

    public record OpenFoodFactsProduct(
            String externalFoodId,
            String barcode,
            String name,
            String brand,
            BigDecimal caloriesPer100g,
            BigDecimal proteinPer100g,
            BigDecimal carbsPer100g,
            BigDecimal fatsPer100g
    ) {
    }
}