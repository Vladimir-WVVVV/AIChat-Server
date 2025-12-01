package com.example.aichatserver.repo;

import com.example.aichatserver.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
  List<Message> findByConversationIdOrderByIdxAsc(Long conversationId);
  Optional<Message> findTopByConversationIdOrderByIdxDesc(Long conversationId);
}