package com.example.aichatserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Configuration
public class AiConfig {

  @Bean
  @ConfigurationProperties(prefix = "ai")
  public AiProperties aiProperties() { return new AiProperties(); }

  @Bean
  public WebClient siliconflowClient(AiProperties props) {
    ExchangeStrategies strategies = ExchangeStrategies.builder()
        .codecs(c -> c.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
        .build();
    AiProperties.Provider provider = null;
    if (props.getProviders() != null) {
      provider = props.getProviders().getOrDefault("siliconflow", new AiProperties.Provider());
    } else {
      provider = new AiProperties.Provider();
    }
    WebClient.Builder b = WebClient.builder()
        .baseUrl(provider.getBaseUrl() != null ? provider.getBaseUrl() : "https://api.siliconflow.cn")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .exchangeStrategies(strategies);
    if (provider.getApiKey() != null && !provider.getApiKey().isBlank()) {
      b.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + provider.getApiKey());
    }
    return b.build();
  }

  public static class AiProperties {
    private Map<String, Provider> providers;
    private Map<String, String> modelMap;

    public Map<String, Provider> getProviders() { return providers; }
    public void setProviders(Map<String, Provider> providers) { this.providers = providers; }
    public Map<String, String> getModelMap() { return modelMap; }
    public void setModelMap(Map<String, String> modelMap) { this.modelMap = modelMap; }

    public static class Provider {
      private String baseUrl;
      private String apiKey;
      public String getBaseUrl() { return baseUrl; }
      public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
      public String getApiKey() { return apiKey; }
      public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    }
  }
}
