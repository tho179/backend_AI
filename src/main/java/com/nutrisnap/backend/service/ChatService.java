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

    @Value("${openrouter.api.url}")
    private String apiUrl;
    @Value("${openrouter.api.key}")
    private String apiKey;
    @Value("${openrouter.api.model}")
    private String apiModel;

    // 1. Dùng WebClient thay cho RestTemplate
    private final WebClient webClient = WebClient.builder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 2. RAM Cache để lưu lịch sử tạm thời (Giảm tải cho Firebase)
    private final Map<String, List<Map<String, String>>> chatCache = new ConcurrentHashMap<>();
    private static final int MAX_HISTORY = 10; // Giới hạn 10 tin nhắn gần nhất

    public Flux<String> streamUserMessage(ChatRequestDTO request) {
        Firestore db = FirestoreClient.getFirestore();
        String sessionId = request.getSessionId();

        // Nếu Android gửi lên sessionId null, ta tự tạo và thông báo lưu session mới
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("userId", request.getUserId());
            sessionData.put("startAt", System.currentTimeMillis());
            db.collection("chat_sessions").document(sessionId).set(sessionData); // Lưu bất đồng bộ
        }
        final String finalSessionId = sessionId;

        // Lưu tin nhắn User vào Firebase dưới nền (không dùng .get() để tránh bị block)
        saveToFirebaseAsync(finalSessionId, "user", request.getMessage());

        // 3. Xử lý Lịch sử (Context Window & Cache)
        List<Map<String, String>> history = chatCache.getOrDefault(finalSessionId, new ArrayList<>());
        if (history.isEmpty()) {
            history.add(Map.of("role", "system", "content", "Bạn là NutriSnap, chuyên gia dinh dưỡng ảo. Trả lời ngắn gọn, bằng tiếng Việt."));
        }
        history.add(Map.of("role", "user", "content", request.getMessage()));

        // Thu gọn lịch sử nếu vượt quá MAX_HISTORY (Giữ lại câu System đầu tiên)
        if (history.size() > MAX_HISTORY + 1) {
            List<Map<String, String>> trimmedHistory = new ArrayList<>();
            trimmedHistory.add(history.get(0)); // Giữ system prompt
            trimmedHistory.addAll(history.subList(history.size() - MAX_HISTORY, history.size()));
            history = trimmedHistory;
        }

        // Cập nhật lại Cache
        chatCache.put(finalSessionId, history);
        final List<Map<String, String>> finalHistory = history;

        // 4. Gọi API OpenRouter dưới dạng Streaming
        Map<String, Object> body = new HashMap<>();
        body.put("model", apiModel);
        body.put("messages", history);
        body.put("stream", true); // BẮT BUỘC ĐỂ TRẢ VỀ THEO DÒNG

        StringBuilder aiFullResponse = new StringBuilder();

        return webClient.post()
                .uri(apiUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .header("HTTP-Referer", "http://localhost:8080")
                .header("X-Title", "NutriSnap")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class) // Lấy dữ liệu từng luồng một
                .map(this::extractContentFromStream) // Cắt lấy chữ từ JSON trả về
                .filter(text -> !text.isEmpty())
                .doOnNext(aiFullResponse::append) // Gom dần thành câu trả lời hoàn chỉnh
                .map(text -> {
                    try {
                        java.util.Map<String, String> dataMap = new java.util.HashMap<>();
                        dataMap.put("text", text);
                        return objectMapper.writeValueAsString(dataMap);
                    } catch (Exception e) {
                        return "{\"text\":\"\"}";
                    }
                })
                .doOnComplete(() -> {
                    // KHI AI TRẢ LỜI XONG HOÀN TOÀN
                    String finalAiText = aiFullResponse.toString();

                    // Cập nhật câu trả lời vào RAM Cache
                    finalHistory.add(Map.of("role", "assistant", "content", finalAiText));
                    chatCache.put(finalSessionId, finalHistory);

                    // Lưu câu trả lời hoàn chỉnh vào Firebase (Chạy ngầm)
                    saveToFirebaseAsync(finalSessionId, "assistant", finalAiText);
                });
    }

    // Hàm lưu Firebase chạy ngầm (Fire-and-forget)
    private void saveToFirebaseAsync(String sessionId, String role, String content) {
        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> msg = new HashMap<>();
        msg.put("role", role);
        msg.put("content", content);
        msg.put("timestamp", System.currentTimeMillis());
        // Lệnh add() không gọi .get() sẽ tự động chạy trong luồng (thread) riêng của Firebase SDK
        db.collection("chat_sessions").document(sessionId).collection("messages").add(msg);
    }

    // Hàm bóc tách chữ từ định dạng SSE của OpenRouter
    private String extractContentFromStream(String chunk) {
        if (chunk.equals("[DONE]")) return "";
        try {
            // OpenRouter gửi chuỗi bắt đầu bằng "data: "
            if (chunk.startsWith("data: ")) {
                chunk = chunk.substring(6);
            }
            JsonNode rootNode = objectMapper.readTree(chunk);
            JsonNode contentNode = rootNode.at("/choices/0/delta/content");
            if (!contentNode.isMissingNode()) {
                return contentNode.asText();
            }
        } catch (Exception e) {
            // Bỏ qua các mảnh JSON bị cắt nửa mạng
        }
        return "";
    }
}