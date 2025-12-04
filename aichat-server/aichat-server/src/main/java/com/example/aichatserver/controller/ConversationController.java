package com.example.aichatserver.controller;

import com.example.aichatserver.domain.Conversation;
import com.example.aichatserver.dto.CreateConversationRequest;
import com.example.aichatserver.repo.ConversationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.example.aichatserver.domain.Message;
import com.example.aichatserver.repo.MessageRepository;

@RestController
@RequestMapping("/conversations")
public class ConversationController {
  private final ConversationRepository repo;
  private final MessageRepository messageRepo;

  public ConversationController(ConversationRepository repo, MessageRepository messageRepo) {
    this.repo = repo;
    this.messageRepo = messageRepo;
  }

  @PostMapping
  public ResponseEntity<Map<String, Long>> create(@Validated @RequestBody CreateConversationRequest req) {
    String subject = String.valueOf(org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()
        .getAttribute("subject", org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST));
    java.util.zip.CRC32 crc = new java.util.zip.CRC32();
    crc.update(subject.getBytes());
    long userId = crc.getValue();
    Conversation c = new Conversation();
    c.setId(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE));
    c.setUserId(userId);
    c.setTitle(req.getTitle());
    c.setStatus((short)1);
    LocalDateTime now = LocalDateTime.now();
    c.setCreatedAt(now);
    c.setUpdatedAt(now);
    repo.save(c);
    return ResponseEntity.ok(Map.of("conversation_id", c.getId()));
  }

  @GetMapping
  public ResponseEntity<List<Map<String, Object>>> list(
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "20") int size) {
    String subject = String.valueOf(org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()
        .getAttribute("subject", org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST));
    java.util.zip.CRC32 crc = new java.util.zip.CRC32();
    crc.update(subject.getBytes());
    long userId = crc.getValue();
    org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
    List<Conversation> all = repo.findByUserIdOrderByUpdatedAtDesc(userId, pageable).getContent();
    List<Map<String, Object>> out = all.stream().map(c -> {
      java.util.HashMap<String, Object> m = new java.util.HashMap<>();
      m.put("conversationId", c.getId());
      long latest = c.getUpdatedAt() == null ? 0L : c.getUpdatedAt().atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli();
      m.put("latest", latest);
      return m;
    }).collect(Collectors.toList());
    return ResponseEntity.ok(out);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    String subject = String.valueOf(org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()
        .getAttribute("subject", org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST));
    java.util.zip.CRC32 crc = new java.util.zip.CRC32();
    crc.update(subject.getBytes());
    long userId = crc.getValue();
    Conversation c = repo.findById(id).orElse(null);
    if (c == null || !c.getUserId().equals(userId)) return ResponseEntity.status(404).build();
    repo.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
