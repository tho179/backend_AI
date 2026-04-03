package com.nutrisnap.backend.controller;

import com.nutrisnap.backend.entity.DeviceToken;
import com.nutrisnap.backend.repository.DeviceTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device-token")
@CrossOrigin(origins = "*")
public class DeviceTokenController {

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    @PostMapping("/save")
    public DeviceToken saveToken(@RequestBody DeviceToken deviceToken) {
        // Kiểm tra xem User này đã có token chưa, có rồi thì cập nhật, chưa thì tạo mới
        DeviceToken existingToken = deviceTokenRepository.findByTblUserId(deviceToken.getTblUserId()).orElse(null);
        if (existingToken != null) {
            existingToken.setToken(deviceToken.getToken());
            existingToken.setPlatform(deviceToken.getPlatform());
            return deviceTokenRepository.save(existingToken);
        }
        return deviceTokenRepository.save(deviceToken);
    }
}