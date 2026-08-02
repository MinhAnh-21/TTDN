package com.example.TTDN.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateExamRequest {
    private String examName;
    private Integer durationMinutes;
    private Integer totalQuestions;
    private String status; // Ví dụ: "DRAFT", "PUBLISHED"
    private Long courseId;
}