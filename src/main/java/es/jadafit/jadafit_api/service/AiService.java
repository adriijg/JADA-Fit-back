package es.jadafit.jadafit_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.jadafit.jadafit_api.dto.FitnessProfileResponseDTO;
import es.jadafit.jadafit_api.dto.MessageDTO;
import es.jadafit.jadafit_api.dto.NutritionDaySummaryResponseDTO;
import es.jadafit.jadafit_api.dto.NutritionMealResponseDTO;
import es.jadafit.jadafit_api.model.MealType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class AiService {

    private static final int MAX_HISTORY = 10;
    private static final int HISTORY_DAYS = 3;

    private final String apiUrl;
    private final String model;
    private final ObjectMapper objectMapper;
    private final NutritionService nutritionService;
    private final NutritionGoalService nutritionGoalService;
    private final FitnessProfileService fitnessProfileService;

    public AiService(
            @Value("${ai.api.url}") String apiUrl,
            @Value("${ai.api.model}") String model,
            NutritionService nutritionService,
            NutritionGoalService nutritionGoalService,
            FitnessProfileService fitnessProfileService
    ) {
        this.apiUrl = apiUrl;
        this.model = model;
        this.objectMapper = new ObjectMapper();
        this.nutritionService = nutritionService;
        this.nutritionGoalService = nutritionGoalService;
        this.fitnessProfileService = fitnessProfileService;
    }

    public String chat(String userId, String userMessage, List<MessageDTO> history) {
        try {
            UUID uuid = UUID.fromString(userId);
            String userContext = buildUserContext(uuid);
            String jsonBody = buildRequestBody(userContext, userMessage, history);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return "Error: el servidor local respondió con código " + response.statusCode();
            }

            return extractText(response.body());
        } catch (Exception e) {
            return "Error llamando a la IA local: " + e.getMessage();
        }
    }

    private String buildUserContext(UUID userId) {
        StringBuilder sb = new StringBuilder();

        appendProfile(sb, userId);
        appendTodayNutrition(sb, userId);
        appendNutritionHistory(sb, userId);

        return sb.toString();
    }

    private void appendProfile(StringBuilder sb, UUID userId) {
        try {
            FitnessProfileResponseDTO profile = fitnessProfileService.getMyFitnessProfile(userId);
            sb.append("- Peso: ").append(profile.weight()).append(" kg");
            if (profile.height() != null) {
                sb.append(", Altura: ").append(profile.height()).append(" cm");
            }
            if (profile.age() != null) {
                sb.append(", Edad: ").append(profile.age()).append(" años");
            }
            if (profile.goal() != null) {
                String goalText = switch (profile.goal()) {
                    case GANAR_MUSCULO -> "Ganar músculo";
                    case PERDER_GRASA -> "Perder grasa";
                    case MANTENERSE_ATLETICO -> "Mantenerse atlético";
                    case MEJORAR_RENDIMIENTO -> "Mejorar rendimiento";
                    case RECOMPOSICION_CORPORAL -> "Recomposición corporal";
                };
                sb.append("\n- Objetivo: ").append(goalText);
            }
            if (profile.gender() != null) {
                String genderText = switch (profile.gender()) {
                    case HOMBRE -> "Hombre";
                    case MUJER -> "Mujer";
                };
                sb.append("\n- Género: ").append(genderText);
            }
        } catch (Exception ignored) {}
    }

    private void appendTodayNutrition(StringBuilder sb, UUID userId) {
        try {
            LocalDate today = LocalDate.now();
            NutritionDaySummaryResponseDTO nutrition = nutritionService.getDaySummary(userId, today);
            String dateStr = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            sb.append("\n\n--- NUTRICIÓN HOY (").append(dateStr).append(") ---");
            sb.append("\n- Calorías: ").append(nutrition.totalCalories().intValue())
                    .append(" / ").append(nutrition.caloriesTarget().intValue()).append(" kcal");
            sb.append("\n- Proteína: ").append(nutrition.totalProtein().intValue())
                    .append(" / ").append(nutrition.proteinTarget().intValue()).append(" g");
            sb.append("\n- Carbohidratos: ").append(nutrition.totalCarbs().intValue())
                    .append(" / ").append(nutrition.carbsTarget().intValue()).append(" g");
            sb.append("\n- Grasas: ").append(nutrition.totalFats().intValue())
                    .append(" / ").append(nutrition.fatsTarget().intValue()).append(" g");

            List<NutritionMealResponseDTO> meals = nutrition.meals();
            if (meals != null && !meals.isEmpty()) {
                sb.append("\n\nComidas registradas hoy:");
                for (NutritionMealResponseDTO meal : meals) {
                    String mealTypeLabel = switch (meal.mealType()) {
                        case DESAYUNO -> "DESAYUNO";
                        case COMIDA -> "COMIDA";
                        case CENA -> "CENA";
                        case SNACK -> "SNACK";
                    };
                    sb.append("\n  • ").append(mealTypeLabel)
                            .append(": ").append(meal.foodName())
                            .append(" (").append(meal.quantityGrams().intValue()).append("g)")
                            .append(" - ").append(meal.calories().intValue()).append(" kcal")
                            .append(" | ").append(meal.protein().intValue()).append("g prot")
                            .append(" | ").append(meal.carbs().intValue()).append("g carb")
                            .append(" | ").append(meal.fats().intValue()).append("g gras");
                }
            } else {
                sb.append("\n\nNo hay comidas registradas hoy.");
            }
        } catch (Exception ignored) {}
    }

    private void appendNutritionHistory(StringBuilder sb, UUID userId) {
        try {
            LocalDate today = LocalDate.now();
            sb.append("\n\n--- HISTORIAL RECIENTE ---");

            for (int i = 1; i <= HISTORY_DAYS; i++) {
                LocalDate day = today.minusDays(i);
                try {
                    NutritionDaySummaryResponseDTO nutrition = nutritionService.getDaySummary(userId, day);
                    String dayStr = day.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    int mealsCount = nutrition.meals() != null ? nutrition.meals().size() : 0;

                    sb.append("\n- ").append(dayStr).append(": ")
                            .append(nutrition.totalCalories().intValue()).append("/").append(nutrition.caloriesTarget().intValue()).append(" kcal")
                            .append(" | Prot ").append(nutrition.totalProtein().intValue()).append("/").append(nutrition.proteinTarget().intValue()).append("g")
                            .append(" | Carb ").append(nutrition.totalCarbs().intValue()).append("/").append(nutrition.carbsTarget().intValue()).append("g")
                            .append(" | Gras ").append(nutrition.totalFats().intValue()).append("/").append(nutrition.fatsTarget().intValue()).append("g")
                            .append(" (").append(mealsCount).append(" comidas)");
                } catch (Exception ignored) {
                    sb.append("\n- ").append(day.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                            .append(": sin datos");
                }
            }
        } catch (Exception ignored) {}
    }

    private String buildRequestBody(String userContext, String userMessage, List<MessageDTO> history) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", model);
        root.put("temperature", 0.7);
        root.put("top_p", 0.9);
        root.put("max_tokens", 1024);

        ArrayNode messages = root.putArray("messages");

        ObjectNode systemMsg = messages.addObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", """
                Eres JADA Fit, un entrenador personal experto en fitness y nutrición.
                Responde en español, de forma clara y concisa.

                NORMAS:
                - Usa los DATOS DEL USUARIO que se proporcionan abajo. No inventes calorías, proteínas, carbohidratos ni grasas.
                - Responde ÚNICAMENTE a lo que el usuario pregunta. No te desvíes del tema.
                - Si faltan datos relevantes, indícalo brevemente.
                - Las recomendaciones deben ser prácticas y adaptadas a los datos del usuario.

                DATOS DEL USUARIO:
                """ + userContext);

        int start = Math.max(0, history.size() - MAX_HISTORY);
        for (int i = start; i < history.size(); i++) {
            MessageDTO msg = history.get(i);
            ObjectNode historyMsg = messages.addObject();
            historyMsg.put("role", msg.getRole());
            historyMsg.put("content", msg.getContent());
        }

        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);

        return root.toString();
    }

    private String extractText(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && !choices.isEmpty()) {
                JsonNode message = choices.get(0).get("message");
                if (message != null) {
                    JsonNode content = message.get("content");
                    if (content != null) {
                        return content.asText();
                    }
                }
            }
        } catch (Exception ignored) {}
        return json;
    }
}
