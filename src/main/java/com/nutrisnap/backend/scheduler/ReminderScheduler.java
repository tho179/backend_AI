package com.nutrisnap.backend.scheduler;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.nutrisnap.backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReminderScheduler {

    @Autowired
    private NotificationService notificationService;

    // Biểu thức Cron: "0 0 8-22 * * ?" nghĩa là chạy vào phút thứ 0, giây thứ 0, mỗi tiếng 1 lần từ 8h sáng đến 10h tối.
    @Scheduled(cron = "0 0 8-22 * * ?")
    public void checkAndSendWaterReminders() {
        System.out.println("⏳ [" + LocalTime.now() + "] Đang quét Firebase tìm người cần nhắc uống nước...");

        Firestore db = FirestoreClient.getFirestore();
        List<String> tokens = new ArrayList<>();

        try {
            // 1. Tìm tất cả user có bật nhắc nhở uống nước trong collection "settings"
            ApiFuture<QuerySnapshot> settingsFuture = db.collection("settings")
                    .whereEqualTo("waterReminderEnabled", true)
                    .get();

            // ĐÃ SỬA LỖI: Sử dụng QueryDocumentSnapshot ở đây
            List<QueryDocumentSnapshot> settingDocs = settingsFuture.get().getDocuments();

            if (settingDocs.isEmpty()) {
                System.out.println("Chưa có user nào bật tính năng nhắc uống nước.");
                return;
            }

            // 2. Lấy danh sách userId cần nhắc (Đã xóa phần code bị lặp)
            List<String> userIds = new ArrayList<>();
            for (QueryDocumentSnapshot doc : settingDocs) {
                userIds.add(doc.getId());
            }

            // 3. Lấy Device Token của những user này từ collection "device_tokens"
            for (String userId : userIds) {
                DocumentSnapshot tokenDoc = db.collection("device_tokens").document(userId).get().get();
                if (tokenDoc.exists()) {
                    String token = tokenDoc.getString("token");
                    if (token != null && !token.isEmpty()) {
                        tokens.add(token);
                    }
                }
            }

            // 4. Gửi thông báo hàng loạt nếu có dữ liệu
            if (!tokens.isEmpty()) {
                System.out.println("💧 Tìm thấy " + tokens.size() + " thiết bị bật thông báo. Đang tiến hành gửi...");
                notificationService.sendMulticastNotification(
                        tokens,
                        "Đến giờ uống nước rồi! 💧",
                        "NutriSnap nhắc bạn uống một cốc nước để giữ gìn sức khỏe và vóc dáng nhé."
                );
            } else {
                System.out.println("Không có thiết bị (Token) nào hợp lệ để gửi.");
            }

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi quét Firebase trong Scheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }
}