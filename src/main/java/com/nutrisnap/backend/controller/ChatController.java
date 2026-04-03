package com.nutrisnap.backend.controller;

import com.nutrisnap.backend.dto.ChatRequestDTO;
import com.nutrisnap.backend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/message")
    public ResponseEntity<Map<String, String>> sendMessage(@RequestBody ChatRequestDTO request) {
        // Truyền cả object responseBody vào Service để vừa lấy câu trả lời, vừa lấy SessionId
        Map<String, String> response = chatService.processUserMessage(request);
        return ResponseEntity.ok(response);
    }
}