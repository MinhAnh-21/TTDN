package com.example.TTDN.repository;

import com.example.TTDN.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    // Tìm hồ sơ Sinh viên theo ID người dùng (UserId)
    Optional<StudentProfile> findByUserId(Long userId);
}