package com.nutrisnap.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tblDeviceToken")
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String token;

    private String platform; // Ví dụ: "android" hoặc "ios"

    @Column(name = "tbl_user_id", nullable = false)
    private String tblUserId;
}