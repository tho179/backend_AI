package com.nutrisnap.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.sql.Time;

@Data // Tự động tạo Getter, Setter nhờ thư viện Lombok
@Entity
@Table(name = "tblSetting")
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "tbl_user_id", nullable = false)
    private String userId;

    private boolean isWaterReminderEnabled;
    private int waterReminderInterval; // Tính bằng phút
    private Time mealReminderTime;
}