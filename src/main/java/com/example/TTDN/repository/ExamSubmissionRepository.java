package com.example.TTDN.repository;

import com.example.TTDN.entity.ExamSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExamSubmissionRepository extends JpaRepository<ExamSubmission, Long> {

    // Dùng JPQL chuẩn thay cho Native SQL để tránh lỗi ClassCastException Long -> Boolean
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM ExamSubmission s WHERE s.examId = :examId AND s.studentProfileId = :studentProfileId")
    boolean existsByExamIdAndStudentProfileId(@Param("examId") Long examId, @Param("studentProfileId") Long studentProfileId);

    // Lấy bài nộp để lấy điểm số
    Optional<ExamSubmission> findTopByExamIdAndStudentProfileIdOrderByIdExamSubmissionsDesc(Long examId, Long studentProfileId);
}