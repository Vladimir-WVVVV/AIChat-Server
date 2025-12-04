package com.example.aichatserver.config;

import com.example.aichatserver.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final JwtService jwtService;
  private final String allowedOrigins;

  public WebConfig(JwtService jwtService, org.springframework.core.env.Environment env) {
    this.jwtService = jwtService;
    this.allowedOrigins = env.getProperty("cors.allowed-origins", "*");
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins(allowedOrigins.split(","))
        .allowedMethods("GET", "POST", "DELETE", "PUT")
        .allowCredentials(true);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new AuthInterceptor(jwtService))
        .addPathPatterns("/stream/**", "/multimodal/**", "/conversations/**", "/messages/**")
        .excludePathPatterns("/auth/**", "/actuator/**", "/health");
    registry.addInterceptor(new StreamConcurrencyInterceptor())
        .addPathPatterns("/stream/**");
  }

  static class AuthInterceptor implements HandlerInterceptor {
    private final JwtService jwtService;
    AuthInterceptor(JwtService jwtService) { this.jwtService = jwtService; }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
      String header = request.getHeader("Authorization");
      if (header == null || !header.startsWith("Bearer ")) {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"missing_token\"}");
        return false;
      }
      String sub = jwtService.verifyToken(header.substring(7));
      if (sub == null) {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"invalid_token\"}");
        return false;
      }
      request.setAttribute("subject", sub);
      return true;
    }
  }

  static class StreamConcurrencyInterceptor implements HandlerInterceptor {
    private final java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.atomic.AtomicInteger> counters = new java.util.concurrent.ConcurrentHashMap<>();
    private final int limit = 2;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
      String key = String.valueOf(request.getAttribute("subject"));
      if (key == null || key.equals("null")) key = "anonymous";
      java.util.concurrent.atomic.AtomicInteger c = counters.computeIfAbsent(key, k -> new java.util.concurrent.atomic.AtomicInteger(0));
      if (c.incrementAndGet() > limit) {
        c.decrementAndGet();
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"RATE_LIMIT\",\"message\":\"too_many_streams\"}");
        return false;
      }
      request.setAttribute("_stream_counter_key", key);
      return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
      String key = String.valueOf(request.getAttribute("_stream_counter_key"));
      if (key != null) {
        java.util.concurrent.atomic.AtomicInteger c = counters.get(key);
        if (c != null) c.decrementAndGet();
      }
    }
  }
}
