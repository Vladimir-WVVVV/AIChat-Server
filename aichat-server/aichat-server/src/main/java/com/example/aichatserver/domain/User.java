package com.example.aichatserver.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = {"username"})})
public class User {
  @Id
  private Long id;
  @Column(nullable = false, length = 64)
  private String username;
  @Column(name = "password_hash", nullable = false, length = 128)
  private String passwordHash;
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }
  public String getPasswordHash() { return passwordHash; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

