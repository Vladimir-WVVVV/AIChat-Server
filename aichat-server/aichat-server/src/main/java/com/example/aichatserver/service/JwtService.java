package com.example.aichatserver.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
  private final SecretKey secretKey;
  private final long expiresSeconds;

  public JwtService(@Value("${security.jwt.secret}") String secret,
                    @Value("${security.jwt.expiresSeconds:0}") long expiresSeconds,
                    @Value("${security.jwt.expiresMinutes:1440}") long expiresMinutes) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    this.expiresSeconds = expiresSeconds > 0 ? expiresSeconds : expiresMinutes * 60;
  }

  public String issueToken(String subject) {
    Instant now = Instant.now();
    return Jwts.builder()
        .setSubject(subject)
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plusSeconds(expiresSeconds)))
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public String verifyToken(String token) {
    try {
      return Jwts.parserBuilder().setSigningKey(secretKey).build()
          .parseClaimsJws(token)
          .getBody()
          .getSubject();
    } catch (Exception e) {
      return null;
    }
  }

  public long getExpiresSeconds() { return expiresSeconds; }
}
