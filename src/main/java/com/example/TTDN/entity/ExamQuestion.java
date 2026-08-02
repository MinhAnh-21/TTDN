package com.example.TTDN.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exam_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "exams_id_exams", nullable = false)
    private Long examId;

    @Column(name = "questions_id_questions", nullable = false)
    private Long questionId;
}