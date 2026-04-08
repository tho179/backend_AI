package com.nutrisnap.backend.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.MulticastMessage;
import java.util.List;

@Service
public class NotificationService {

    public void sendPushNotification(String deviceToken, String title, String body) {
        try {
            // Tạo một gói thông báo
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // Nhắm chuẩn xác vào thiết bị Android thông qua Token
            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(notification)
                    .build();

            // Gửi đi bằng sức mạnh của Firebase
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("✅ Đã gửi thông báo thành công: " + response);

        } catch (Exception e) {
            System.err.println("❌ Lỗi gửi thông báo: " + e.getMessage());
        }
    }
    public void sendMulticastNotification(List<String> deviceTokens, String title, String body) {
        if (deviceTokens == null || deviceTokens.isEmpty()) return;

        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // Nhét tất cả Token vào chung một gói (Firebase hỗ trợ tối đa 500 token/lần)
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(deviceTokens)
                    .setNotification(notification)
                    .build();

            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            System.out.println("✅ Đã gửi thành công " + response.getSuccessCount() + " thông báo!");
        } catch (Exception e) {
            System.err.println("❌ Lỗi gửi thông báo hàng loạt: " + e.getMessage());
        }
    }
}