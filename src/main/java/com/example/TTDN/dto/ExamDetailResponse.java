package com.example.TTDN.dto;

import com.example.TTDN.entity.Exam;
import com.example.TTDN.entity.Question;
import lombok.Data;
import java.util.List;

@Data
public class ExamDetailResponse {
    private Exam exam;
    private List<QuestionDetailDto> questions;

    @Data
    public static class QuestionDetailDto {
        private Question question;
        private List<com.example.TTDN.entity.QuestionOption> options;
    }
}