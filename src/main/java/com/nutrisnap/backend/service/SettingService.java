package com.nutrisnap.backend.service;

import com.nutrisnap.backend.entity.Setting;
import com.nutrisnap.backend.repository.SettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingService {

    @Autowired
    private SettingRepository settingRepository;

    // Lấy cấu hình của User, nếu chưa có thì tạo mặc định
    public Setting getSettingByUserId(String userId) {
        return settingRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Setting defaultSetting = new Setting();
                    defaultSetting.setUserId(userId);
                    defaultSetting.setWaterReminderEnabled(true);
                    defaultSetting.setWaterReminderInterval(120); // Mặc định 120 phút
                    return settingRepository.save(defaultSetting);
                });
    }

    // Cập nhật cấu hình mới
    public Setting updateSetting(Setting setting) {
        return settingRepository.save(setting);
    }
}