package com.example.TTDN.repository;

import com.example.TTDN.entity.ClassStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassStudentRepository extends JpaRepository<ClassStudent, Long> {
    List<ClassStudent> findByClassesId(Long classesId);

    // Thêm phương thức tìm danh sách liên kết lớp dựa theo ID hồ sơ học sinh
    List<ClassStudent> findByStudentProfileId(Long studentProfileId);

    boolean existsByClassesIdAndStudentProfileId(Long classesId, Long studentProfileId);
    void deleteByClassesIdAndStudentProfileId(Long classesId, Long studentProfileId);
    void deleteByClassesId(Long classesId);
}