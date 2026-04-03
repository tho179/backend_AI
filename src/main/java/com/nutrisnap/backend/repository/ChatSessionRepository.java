package com.nutrisnap.backend.repository;

import com.nutrisnap.backend.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {
    // Lấy toàn bộ các phiên chat của 1 user, sắp xếp từ mới nhất đến cũ nhất
    List<ChatSession> findByTblUserIdOrderByStartAtDesc(String tblUserId);
}