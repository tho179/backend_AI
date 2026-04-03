package com.nutrisnap.backend.repository;

import com.nutrisnap.backend.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettingRepository extends JpaRepository<Setting, String> {
    // Tự động viết câu lệnh: SELECT * FROM tblSetting WHERE user_id = ?
    Optional<Setting> findByUserId(String userId);
}