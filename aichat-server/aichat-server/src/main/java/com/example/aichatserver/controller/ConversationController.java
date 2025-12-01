package com.example.aichatserver.controller;

import com.example.aichatserver.domain.Conversation;
import com.example.aichatserver.dto.CreateConversationRequest;
import com.example.aichatserver.repo.ConversationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/conversations")
public class ConversationController {
  private final ConversationRepository repo;

  public ConversationController(ConversationRepository repo) { this.repo = repo; }

  @PostMapping
  public ResponseEntity<Map<String, Long>> create(@Validated @RequestBody CreateConversationRequest req) {
    Conversation c = new Conversation();
    c.setId(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE));
    c.setUserId(1L);
    c.setTitle(req.getTitle());
    c.setStatus((short)1);
    LocalDateTime now = LocalDateTime.now();
    c.setCreatedAt(now);
    c.setUpdatedAt(now);
    repo.save(c);
    return ResponseEntity.ok(Map.of("conversation_id", c.getId()));
  }
}