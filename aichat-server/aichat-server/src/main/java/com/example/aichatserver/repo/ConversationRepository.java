package com.example.aichatserver.repo;

import com.example.aichatserver.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
  List<Conversation> findByUserIdOrderByUpdatedAtDesc(Long userId);
  Page<Conversation> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);
}
