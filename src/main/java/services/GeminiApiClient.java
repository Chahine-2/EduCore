package services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;
import java.io.InputStream;

/**
 * Lightweight HTTP client to call Google Gemini (configurable endpoint).
 *
 * Configuration resolution order:
 * 1) Environment variables: GEMINI_API_KEY, GEMINI_API_URL
 * 2) src/main/resources/config.properties (gemini.api.key, gemini.api.url)
 */
public class GeminiApiClient {

    private final HttpClient http;
    private final String apiKey;
    private final String apiUrl;

    public GeminiApiClient() {
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // Resolve configuration
        String keyFromEnv = System.getenv("GEMINI_API_KEY");
        String urlFromEnv = System.getenv("GEMINI_API_URL");

        String key = null;
        String url = null;

        // try properties file
        try (InputStream in = getClass().getResourceAsStream("/config.properties")) {
            if (in != null) {
                Properties p = new Properties();
                p.load(in);
                if (keyFromEnv == null) key = p.getProperty("gemini.api.key");
                if (urlFromEnv == null) url = p.getProperty("gemini.api.url");
            }
        } catch (IOException e) {
            // ignore - we will fallback to env or throw later
        }

        this.apiKey = keyFromEnv != null ? keyFromEnv : key;
        this.apiUrl = urlFromEnv != null ? urlFromEnv : (url != null ? url : "https://generativelanguage.googleapis.com/v1beta2/models/text-bison-001:generate");
    }

    /**
     * Sends the provided prompt to the configured Gemini endpoint and returns the response body as string.
     * This method uses the Google-style generateContent payload where the prompt is provided inside
     * contents[0].parts[0].text. It sends the API key using the X-goog-api-key header which matches
     * the curl example the project provides.
     */
    public String generate(String promptText) throws IOException, InterruptedException, GeminiClientException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new GeminiClientException("Missing GEMINI API key. Set GEMINI_API_KEY env or config.properties");
        }

        // Build the expected request body for the Google generative API (generateContent)
        String body = "{\n" +
                "  \"contents\": [\n" +
                "    {\n" +
                "      \"parts\": [\n" +
                "        {\n" +
                "          \"text\": \"" + escapeJson(promptText) + "\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("X-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

        int code = resp.statusCode();
        if (code >= 200 && code < 300) {
            return resp.body();
        }

        throw new GeminiClientException("API returned status " + code + ": " + resp.body());
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    public static class GeminiClientException extends Exception {
        public GeminiClientException(String message) { super(message); }
    }
}


