package com.recipesync.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipesync.dto.CreateRecipeIngredientDto;
import com.recipesync.dto.ImportedRecipeDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class RecipeImportService {

    private static final Pattern DURATION_MINUTES_PATTERN = Pattern.compile("(\\d+)\\s*(?:min|m)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern SERVINGS_PATTERN = Pattern.compile("(\\d+)");

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Inject
    public RecipeImportService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public ImportedRecipeDto importFromUrl(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            throw badRequest("Webbadressen kan inte vara tom.");
        }

        String trimmedUrl = urlString.trim();
        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
            trimmedUrl = "https://" + trimmedUrl;
        }

        URI uri;
        try {
            uri = URI.create(trimmedUrl);
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw badRequest("Ogiltig webbadress.");
            }
        } catch (Exception e) {
            throw badRequest("Ogiltig webbadress: " + e.getMessage());
        }

        String html;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "sv-SE,sv;q=0.9,en-US;q=0.8,en;q=0.7")
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 400) {
                throw badRequest("Kunde inte hämta webbsidan (HTTP " + response.statusCode() + ").");
            }
            html = response.body();
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw badRequest("Kunde inte ansluta till webbplatsen: " + e.getMessage());
        }

        return parseRecipeFromHtml(html, trimmedUrl);
    }

    public ImportedRecipeDto parseRecipeFromHtml(String html, String sourceUrl) {
        Document doc = Jsoup.parse(html, sourceUrl != null ? sourceUrl : "");
        Elements scriptTags = doc.select("script[type=application/ld+json]");

        for (Element script : scriptTags) {
            String jsonText = script.data();
            if (jsonText == null || jsonText.isBlank()) {
                continue;
            }

            try {
                JsonNode root = objectMapper.readTree(jsonText.trim());
                JsonNode recipeNode = findRecipeNode(root);
                if (recipeNode != null) {
                    return buildFromRecipeNode(recipeNode, sourceUrl);
                }
            } catch (Exception ignored) {
                // If this specific script block has invalid JSON, continue checking others
            }
        }

        throw badRequest("Kunde inte hitta något standardiserat recept (schema.org/Recipe) på webbsidan. Kontrollera att länken leder direkt till en receptsida.");
    }

    private JsonNode findRecipeNode(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        if (node.isArray()) {
            for (JsonNode child : node) {
                JsonNode found = findRecipeNode(child);
                if (found != null) {
                    return found;
                }
            }
            return null;
        }

        if (node.isObject()) {
            JsonNode typeNode = node.get("@type");
            if (isRecipeType(typeNode)) {
                return node;
            }

            if (node.has("@graph")) {
                JsonNode found = findRecipeNode(node.get("@graph"));
                if (found != null) {
                    return found;
                }
            }

            if (node.has("mainEntity")) {
                JsonNode found = findRecipeNode(node.get("mainEntity"));
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    private boolean isRecipeType(JsonNode typeNode) {
        if (typeNode == null || typeNode.isNull()) {
            return false;
        }
        if (typeNode.isTextual()) {
            return "Recipe".equalsIgnoreCase(typeNode.asText().trim());
        }
        if (typeNode.isArray()) {
            for (JsonNode elem : typeNode) {
                if (elem.isTextual() && "Recipe".equalsIgnoreCase(elem.asText().trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    private ImportedRecipeDto buildFromRecipeNode(JsonNode recipe, String sourceUrl) {
        String title = unescape(recipe.path("name").asText(null));
        if (title == null || title.isBlank()) {
            title = "Importerat recept";
        }

        String description = unescape(recipe.path("description").asText(null));
        if (description != null && description.isBlank()) {
            description = null;
        }

        Integer servings = parseServings(recipe.get("recipeYield"));
        Integer prepTime = parseDurationMinutes(recipe.path("prepTime").asText(null));
        Integer cookTime = parseDurationMinutes(recipe.path("cookTime").asText(null));
        Integer totalTime = parseDurationMinutes(recipe.path("totalTime").asText(null));

        if (prepTime == null && cookTime == null && totalTime != null) {
            cookTime = totalTime;
        } else if (prepTime != null && cookTime == null && totalTime != null && totalTime > prepTime) {
            cookTime = totalTime - prepTime;
        }

        String imageUrl = extractImageUrl(recipe.get("image"));
        List<CreateRecipeIngredientDto> ingredients = extractIngredients(recipe.get("recipeIngredient"));
        String instructions = extractInstructions(recipe.get("recipeInstructions"));

        return new ImportedRecipeDto(
                title,
                description,
                instructions,
                servings,
                prepTime,
                cookTime,
                sourceUrl,
                imageUrl,
                ingredients
        );
    }

    private String extractImageUrl(JsonNode imageNode) {
        if (imageNode == null || imageNode.isNull()) {
            return null;
        }
        if (imageNode.isTextual()) {
            return imageNode.asText().trim();
        }
        if (imageNode.isArray() && imageNode.size() > 0) {
            JsonNode first = imageNode.get(0);
            return extractImageUrl(first);
        }
        if (imageNode.isObject()) {
            if (imageNode.hasNonNull("url")) {
                return imageNode.get("url").asText().trim();
            }
        }
        return null;
    }

    private List<CreateRecipeIngredientDto> extractIngredients(JsonNode ingredientNode) {
        List<CreateRecipeIngredientDto> result = new ArrayList<>();
        if (ingredientNode == null || ingredientNode.isNull()) {
            return result;
        }

        if (ingredientNode.isArray()) {
            for (JsonNode item : ingredientNode) {
                String line = item.isTextual() ? item.asText() : item.path("text").asText("");
                if (!line.isBlank()) {
                    CreateRecipeIngredientDto dto = IngredientParser.parse(line);
                    if (dto != null) {
                        result.add(dto);
                    }
                }
            }
        } else if (ingredientNode.isTextual()) {
            String[] lines = ingredientNode.asText().split("\\r?\\n");
            for (String line : lines) {
                if (!line.isBlank()) {
                    CreateRecipeIngredientDto dto = IngredientParser.parse(line);
                    if (dto != null) {
                        result.add(dto);
                    }
                }
            }
        }

        return result;
    }

    private String extractInstructions(JsonNode instructionsNode) {
        if (instructionsNode == null || instructionsNode.isNull()) {
            return null;
        }

        List<String> steps = new ArrayList<>();
        collectInstructions(instructionsNode, steps);

        if (steps.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        int stepNumber = 1;
        for (String step : steps) {
            String trimmed = step.trim();
            if (trimmed.isBlank()) continue;

            if (trimmed.startsWith("###")) {
                // Section header
                if (sb.length() > 0) sb.append("\n\n");
                sb.append(trimmed);
                stepNumber = 1;
            } else {
                if (sb.length() > 0) sb.append("\n");
                // Remove existing numbers like "1." if present
                String cleanStep = trimmed.replaceAll("^\\d+[.)]\\s*", "");
                sb.append(stepNumber++).append(". ").append(cleanStep);
            }
        }

        return sb.toString();
    }

    private void collectInstructions(JsonNode node, List<String> steps) {
        if (node == null || node.isNull()) {
            return;
        }

        if (node.isTextual()) {
            String[] lines = node.asText().split("\\r?\\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isBlank()) {
                    steps.add(trimmed);
                }
            }
            return;
        }

        if (node.isArray()) {
            for (JsonNode item : node) {
                collectInstructions(item, steps);
            }
            return;
        }

        if (node.isObject()) {
            String type = node.path("@type").asText("");
            if ("HowToSection".equalsIgnoreCase(type)) {
                String sectionName = node.path("name").asText("");
                if (!sectionName.isBlank()) {
                    steps.add("### " + sectionName);
                }
                if (node.has("itemListElement")) {
                    collectInstructions(node.get("itemListElement"), steps);
                }
            } else {
                // HowToStep or text
                String text = node.path("text").asText("");
                if (text.isBlank()) {
                    text = node.path("name").asText("");
                }
                if (!text.isBlank()) {
                    steps.add(text);
                }
            }
        }
    }

    public Integer parseServings(JsonNode yieldNode) {
        if (yieldNode == null || yieldNode.isNull()) {
            return 4; // Sensible default
        }

        String text;
        if (yieldNode.isArray() && yieldNode.size() > 0) {
            text = yieldNode.get(0).asText();
        } else {
            text = yieldNode.asText();
        }

        if (text == null || text.isBlank()) {
            return 4;
        }

        Matcher matcher = SERVINGS_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }

        return 4;
    }

    public Integer parseDurationMinutes(String durationStr) {
        if (durationStr == null || durationStr.isBlank()) {
            return null;
        }

        String clean = durationStr.trim();
        // Try ISO 8601 duration e.g. "PT30M", "PT1H15M"
        if (clean.startsWith("P") || clean.startsWith("p")) {
            try {
                Duration duration = Duration.parse(clean.toUpperCase(Locale.ROOT));
                return (int) duration.toMinutes();
            } catch (Exception ignored) {
            }
        }

        // Try extracting digits like "30 min", "45", etc.
        Matcher matcher = DURATION_MINUTES_PATTERN.matcher(clean);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }

        return null;
    }

    public ImportedRecipeDto importFromText(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            throw badRequest("Recepttexten kan inte vara tom.");
        }

        String[] lines = rawText.split("\\r?\\n");
        String title = null;
        Integer servings = 4;
        Integer prepTime = null;
        Integer cookTime = null;

        List<CreateRecipeIngredientDto> ingredients = new ArrayList<>();
        StringBuilder instructionsBuilder = new StringBuilder();

        boolean inIngredients = false;
        boolean inInstructions = false;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isBlank()) continue;

            String lower = line.toLowerCase(Locale.ROOT);

            // Detect section headers
            if (lower.startsWith("ingrediens") || lower.startsWith("ingredienser:")) {
                inIngredients = true;
                inInstructions = false;
                continue;
            } else if (lower.startsWith("gör så här") || lower.startsWith("instruktion") || lower.startsWith("tillagning")) {
                inIngredients = false;
                inInstructions = true;
                continue;
            }

            if (title == null) {
                // First non-empty line is assumed to be title
                title = line;
                continue;
            }

            // Check for servings line e.g. "4 portioner"
            if (lower.contains("portion")) {
                Matcher m = SERVINGS_PATTERN.matcher(line);
                if (m.find()) {
                    servings = Integer.parseInt(m.group(1));
                    continue;
                }
            }

            // Check for time line e.g. "30 min"
            if (lower.contains("min") || lower.contains("tim")) {
                Integer time = parseDurationMinutes(line);
                if (time != null && cookTime == null) {
                    cookTime = time;
                    continue;
                }
            }

            if (inInstructions) {
                if (instructionsBuilder.length() > 0) instructionsBuilder.append("\n");
                instructionsBuilder.append(line);
            } else if (inIngredients) {
                CreateRecipeIngredientDto dto = IngredientParser.parse(line);
                if (dto != null) {
                    ingredients.add(dto);
                }
            } else {
                // Heuristic: if it parses nicely with amount/unit, treat as ingredient
                CreateRecipeIngredientDto dto = IngredientParser.parse(line);
                if (dto != null && (dto.amount() != null || dto.unit() != null)) {
                    ingredients.add(dto);
                } else {
                    if (instructionsBuilder.length() > 0) instructionsBuilder.append("\n");
                    instructionsBuilder.append(line);
                }
            }
        }

        return new ImportedRecipeDto(
                title != null ? title : "Importerat recept",
                null,
                instructionsBuilder.length() > 0 ? instructionsBuilder.toString() : null,
                servings,
                prepTime,
                cookTime,
                null,
                null,
                ingredients
        );
    }

    private String unescape(String text) {
        if (text == null) return null;
        return Jsoup.parse(text).text();
    }

    private BadRequestException badRequest(String message) {
        return new BadRequestException(
                Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("message", message))
                        .type(MediaType.APPLICATION_JSON)
                        .build()
        );
    }
}
