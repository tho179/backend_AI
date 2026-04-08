package com.nutrisnap.backend.controller;

// Thêm dòng import Model mới của bạn vào đây
import com.nutrisnap.backend.model.SettingModel;
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
    public SettingModel getSetting(@PathVariable String userId) { // Đổi Setting -> SettingModel
        return settingService.getSettingByUserId(userId);
    }

    // API 2: Android gọi POST /api/settings/update để lưu cấu hình mới
    @PostMapping("/update")
    public SettingModel updateSetting(@RequestBody SettingModel setting) { // Đổi Setting -> SettingModel
        return settingService.updateSetting(setting);
    }
}