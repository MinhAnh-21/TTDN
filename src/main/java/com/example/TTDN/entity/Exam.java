package com.example.TTDN.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_exams")
    private Long idExams;

    @Column(name = "exam_name", nullable = false, length = 255)
    private String examName;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "status", nullable = false, length = 45)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "courses_id_courses", nullable = false)
    private Long courseId;

    @Column(name = "teacher_profiles_id_teacher_profiles")
    private Long teacherProfileId;

    // Ánh xạ thêm cột khóa ngoại thứ hai đang bắt buộc trong CSDL
    @Column(name = "teacher_profiles_id_teacher")
    private Long teacherProfileIdAlt;
}