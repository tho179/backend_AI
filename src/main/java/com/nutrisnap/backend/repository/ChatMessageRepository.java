package com.nutrisnap.backend.repository;

import com.nutrisnap.backend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    // Lấy toàn bộ tin nhắn trong 1 phiên chat, sắp xếp theo thứ tự thời gian tăng dần (cũ đến mới)
    List<ChatMessage> findByTblChatSessionIdOrderByTimestampAsc(String tblChatSessionId);
}