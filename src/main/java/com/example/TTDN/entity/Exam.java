package com.example.TTDN.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_exams")
    private Long idExams;

    // Đã đồng bộ tên cột thành "exam_name" khớp chính xác với CSDL của bạn
    @Column(name = "exam_name", nullable = false, length = 255)
    private String examName;

    @Column(name = "duration", nullable = false)
    private Integer durationMinutes;

    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "status", length = 45)
    private String status;

    @Column(name = "exam_type", length = 45)
    private String examType; // '15_MINUTES', 'MIDTERM', 'FINAL'

    // --- CÁC TRƯỜNG BỔ SUNG CẦN THI CHO TÍNH NĂNG TẠO ĐỀ THI ---
    @Column(name = "exam_format", length = 45)
    private String examFormat; // 'MULTIPLE_CHOICE' hoặc 'ESSAY'

    @Column(name = "grade", length = 45)
    private String grade; // Khối học (VD: Lớp 1)

    @Column(name = "subject", length = 45)
    private String subject; // Môn học (VD: Toán học)

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "exam_file_url", length = 500)
    private String examFileUrl; // Link file đề thi

    @Column(name = "answer_file_url", length = 500)
    private String answerFileUrl; // Link file đáp án

    @Column(name = "note", columnDefinition = "TEXT")
    private String note; // Ghi chú / Hướng dẫn làm bài

    @Column(name = "view_result", length = 10)
    private String viewResult; // 'Có' hoặc 'Không'
    // -----------------------------------------------------------

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "courses_id_courses")
    private Long courseId;

    @Column(name = "teacher_profiles_id_teacher_profiles")
    private Long teacherProfileId;

    @Column(name = "teacher_profiles_id_teacher")
    private Long teacherProfileIdAlt;
}