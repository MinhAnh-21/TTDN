package com.example.TTDN.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionOptionDto {
    private String optionText;
    private Boolean isCorrect; // true nếu là đáp án đúng
}