package com.example.TTDN.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_student_answers")
    private Long idStudentAnswers;

    @Column(name = "exam_submissions_id_exam_submissions", nullable = false)
    private Long submissionId;

    @Column(name = "questions_id_questions", nullable = false)
    private Long questionId;

    @Column(name = "selected_option_id")
    private Long selectedOptionId;

    @Column(name = "is_correct")
    private Boolean isCorrect;
}