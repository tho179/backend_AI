package com.nutrisnap.backend.controller;

import com.nutrisnap.backend.entity.Setting;
import com.nutrisnap.backend.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*") // Cho phép các thiết bị khác gọi vào
public class SettingController {

    @Autowired
    private SettingService settingService;

    // API 1: Android gọi GET /api/settings/{userId} để lấy cấu hình
    @GetMapping("/{userId}")
    public Setting getSetting(@PathVariable String userId) {
        return settingService.getSettingByUserId(userId);
    }

    // API 2: Android gọi POST /api/settings/update để lưu cấu hình mới
    @PostMapping("/update")
    public Setting updateSetting(@RequestBody Setting setting) {
        return settingService.updateSetting(setting);
    }
}