package com.nutrisnap.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tblChatSession")
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "tbl_user_id", nullable = false)
    private String tblUserId;

    // Thời gian bắt đầu phiên chat (như thiết kế "startAt" trong PDF)
    private LocalDateTime startAt;
}