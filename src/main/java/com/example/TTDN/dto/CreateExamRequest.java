package com.example.TTDN.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CreateExamRequest {
    // --- Cấu trúc ban đầu ---
    private String examName;
    private Integer durationMinutes;
    private Integer totalQuestions;
    private String status; // Ví dụ: "DRAFT", "PUBLISHED"
    private Long courseId;

    // --- Các trường bổ sung cần thiết cho tạo đề thi trực tuyến ---
    private String grade;           // Khối học (VD: Lớp 1)
    private String subject;         // Môn học (VD: Toán học)
    private String examType;        // Loại bài thi (15_MINUTES, MIDTERM, FINAL)
    private String examFormat;      // Hình thức: 'MULTIPLE_CHOICE' hoặc 'ESSAY'
    private String startTime;       // Thời gian bắt đầu
    private String endTime;         // Thời gian kết thúc
    private String examFileUrl;     // Link file đề thi (Cloudinary)
    private String answerFileUrl;   // Link file đáp án (Cloudinary)
    private String note;            // Ghi chú / Hướng dẫn làm bài
    private String viewResult;      // Xem kết quả ngay: 'Có' hoặc 'Không'
    private List<Long> classes;     // Danh sách ID các lớp nhận đề thi
}