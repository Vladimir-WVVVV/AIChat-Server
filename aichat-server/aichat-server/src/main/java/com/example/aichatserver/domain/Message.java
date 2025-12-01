package com.example.aichatserver.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
  @Id
  private Long id;

  @Column(name = "conversation_id", nullable = false)
  private Long conversationId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private Role role;

  @Lob
  @Column(nullable = false)
  private String content;

  @Column(nullable = false)
  private Integer idx;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  public enum Role { user, assistant, system }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getConversationId() { return conversationId; }
  public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
  public Role getRole() { return role; }
  public void setRole(Role role) { this.role = role; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public Integer getIdx() { return idx; }
  public void setIdx(Integer idx) { this.idx = idx; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}