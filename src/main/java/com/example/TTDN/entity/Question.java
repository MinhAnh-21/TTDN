package com.example.TTDN.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_questions")
    private Long idQuestions;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "question_type", length = 45)
    private String questionType;

    @Column(name = "difficulty_level", length = 45)
    private String difficultyLevel;

    @Column(name = "courses_id_courses")
    private Long courseId;

    @Column(name = "teacher_profiles_id_teacher_profiles")
    private Long teacherProfileId;

    // Liên kết với bảng exams
    @Column(name = "exams_id_exams")
    private Long examId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}