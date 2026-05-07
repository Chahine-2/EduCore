package utils;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AIService {

    // Pollinations AI - API gratuite sans clé d'authentification
    private static final String URL = "https://text.pollinations.ai/";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    public static String envoyer(String systemPrompt, String userMessage) {
        try {
            // Configuration du corps de la requête pour Pollinations AI (format de messages OpenAI)
            JSONObject body = new JSONObject();
            JSONArray messages = new JSONArray();
            
            JSONObject systemMessageObj = new JSONObject();
            systemMessageObj.put("role", "system");
            systemMessageObj.put("content", systemPrompt);
            
            JSONObject userMessageObj = new JSONObject();
            userMessageObj.put("role", "user");
            userMessageObj.put("content", userMessage);
            
            messages.put(systemMessageObj);
            messages.put(userMessageObj);
            
            body.put("messages", messages);
            // Optionnel : Vous pouvez spécifier le modèle (openai, mistral, llama, etc.)
            body.put("model", "openai"); 

            Request request = new Request.Builder()
                    .url(URL)
                    .post(RequestBody.create(
                            body.toString(),
                            MediaType.parse("application/json")
                    ))
                    .addHeader("Content-Type", "application/json")
                    // Pas de header Authorization requis pour Pollinations !
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                return "❌ Erreur API (" + response.code() + "): " + response.message();
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                return "❌ Erreur : Réponse vide de l'API";
            }

            String bodyString = responseBody.string();
            
            // Pollinations retourne le texte généré directement en tant que chaîne de caractères simple
            if (bodyString != null && !bodyString.trim().isEmpty()) {
                // Au cas où l'API retournerait un format JSON (style OpenAI)
                try {
                    if (bodyString.trim().startsWith("{")) {
                         JSONObject json = new JSONObject(bodyString);
                         if (json.has("choices")) {
                             return json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                         }
                    }
                } catch (JSONException e) {
                    // Si ce n'est pas du JSON, c'est le texte brut, ce qui est le comportement normal
                }
                return bodyString;
            }
            
            return "❌ Erreur : Réponse inattendue ou vide.";

        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                return "⏱ Délai d'attente dépassé. Veuillez réessayer.";
            }
            return "❌ Erreur réseau : " + e.getMessage();
        } catch (Exception e) {
            return "❌ Erreur inattendue : " + e.getMessage();
        }
    }
}