package com.nutrisnap.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

// ĐỔI CHỮ javax THÀNH jakarta Ở DÒNG NÀY:
import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() throws Exception {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccount = this.getClass().getClassLoader().getResourceAsStream("firebase-config.json");

            if (serviceAccount == null) {
                throw new RuntimeException("❌ LỖI NGHIÊM TRỌNG: Không tìm thấy file firebase-config.json trong thư mục resources!");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("✅ KHỞI TẠO FIREBASE ADMIN SDK THÀNH CÔNG! SẴN SÀNG KẾT NỐI.");
        }
    }
}