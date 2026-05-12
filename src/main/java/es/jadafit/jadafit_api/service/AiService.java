package es.jadafit.jadafit_api.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.jadafit.jadafit_api.dto.FitnessProfileResponseDTO;
import es.jadafit.jadafit_api.dto.NutritionDaySummaryResponseDTO;
import es.jadafit.jadafit_api.dto.NutritionGoalResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
@Service
public class AiService {
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
    public String chat(String userId, String userMessage) {
        try {
            UUID uuid = UUID.fromString(userId);
            String userContext = buildUserContext(uuid);
            String jsonBody = buildRequestBody(userContext, userMessage);
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
                    default -> profile.goal().name().replace("_", " ").toLowerCase();
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
        try {
            LocalDate today = LocalDate.now();
            NutritionDaySummaryResponseDTO nutrition = nutritionService.getDaySummary(userId, today);
            NutritionGoalResponseDTO goals = nutritionGoalService.getMyNutritionGoal(userId);
            String dateStr = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            sb.append("\n- Fecha: ").append(dateStr);
            sb.append("\n- Calorías: ").append(nutrition.totalCalories().intValue())
                    .append(" / ").append(goals.caloriesTarget().intValue()).append(" kcal");
            sb.append("\n- Proteína: ").append(nutrition.totalProtein().intValue())
                    .append(" / ").append(goals.proteinTarget().intValue()).append(" g");
            sb.append("\n- Carbohidratos: ").append(nutrition.totalCarbs().intValue())
                    .append(" / ").append(goals.carbsTarget().intValue()).append(" g");
            sb.append("\n- Grasas: ").append(nutrition.totalFats().intValue())
                    .append(" / ").append(goals.fatsTarget().intValue()).append(" g");
        } catch (Exception ignored) {}
        return sb.toString();
    }
    private String buildRequestBody(String userContext, String userMessage) {
        return """
        {
          "model": "%s",
          "messages": [
            {
              "role": "system",
              "content": "Eres un entrenador personal experto en fitness y nutrición. Responde de forma clara, práctica y breve. Usa los datos del usuario para dar recomendaciones personalizadas.\\n\\nDATOS DEL USUARIO:\\n%s"
            },
            {
              "role": "user",
              "content": "%s"
            }
          ]
        }
        """.formatted(model, escapeJson(userContext), escapeJson(userMessage));
    }
    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
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