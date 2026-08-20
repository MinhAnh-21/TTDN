package com.example.TTDN.dto;

import lombok.Data;
import java.util.List;

@Data
public class SubmitExamRequest {
    private Long examId;
    private Long studentProfileId;
    private Long classesHasExamsId; // Thêm trường này để nhận ID lớp-đề thi từ client
    private Integer tabSwitchCount; // Bổ sung trường này để nhận số lần gian lận chuyển tab từ client
    private List<AnswerDto> answers;

    @Data
    public static class AnswerDto {
        private Long questionId;
        private Long selectedOptionId;
    }
}