package com.example.TTDN.repository;

import com.example.TTDN.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, Long> {

    // Tìm kiếm lớp học theo mã tham gia (enrollCode)
    Optional<ClassEntity> findByEnrollCode(String enrollCode);

    // Lấy danh sách lớp học theo ID khóa học (courseId)
    List<ClassEntity> findByCourseId(Long courseId);
}