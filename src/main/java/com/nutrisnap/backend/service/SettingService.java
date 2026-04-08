package com.nutrisnap.backend.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.nutrisnap.backend.model.SettingModel;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class SettingService {

    // Lấy cấu hình của User, nếu chưa có thì tạo mặc định
    public SettingModel getSettingByUserId(String userId) {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection("settings").document(userId); // Dùng userId làm ID

        try {
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get(); // Lấy dữ liệu (chờ xử lý)

            if (document.exists()) {
                // Nếu đã có cấu hình, map thẳng từ Firebase sang Object
                return document.toObject(SettingModel.class);
            } else {
                // Nếu chưa có, tạo cấu hình mặc định và lưu lên Firebase
                SettingModel defaultSetting = new SettingModel();
                defaultSetting.setUserId(userId);
                defaultSetting.setWaterReminderEnabled(true);
                defaultSetting.setWaterReminderInterval(120);
                defaultSetting.setMealReminderTime("12:00");

                docRef.set(defaultSetting); // Ghi đè lên Firebase
                return defaultSetting;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi đọc Firestore: " + e.getMessage());
        }
    }

    // Cập nhật cấu hình mới
    public SettingModel updateSetting(SettingModel setting) {
        Firestore db = FirestoreClient.getFirestore();
        // Lấy document theo userId và ghi đè dữ liệu mới
        DocumentReference docRef = db.collection("settings").document(setting.getUserId());
        docRef.set(setting);
        return setting;
    }
}