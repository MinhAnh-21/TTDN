package com.example.TTDN.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignment_submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_assignment_submissions")
    private Long idAssignmentSubmissions;

    @Column(name = "submitted_file", nullable = false, length = 255)
    private String submittedFile;

    @Column(name = "student_note", columnDefinition = "TEXT")
    private String studentNote;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "grade", columnDefinition = "DECIMAL(4,2)")
    private Double grade;

    @Column(name = "teacher_feedback", columnDefinition = "TEXT")
    private String teacherFeedback;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @Column(name = "assignments_id_assignments", nullable = false)
    private Long assignmentId;

    @Column(name = "student_profiles_id_student_profiles", nullable = false)
    private Long studentProfileId;
}