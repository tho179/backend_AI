package com.nutrisnap.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.nutrisnap.backend.dto.ChatRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {

    // Lấy config của Cloudflare
    @Value("${cloudflare.account.id}")
    private String cfAccountId;
    @Value("${cloudflare.api.token}")
    private String cfApiToken;
    @Value("${cloudflare.api.model}")
    private String cfApiModel;

    private final WebClient webClient = WebClient.builder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, List<Map<String, String>>> chatCache = new ConcurrentHashMap<>();
    private static final int MAX_HISTORY = 10;

    public Flux<String> streamUserMessage(ChatRequestDTO request) {
        Firestore db = FirestoreClient.getFirestore();
        String sessionId = request.getSessionId();

        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("userId", request.getUserId());
            sessionData.put("startAt", System.currentTimeMillis());
            db.collection("chat_sessions").document(sessionId).set(sessionData);
        }
        final String finalSessionId = sessionId;

        saveToFirebaseAsync(finalSessionId, "user", request.getMessage());

        List<Map<String, String>> history = chatCache.getOrDefault(finalSessionId, new ArrayList<>());
        if (history.isEmpty()) {
            String systemPrompt = "MANDATORY RULE: You MUST answer entirely in Vietnamese. DO NOT use English under any circumstances.\n" +
                    "Bạn là NutriSnap, chuyên gia dinh dưỡng ảo. " +
                    "Chỉ được phép giao tiếp bằng Tiếng Việt. " +
                    "Hãy trình bày câu trả lời dưới dạng danh sách gạch đầu dòng (sử dụng dấu - ) cho dễ đọc. " +
                    "Luôn dùng emoji phù hợp với các loại thức ăn.";
            history.add(Map.of("role", "system", "content", systemPrompt));
        }
        history.add(Map.of("role", "user", "content", request.getMessage()));

        if (history.size() > MAX_HISTORY + 1) {
            List<Map<String, String>> trimmedHistory = new ArrayList<>();
            trimmedHistory.add(history.get(0));
            trimmedHistory.addAll(history.subList(history.size() - MAX_HISTORY, history.size()));
            history = trimmedHistory;
        }

        chatCache.put(finalSessionId, history);
        final List<Map<String, String>> finalHistory = history;

        // URL gọi Cloudflare AI
        String apiUrl = "https://api.cloudflare.com/client/v4/accounts/" + cfAccountId + "/ai/run/" + cfApiModel;

        Map<String, Object> body = new HashMap<>();
        body.put("messages", history);
        body.put("stream", true);

        StringBuilder aiFullResponse = new StringBuilder();

        return webClient.post()
                .uri(apiUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + cfApiToken)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .map(this::extractContentFromStream) // Cắt lấy chữ từ Cloudflare
                .filter(text -> !text.isEmpty())
                .doOnNext(aiFullResponse::append)
                .map(text -> {
                    try {
                        java.util.Map<String, String> dataMap = new java.util.HashMap<>();
                        // Đóng gói lại thành key "text" để App Android cũ không cần sửa code
                        dataMap.put("text", text);
                        return objectMapper.writeValueAsString(dataMap);
                    } catch (Exception e) {
                        return "{\"text\":\"\"}";
                    }
                })
                .doOnComplete(() -> {
                    String finalAiText = aiFullResponse.toString();
                    finalHistory.add(Map.of("role", "assistant", "content", finalAiText));
                    chatCache.put(finalSessionId, finalHistory);
                    saveToFirebaseAsync(finalSessionId, "assistant", finalAiText);
                });
    }

    private void saveToFirebaseAsync(String sessionId, String role, String content) {
        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> msg = new HashMap<>();
        msg.put("role", role);
        msg.put("content", content);
        msg.put("timestamp", System.currentTimeMillis());
        db.collection("chat_sessions").document(sessionId).collection("messages").add(msg);
    }

    // ĐÃ SỬA: Hàm bóc tách chữ từ định dạng của Cloudflare
    private String extractContentFromStream(String chunk) {
        if (chunk == null || chunk.isEmpty()) return "";

        try {
            if (chunk.startsWith("data: ")) {
                chunk = chunk.substring(6).trim();
            }

            // Cloudflare trả về [DONE] khi kết thúc stream
            if (chunk.equals("[DONE]")) return "";

            JsonNode rootNode = objectMapper.readTree(chunk);
            // Key chữ của Cloudflare nằm ở root, tên là "response"
            JsonNode responseNode = rootNode.at("/response");
            if (!responseNode.isMissingNode()) {
                return responseNode.asText();
            }
        } catch (Exception e) {
            // Bỏ qua các mảnh JSON bị lỗi do cắt nửa mạng
        }
        return "";
    }
}