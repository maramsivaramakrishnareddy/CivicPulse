package com.smartgovernance.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;


@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public static class Classification {
        public String department;
        public String priority;
    }

    /**
     * Suggests both the department and priority for a complaint description in a single
     * Gemini call. departmentNames must be the exact list of valid department names (as
     * stored in the Department table) so the AI's answer always matches a real department.
     */
    public Classification classifyComplaint(String description, List<String> departmentNames) {
        Classification fallback = fallbackClassify(description, departmentNames);
        if (apiKey == null || apiKey.isBlank()) {
            logger.warn("gemini.api.key is not set - AI department/priority suggestion is using the keyword fallback only.");
            return fallback;
        }

        String prompt = "You are classifying a citizen civic complaint for an Indian local governance " +
                "platform. Valid departments: " + String.join(", ", departmentNames) + ". " +
                "Given the complaint description below, respond with ONLY a compact JSON object like " +
                "{\"department\": \"<one of the department names exactly as given>\", \"priority\": \"LOW|MEDIUM|HIGH\"}. " +
                "No explanation, no markdown, just the JSON.\n\nComplaint: \"" + description + "\"";
        try {
            String raw = callGeminiText(prompt);
            String cleaned = raw.replaceAll("```json", "").replaceAll("```", "").trim();
            JsonNode node = mapper.readTree(cleaned);
            Classification result = new Classification();
            result.department = node.has("department") ? node.get("department").asText() : fallback.department;
            result.priority = node.has("priority") ? node.get("priority").asText() : fallback.priority;
            // Guard against the AI returning a department name outside the real list
            if (departmentNames.stream().noneMatch(d -> d.equalsIgnoreCase(result.department))) {
                result.department = fallback.department;
            }
            return result;
        } catch (Exception e) {
            logger.warn("Gemini department/priority classification failed, using keyword fallback: {}", e.getMessage());
            return fallback;
        }
    }

    private Classification fallbackClassify(String description, List<String> departmentNames) {
        Classification c = new Classification();
        c.priority = fallbackPriority(description);
        c.department = departmentNames.isEmpty() ? "Sanitation Department" : departmentNames.get(0);
        if (description == null) return c;
        String text = description.toLowerCase();
        for (String name : departmentNames) {
            String key = name.toLowerCase();
            if (key.contains("electric") && (text.contains("light") || text.contains("power") || text.contains("electric") || text.contains("transformer"))) {
                c.department = name;
            } else if (key.contains("water") && (text.contains("water") || text.contains("pipe") || text.contains("leak"))) {
                c.department = name;
            } else if (key.contains("road") && (text.contains("road") || text.contains("pothole") || text.contains("footpath"))) {
                c.department = name;
            } else if (key.contains("sanitation") && (text.contains("garbage") || text.contains("waste") || text.contains("sewage") || text.contains("drain"))) {
                c.department = name;
            } else if (key.contains("traffic") && (text.contains("traffic") || text.contains("signal") || text.contains("parking"))) {
                c.department = name;
            }
        }
        return c;
    }

    /** Returns "LOW", "MEDIUM", or "HIGH" based on the complaint description's urgency. */
    public String detectPriority(String description) {
        if (apiKey == null || apiKey.isBlank()) {
            return fallbackPriority(description);
        }
        String prompt = "You are triaging a citizen civic complaint for an Indian local governance " +
                "platform. Read the description and decide its urgency. Respond with ONLY one word: " +
                "LOW, MEDIUM, or HIGH. No explanation, no punctuation.\n\nComplaint: \"" + description + "\"";
        try {
            String raw = callGeminiText(prompt).trim().toUpperCase();
            if (raw.contains("HIGH")) return "HIGH";
            if (raw.contains("LOW")) return "LOW";
            return "MEDIUM";
        } catch (Exception e) {
            logger.warn("Gemini priority detection failed, using keyword fallback: {}", e.getMessage());
            return fallbackPriority(description);
        }
    }

    public static class PhotoMatchResult {
        public boolean matches;
        public String reason;
    }

    /**
     * Checks whether the given photo (as a base64 data URL, e.g. from the frontend's live
     * camera capture) plausibly matches the complaint description. Reads bytes/mime type
     * straight out of the data URL - no separate upload step needed since this project
     * stores evidence as base64 data URLs already.
     */
    public PhotoMatchResult verifyPhotoMatchesDescription(String imageDataUrl, String description) {
        PhotoMatchResult openResult = new PhotoMatchResult();
        openResult.matches = true;
        openResult.reason = "AI photo verification skipped";

        if (apiKey == null || apiKey.isBlank() || imageDataUrl == null || !imageDataUrl.startsWith("data:")) {
            return openResult;
        }

        try {
            int commaIndex = imageDataUrl.indexOf(',');
            String header = imageDataUrl.substring(5, imageDataUrl.indexOf(';')); // e.g. "image/jpeg"
            String base64Data = imageDataUrl.substring(commaIndex + 1);

            String prompt = "You are reviewing a citizen's civic complaint submission for an Indian " +
                    "local governance app. The citizen wrote this description: \"" + description + "\". " +
                    "Look at the attached photo. Does the photo plausibly show the kind of civic issue " +
                    "described (e.g. a road/pothole problem for a road complaint, a garbage/waste issue " +
                    "for a sanitation complaint, etc.)? It doesn't need to be a perfect match - just " +
                    "plausibly related, not something completely unrelated (like a selfie, a pet, food, " +
                    "or a random object) when the description clearly describes a civic issue. " +
                    "Respond with ONLY a compact JSON object like " +
                    "{\"matches\": true|false, \"reason\": \"<one short sentence>\"}. No markdown, just the JSON.";

            String raw = callGeminiWithImage(prompt, base64Data, header);
            String cleaned = raw.replaceAll("```json", "").replaceAll("```", "").trim();
            JsonNode node = mapper.readTree(cleaned);

            PhotoMatchResult result = new PhotoMatchResult();
            result.matches = !node.has("matches") || node.get("matches").asBoolean(true);
            result.reason = node.has("reason") ? node.get("reason").asText() : "";
            return result;
        } catch (Exception e) {
            logger.warn("Gemini photo verification failed, skipping check: {}", e.getMessage());
            return openResult;
        }
    }

    private String fallbackPriority(String description) {
        if (description == null) return "MEDIUM";
        String text = description.toLowerCase();
        if (text.contains("fire") || text.contains("accident") || text.contains("collapse")
                || text.contains("danger") || text.contains("urgent") || text.contains("emergency")) {
            return "HIGH";
        }
        if (text.contains("minor") || text.contains("small")) {
            return "LOW";
        }
        return "MEDIUM";
    }

    private String callGeminiText(String prompt) {
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );
        return postToGemini(body);
    }

    private String callGeminiWithImage(String prompt, String base64Image, String mimeType) {
        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> imagePart = Map.of(
                "inlineData", Map.of("mimeType", mimeType, "data", base64Image)
        );
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(textPart, imagePart)))
        );
        return postToGemini(body);
    }

    private String postToGemini(Map<String, Object> body) {
        String url = apiUrl + "?key=" + apiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        Map<?, ?> response = restTemplate.postForObject(url, entity, Map.class);
        List<?> candidates = (List<?>) response.get("candidates");
        Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
        Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
        List<?> parts = (List<?>) content.get("parts");
        Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);
        return (String) firstPart.get("text");
    }
}
