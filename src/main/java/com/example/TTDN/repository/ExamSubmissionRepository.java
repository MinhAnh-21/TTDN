package com.example.TTDN.repository;

import com.example.TTDN.entity.ExamSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamSubmissionRepository extends JpaRepository<ExamSubmission, Long> {
}