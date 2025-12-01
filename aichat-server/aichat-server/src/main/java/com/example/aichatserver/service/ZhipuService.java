package com.example.aichatserver.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.function.Consumer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class ZhipuService {
    @Value("${zhipu.api-key:}")
    private String apiKey;

    private final OkHttpClient client = new OkHttpClient();

    public boolean stream(String prompt, Consumer<String> onPiece) {
        if (apiKey == null || apiKey.isBlank()) return false;
        try {
            String payload = "{\"model\":\"glm-3-turbo\",\"messages\":[{\"role\":\"user\",\"content\":\""
                    + escape(prompt) + "\"}],\"stream\":true}";
            Request request = new Request.Builder()
                    .url("https://open.bigmodel.cn/api/paas/v4/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(payload, okhttp3.MediaType.get("application/json")))
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) return false;

            try (ResponseBody body = response.body()) {
                BufferedSource source = body.source();
                while (!source.exhausted()) {
                    String raw = source.readUtf8Line();
                    if (raw == null) break;
                    String line = raw.trim();
                    if (line.isEmpty()) continue;
                    String data = line.startsWith("data:") ? line.substring(5).trim() : line;
                    if ("[DONE]".equals(data)) break;
                    String piece = extractContent(data);
                    if (!piece.isEmpty()) onPiece.accept(piece);
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String extractContent(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            JsonArray choices = obj.getAsJsonArray("choices");
            if (choices != null && choices.size() > 0) {
                JsonObject c0 = choices.get(0).getAsJsonObject();
                if (c0.has("delta")) {
                    JsonObject delta = c0.getAsJsonObject("delta");
                    if (delta != null && delta.has("content")) return delta.get("content").getAsString();
                }
                if (c0.has("message")) {
                    JsonObject msg = c0.getAsJsonObject("message");
                    if (msg != null && msg.has("content")) return msg.get("content").getAsString();
                }
            }
        } catch (Exception ignore) {}
        return "";
    }
}