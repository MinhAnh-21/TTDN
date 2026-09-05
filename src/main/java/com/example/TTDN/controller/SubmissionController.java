package com.example.TTDN.controller;

import com.example.TTDN.entity.AssignmentSubmission;
import com.example.TTDN.entity.StudentProfile;
import com.example.TTDN.repository.AssignmentSubmissionRepository;
import com.example.TTDN.repository.StudentProfileRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "*")
public class SubmissionController {

    @Autowired
    private AssignmentSubmissionRepository submissionRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Getter
    @Setter
    public static class SubmitRequest {
        private Long assignmentId;
        private Long userId;
        private String fileUrl;
        private String status;
    }

    @Getter
    @Setter
    public static class GradeRequest {
        private Long assignmentId;
        private Long userId;
        private Double score;
        private Double grade;
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitAssignment(@RequestBody SubmitRequest request) {
        try {
            Long assignmentId = request.getAssignmentId();
            Long rawUserId = request.getUserId();
            String fileUrl = request.getFileUrl();

            if (assignmentId == null || assignmentId <= 0) {
                return ResponseEntity.badRequest().body("Mã bài tập không hợp lệ!");
            }

            // Tìm đúng id_student_profiles từ bảng student_profiles
            Long targetStudentProfileId = rawUserId;
            try {
                List<Long> pList = jdbcTemplate.queryForList(
                        "SELECT id_student_profiles FROM student_profiles WHERE users_id_user = ? OR Users_id_user = ? OR id_student_profiles = ?",
                        Long.class, rawUserId, rawUserId, rawUserId
                );
                if (!pList.isEmpty()) {
                    targetStudentProfileId = pList.get(0);
                }
            } catch (Exception ignored) {}

            // Xóa bài nộp cũ nếu có trước khi thêm mới
            try {
                jdbcTemplate.update(
                        "DELETE FROM assignment_submissions WHERE assignments_id_assignments = ? AND student_profiles_id_student_profiles = ?",
                        assignmentId, targetStudentProfileId
                );
            } catch (Exception ignored) {}

            // Thêm mới bài nộp khớp chính xác theo các cột trong CSDL
            jdbcTemplate.update(
                    "INSERT INTO assignment_submissions (submitted_file, submitted_at, assignments_id_assignments, student_profiles_id_student_profiles) VALUES (?, NOW(), ?, ?)",
                    fileUrl, assignmentId, targetStudentProfileId
            );

            return ResponseEntity.ok("Nộp bài thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi lưu bài nộp: " + e.getMessage());
        }
    }

    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<?> getSubmissionsByAssignment(@PathVariable Long assignmentId) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT * FROM assignment_submissions WHERE assignments_id_assignments = ?",
                    assignmentId
            );
            return ResponseEntity.ok(rows);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
        }
    }

    @PostMapping("/grade")
    public ResponseEntity<?> gradeAssignment(@RequestBody GradeRequest request) {
        try {
            Double finalScore = request.getScore() != null ? request.getScore() : request.getGrade();
            if (finalScore == null) {
                return ResponseEntity.badRequest().body("Điểm số không được để trống");
            }

            Long targetProfileId = request.getUserId();
            try {
                List<Long> pList = jdbcTemplate.queryForList(
                        "SELECT id_student_profiles FROM student_profiles WHERE users_id_user = ? OR Users_id_user = ? OR id_student_profiles = ?",
                        Long.class, request.getUserId(), request.getUserId(), request.getUserId()
                );
                if (!pList.isEmpty()) targetProfileId = pList.get(0);
            } catch (Exception ignored) {}

            jdbcTemplate.update(
                    "UPDATE assignment_submissions SET grade = ?, graded_at = NOW() WHERE assignments_id_assignments = ? AND (student_profiles_id_student_profiles = ? OR student_profiles_id_student_profiles = ?)",
                    finalScore, request.getAssignmentId(), targetProfileId, request.getUserId()
            );

            return ResponseEntity.ok("Lưu điểm thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi lưu điểm: " + e.getMessage());
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkSubmission(
            @RequestParam Long assignmentId,
            @RequestParam Long userId) {
        try {
            Long profileId = userId;
            try {
                List<Long> pList = jdbcTemplate.queryForList(
                        "SELECT id_student_profiles FROM student_profiles WHERE users_id_user = ? OR Users_id_user = ? OR id_student_profiles = ?",
                        Long.class, userId, userId, userId
                );
                if (!pList.isEmpty()) profileId = pList.get(0);
            } catch (Exception ignored) {}

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT * FROM assignment_submissions WHERE assignments_id_assignments = ? AND (student_profiles_id_student_profiles = ? OR student_profiles_id_student_profiles = ?) ORDER BY id_assignment_submissions DESC LIMIT 1",
                    assignmentId, profileId, userId
            );

            if (!rows.isEmpty()) {
                return ResponseEntity.ok(rows.get(0));
            }
            return ResponseEntity.ok(Map.of());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of());
        }
    }
}