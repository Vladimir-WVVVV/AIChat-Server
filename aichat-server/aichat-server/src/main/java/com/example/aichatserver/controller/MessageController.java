package com.example.aichatserver.controller;

import com.example.aichatserver.domain.Message;
import com.example.aichatserver.dto.CreateMessageRequest;
import com.example.aichatserver.repo.MessageRepository;
import com.example.aichatserver.repo.ConversationRepository;
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
  private final ConversationRepository convRepo;

  public MessageController(MessageRepository repo, ConversationRepository convRepo) { this.repo = repo; this.convRepo = convRepo; }

  @PostMapping
  public ResponseEntity<Map<String, Long>> create(@Validated @RequestBody CreateMessageRequest req) {
    String subject = String.valueOf(org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()
        .getAttribute("subject", org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST));
    java.util.zip.CRC32 crc = new java.util.zip.CRC32();
    crc.update(subject.getBytes());
    long userId = crc.getValue();
    com.example.aichatserver.domain.Conversation conv = convRepo.findById(req.getConversationId()).orElse(null);
    if (conv == null || !conv.getUserId().equals(userId)) return ResponseEntity.status(403).body(Map.of("error", -1L));
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
  public ResponseEntity<List<Map<String, Object>>> list(@PathVariable Long conversationId,
                                           @RequestParam(required = false, defaultValue = "0") int page,
                                           @RequestParam(required = false, defaultValue = "50") int size) {
    String subject = String.valueOf(org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()
        .getAttribute("subject", org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST));
    java.util.zip.CRC32 crc = new java.util.zip.CRC32();
    crc.update(subject.getBytes());
    long userId = crc.getValue();
    com.example.aichatserver.domain.Conversation conv = convRepo.findById(conversationId).orElse(null);
    if (conv == null || !conv.getUserId().equals(userId)) return ResponseEntity.status(403).body(java.util.List.of(java.util.Map.of("code","FORBIDDEN","message","no_access")));
    org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
    List<Message> list = repo.findByConversationIdOrderByIdxAsc(conversationId, pageable).getContent();
    List<Map<String, Object>> out = new java.util.ArrayList<>();
    for (Message m : list) {
      java.util.HashMap<String, Object> item = new java.util.HashMap<>();
      item.put("id", String.valueOf(m.getId()));
      item.put("content", m.getContent());
      item.put("isUser", m.getRole() == Message.Role.user);
      long ts = m.getCreatedAt() == null ? 0L : m.getCreatedAt().atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli();
      item.put("timestamp", ts);
      item.put("conversationId", m.getConversationId());
      out.add(item);
    }
    return ResponseEntity.ok(out);
  }

  
}
