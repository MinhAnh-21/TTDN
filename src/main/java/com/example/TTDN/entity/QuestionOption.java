package com.example.TTDN.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "question_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_question_options")
    private Long idQuestionOptions;

    // Sửa tên cột thành option_text cho chuẩn MySQL
    @Column(name = "option_text", nullable = false, columnDefinition = "TEXT")
    private String optionText;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect = false;

    // Tên cột khóa ngoại chính xác trong MySQL của bạn
    @Column(name = "questions_id_questions", nullable = false)
    private Long questionId;
}