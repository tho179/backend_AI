package com.nutrisnap.backend.dto;

import lombok.Data;

@Data
public class ChatRequestDTO {
    private String userId;
    private String sessionId; // Có thể null nếu là phiên chat mới
    private String message;   // Câu hỏi của người dùng
}