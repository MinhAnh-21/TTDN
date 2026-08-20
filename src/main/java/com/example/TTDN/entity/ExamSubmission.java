package com.example.TTDN.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_exam_submissions")
    private Long idExamSubmissions;

    @Column(name = "exams_id")
    private Long examId; // Lưu trực tiếp ID bài thi

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "submit_time")
    private LocalDateTime submitTime;

    @Column(name = "tab_switch_count")
    private Integer tabSwitchCount;

    @Column(name = "student_answers", columnDefinition = "TEXT")
    private String studentAnswers;

    @Column(name = "score")
    private Double score;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "classes_has_exams_id")
    private Long classesHasExamsId;

    @Column(name = "student_profiles_id_student_profiles")
    private Long studentProfileId;
}