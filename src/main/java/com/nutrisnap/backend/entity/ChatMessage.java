package com.nutrisnap.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tblChatMessage")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Cột này để biết tin nhắn này thuộc về Phiên chat nào
    @Column(name = "tbl_chat_session_id", nullable = false)
    private String tblChatSessionId;

    // role: "user" (người dùng hỏi) hoặc "assistant" (AI trả lời)
    private String role;

    // Nội dung tin nhắn. Dùng TEXT vì tin nhắn AI trả về có thể rất dài
    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime timestamp;
}