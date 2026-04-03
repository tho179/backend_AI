package com.nutrisnap.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.InputStream;

@Configuration // Bắt buộc phải có chữ này để Spring Boot nhận diện
public class FirebaseConfig {

    @PostConstruct
    public void initialize() throws Exception {
        // Chỉ khởi tạo nếu chưa có App nào chạy
        if (FirebaseApp.getApps().isEmpty()) {

            // Tìm file chìa khóa trong thư mục resources
            InputStream serviceAccount = this.getClass().getClassLoader().getResourceAsStream("firebase-config.json");

            // NẾU TÌM KHÔNG THẤY FILE, ÉP SERVER BÁO LỖI ĐỎ VÀ DỪNG LẠI NGAY LẬP TỨC
            if (serviceAccount == null) {
                throw new RuntimeException("❌ LỖI NGHIÊM TRỌNG: Không tìm thấy file firebase-config.json trong thư mục resources! Hãy kiểm tra lại tên file.");
            }

            // Tiến hành mở khóa Firebase
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("✅ KHỞI TẠO FIREBASE ADMIN SDK THÀNH CÔNG! SẴN SÀNG KẾT NỐI.");
        }
    }
}