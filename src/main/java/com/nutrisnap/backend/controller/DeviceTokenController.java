package com.nutrisnap.backend.controller;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.nutrisnap.backend.model.DeviceTokenModel;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device-token")
@CrossOrigin(origins = "*")
public class DeviceTokenController {

    @PostMapping("/save")
    public DeviceTokenModel saveToken(@RequestBody DeviceTokenModel deviceToken) {
        Firestore db = FirestoreClient.getFirestore();

        // Lưu thiết bị vào collection "device_tokens" với tên document là userId
        // Hàm set() sẽ tự động: Chưa có thì tạo mới, có rồi thì cập nhật đè lên
        db.collection("device_tokens")
                .document(deviceToken.getTblUserId())
                .set(deviceToken);

        return deviceToken;
    }
}