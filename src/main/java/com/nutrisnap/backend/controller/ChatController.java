package com.nutrisnap.backend.controller;

import com.nutrisnap.backend.dto.ChatRequestDTO;
import com.nutrisnap.backend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // API mới chạy cơ chế Streaming chữ hiện ra dần dần
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamMessage(@RequestBody ChatRequestDTO request) {
        return chatService.streamUserMessage(request);
    }
}