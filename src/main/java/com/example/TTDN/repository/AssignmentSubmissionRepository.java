package com.example.TTDN.repository;

import com.example.TTDN.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    // Sửa @RequestParam thành @Param đúng chuẩn Spring Data JPA
    @Query(value = "SELECT * FROM assignment_submissions WHERE assignments_id_assignments = :assignmentId AND student_profiles_id_student_profiles = :userId LIMIT 1", nativeQuery = true)
    Optional<AssignmentSubmission> findSubmissionByCustom(
            @Param("assignmentId") Long assignmentId,
            @Param("userId") Long userId
    );

}