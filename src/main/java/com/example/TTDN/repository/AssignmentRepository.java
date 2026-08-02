package com.example.TTDN.repository;

import com.example.TTDN.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    // Tìm kiếm danh sách bài tập theo ID của lớp học
    List<Assignment> findByClassesId(Long classesId);
}