package com.nutrisnap.backend.service;

import com.nutrisnap.backend.dto.ChatRequestDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ChatService {

    @Value("${openrouter.api.url}")
    private String apiUrl;
    @Value("${openrouter.api.key}")
    private String apiKey;
    @Value("${openrouter.api.model}")
    private String apiModel;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, String> processUserMessage(ChatRequestDTO request) {
        Firestore db = FirestoreClient.getFirestore();
        String sessionId = request.getSessionId();

        try {
            // 1. Kiểm tra Session cũ hoặc tạo Session mới
            if (sessionId == null || sessionId.isEmpty()) {
                DocumentReference sessionRef = db.collection("chat_sessions").document();
                sessionId = sessionRef.getId(); // Lấy ID tự động phát sinh từ Firebase

                Map<String, Object> sessionData = new HashMap<>();
                sessionData.put("userId", request.getUserId());
                sessionData.put("startAt", System.currentTimeMillis());
                sessionRef.set(sessionData); // Lưu phiên mới lên Firebase
            }

            CollectionReference messagesRef = db.collection("chat_sessions").document(sessionId).collection("messages");

            // 2. Lưu tin nhắn MỚI của User vào Firebase
            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", request.getMessage());
            userMsg.put("timestamp", System.currentTimeMillis());
            messagesRef.add(userMsg);

            // 3. Lôi toàn bộ lịch sử trò chuyện của Session này từ Firebase ra
            List<Map<String, String>> messagesForApi = new ArrayList<>();
            messagesForApi.add(Map.of("role", "system", "content", "Bạn là NutriSnap, chuyên gia dinh dưỡng ảo. Trả lời ngắn gọn, bằng tiếng Việt."));

            // Truy vấn lấy các tin nhắn cũ, sắp xếp theo thời gian tăng dần
            ApiFuture<QuerySnapshot> future = messagesRef.orderBy("timestamp", Query.Direction.ASCENDING).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            for (QueryDocumentSnapshot doc : documents) {
                messagesForApi.add(Map.of(
                        "role", doc.getString("role"),
                        "content", doc.getString("content")
                ));
            }

            // 4. Gọi OpenRouter API (truyền toàn bộ lịch sử vào)
            String aiResponseContent = callOpenRouterApi(messagesForApi);

            // 5. Lưu câu trả lời của AI vào Firebase
            Map<String, Object> aiMsg = new HashMap<>();
            aiMsg.put("role", "assistant");
            aiMsg.put("content", aiResponseContent);
            aiMsg.put("timestamp", System.currentTimeMillis());
            messagesRef.add(aiMsg);

            // 6. Trả kết quả về cho Controller
            Map<String, String> result = new HashMap<>();
            result.put("reply", aiResponseContent);
            result.put("sessionId", sessionId);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("reply", "Lỗi xử lý cơ sở dữ liệu Firebase: " + e.getMessage());
            return error;
        }
    }

    private String callOpenRouterApi(List<Map<String, String>> messages) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("HTTP-Referer", "http://localhost:8080");
        headers.set("X-Title", "NutriSnap");

        Map<String, Object> body = new HashMap<>();
        body.put("model", apiModel);
        body.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            System.err.println("Lỗi gọi API OpenRouter: " + e.getMessage());
            return "Xin lỗi, hệ thống AI đang quá tải. Vui lòng thử lại sau!";
        }
    }
}