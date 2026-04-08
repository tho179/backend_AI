package com.nutrisnap.backend.model;

import lombok.Data;

@Data // Vẫn giữ Lombok để tự tạo Getter/Setter
public class SettingModel {
    private String userId;
    private boolean waterReminderEnabled; // Lưu ý: bỏ tiền tố "is" ở tên biến đi cho dễ mapping JSON
    private int waterReminderInterval;
    private String mealReminderTime; // Đổi java.sql.Time thành String (ví dụ "12:30") để lưu lên Firebase dễ hơn
}