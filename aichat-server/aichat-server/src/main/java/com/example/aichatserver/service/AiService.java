package com.example.aichatserver.service;

import com.example.aichatserver.config.AiConfig.AiProperties;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import  java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiService {
  private final WebClient siliconflowClient;
  private final AiProperties props;

  public AiService(WebClient siliconflowClient, AiProperties props) {
    this.siliconflowClient = siliconflowClient;
    this.props = props;
  }

  public Flux<ServerSentEvent<String>> streamText(Long conversationId, String prompt, String modelKey) {
    String upstreamModel = resolveModel(modelKey);
    Map<String, Object> body = new HashMap<>();
    body.put("model", upstreamModel);
    body.put("stream", true);
    body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

    return postCompletions(body);
  }

  public Flux<ServerSentEvent<String>> streamImage(Long conversationId, String prompt, String modelKey, byte[] imageBytes, String imageUrl) {
    String upstreamModel = resolveModel(modelKey);
    Map<String, Object> body = new HashMap<>();
    body.put("model", upstreamModel);
    body.put("stream", true);

    Map<String, Object> contentText = Map.of("type", "text", "text", prompt);
    Map<String, Object> contentImage;
    if (imageBytes != null) {
      String base64 = Base64.getEncoder().encodeToString(imageBytes);
      contentImage = Map.of("type", "input_image", "image", Map.of("data", base64));
    } else {
      contentImage = Map.of("type", "input_image", "image", Map.of("url", imageUrl));
    }
    body.put("messages", List.of(Map.of("role", "user", "content", List.of(contentText, contentImage))));

    return postCompletions(body);
  }

  private Flux<ServerSentEvent<String>> postCompletions(Map<String, Object> body) {
    return siliconflowClient.post()
        .uri("/v1/chat/completions")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .exchangeToFlux(this::consumeSse)
        .timeout(java.time.Duration.ofSeconds(60))
        .retryWhen(reactor.util.retry.Retry.backoff(2, java.time.Duration.ofMillis(300)))
        .onErrorResume(e -> Flux.just(ServerSentEvent.builder("服务错误").build()));
  }

  private Flux<ServerSentEvent<String>> consumeSse(ClientResponse resp) {
    MediaType mt = resp.headers().contentType().orElse(MediaType.APPLICATION_JSON);
    Flux<String> lines;
    if (mt.includes(MediaType.TEXT_EVENT_STREAM)) {
      lines = resp.bodyToFlux(String.class);
    } else {
      lines = resp.bodyToFlux(String.class);
    }
    return lines
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(s -> s.startsWith("data:") ? s.substring(5).trim() : s)
        .takeUntil(s -> "[DONE]".equals(s))
        .map(this::extractContent)
        .filter(s -> !s.isEmpty())
        .map(s -> ServerSentEvent.builder(s).build())
        .concatWith(Flux.just(ServerSentEvent.builder("[DONE]").build()));
  }

  private String resolveModel(String modelKey) {
    if (modelKey == null || modelKey.isBlank()) return "gpt-3.5-turbo";
    String mapped = props.getModelMap() != null ? props.getModelMap().get(modelKey) : null;
    return mapped != null ? mapped : modelKey;
  }

  private String extractContent(String json) {
    try {
      int idx = json.indexOf("\"content\":");
      if (idx < 0) return "";
      int start = json.indexOf('"', idx + 10);
      if (start < 0) return "";
      int end = json.indexOf('"', start + 1);
      if (end < 0) return "";
      String raw = json.substring(start + 1, end);
      return unescape(raw);
    } catch (Exception e) {
      return "";
    }
  }

  private String unescape(String s) {
    return s.replace("\\\"", "\"")
        .replace("\\n", "\n")
        .replace("\\r", "\r")
        .replace("\\t", "\t");
  }
}
