package com.example.TTDN.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CreateQuestionRequest {
    private String content;
    private String questionType; // MULTIPLE_CHOICE, ESSAY...
    private String difficultyLevel; // EASY, MEDIUM, HARD
    private Long courseId;
    private List<QuestionOptionDto> options; // Danh sách 4 đáp án A, B, C, D
}