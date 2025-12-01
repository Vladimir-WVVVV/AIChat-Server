package com.example.aichatserver.controller;

import com.example.aichatserver.domain.Message;
import com.example.aichatserver.dto.CreateMessageRequest;
import com.example.aichatserver.repo.MessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/messages")
public class MessageController {
  private final MessageRepository repo;

  public MessageController(MessageRepository repo) { this.repo = repo; }

  @PostMapping
  public ResponseEntity<Map<String, Long>> create(@Validated @RequestBody CreateMessageRequest req) {
    int nextIdx = repo.findTopByConversationIdOrderByIdxDesc(req.getConversationId())
        .map(Message::getIdx).map(i -> i + 1).orElse(1);

    Message m = new Message();
    m.setId(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE));
    m.setConversationId(req.getConversationId());
    m.setRole(Message.Role.valueOf(req.getRole()));
    m.setContent(req.getContent());
    m.setIdx(nextIdx);
    m.setCreatedAt(LocalDateTime.now());
    repo.save(m);
    return ResponseEntity.ok(Map.of("message_id", m.getId()));
  }

  @GetMapping("/{conversationId}")
  public ResponseEntity<List<Message>> list(@PathVariable Long conversationId) {
    return ResponseEntity.ok(repo.findByConversationIdOrderByIdxAsc(conversationId));
  }
}