package com.nutrisnap.backend.model;

import lombok.Data;

@Data
public class DeviceTokenModel {
    private String tblUserId; // Có thể đổi thành userId cho gọn nếu muốn
    private String token;
    private String platform;
}