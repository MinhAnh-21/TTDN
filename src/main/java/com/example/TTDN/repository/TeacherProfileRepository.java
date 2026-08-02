package com.example.TTDN.repository;

import com.example.TTDN.entity.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, Long> {

    // Tìm hồ sơ Giảng viên theo ID người dùng (UserId)
    Optional<TeacherProfile> findByUserId(Long userId);
}