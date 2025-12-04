package com.example.aichatserver.controller;

import com.example.aichatserver.domain.User;
import com.example.aichatserver.repo.UserRepository;
import com.example.aichatserver.service.JwtService;
import com.example.aichatserver.service.PasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
  private final UserRepository userRepo;
  private final PasswordService passwordService;
  private final JwtService jwtService;

  public AuthController(UserRepository userRepo, PasswordService passwordService, JwtService jwtService) {
    this.userRepo = userRepo;
    this.passwordService = passwordService;
    this.jwtService = jwtService;
  }

  @PostMapping("/register")
  public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
    String username = body.get("username");
    String password = body.get("password");
    if (username == null || username.isBlank()) return ResponseEntity.badRequest().body(Map.of("code","VALIDATION_ERROR","message","username_required"));
    if (password == null || password.length() < 6) return ResponseEntity.badRequest().body(Map.of("code","VALIDATION_ERROR","message","password_too_short"));
    if (userRepo.existsByUsername(username)) return ResponseEntity.status(409).body(Map.of("code","CONFLICT","message","username_exists"));
    User u = new User();
    u.setId(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE));
    u.setUsername(username);
    u.setPasswordHash(passwordService.hash(password));
    u.setCreatedAt(LocalDateTime.now());
    userRepo.save(u);
    String token = jwtService.issueToken(username);
    Map<String,Object> resp = new HashMap<>();
    resp.put("token", token);
    resp.put("ttlSec", jwtService.getExpiresSeconds());
    resp.put("expiresAt", java.time.Instant.now().plusSeconds(jwtService.getExpiresSeconds()).toEpochMilli());
    return ResponseEntity.ok(resp);
  }

  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
    String username = body.get("username");
    String password = body.get("password");
    if (username == null || username.isBlank() || password == null || password.isBlank())
      return ResponseEntity.badRequest().body(Map.of("code","VALIDATION_ERROR","message","username_and_password_required"));
    User u = userRepo.findByUsername(username).orElse(null);
    if (u == null || !passwordService.matches(password, u.getPasswordHash()))
      return ResponseEntity.status(401).body(Map.of("code","UNAUTHORIZED","message","invalid_credentials"));
    String token = jwtService.issueToken(username);
    Map<String,Object> resp = new HashMap<>();
    resp.put("token", token);
    resp.put("ttlSec", jwtService.getExpiresSeconds());
    resp.put("expiresAt", java.time.Instant.now().plusSeconds(jwtService.getExpiresSeconds()).toEpochMilli());
    return ResponseEntity.ok(resp);
  }

  @PostMapping("/sendCode")
  public ResponseEntity<Map<String, Object>> deprecatedSendCode() {
    return ResponseEntity.status(404).body(Map.of("code","NOT_FOUND","message","deprecated"));
  }

  @PostMapping("/verify")
  public ResponseEntity<Map<String, Object>> deprecatedVerify() {
    return ResponseEntity.status(404).body(Map.of("code","NOT_FOUND","message","deprecated"));
  }
}
