package com.example.TTDN.controller;

import com.example.TTDN.entity.AssignmentSubmission;
import com.example.TTDN.entity.StudentProfile;
import com.example.TTDN.repository.AssignmentSubmissionRepository;
import com.example.TTDN.repository.StudentProfileRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "*")
public class SubmissionController {

    @Autowired
    private AssignmentSubmissionRepository submissionRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

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
            Long targetStudentProfileId = request.getUserId();
            StudentProfile profile = studentProfileRepository.findByUserId(request.getUserId()).orElse(null);
            if (profile != null) {
                targetStudentProfileId = profile.getIdStudentProfiles();
            } else {
                profile = new StudentProfile();
                profile.setUserId(request.getUserId());
                profile.setStudentCode("HS" + String.format("%03d", request.getUserId()));
                profile.setCreatedAt(LocalDateTime.now());
                profile = studentProfileRepository.save(profile);
                targetStudentProfileId = profile.getIdStudentProfiles();
            }

            AssignmentSubmission submission = submissionRepository
                    .findSubmissionByCustom(request.getAssignmentId(), targetStudentProfileId)
                    .orElse(new AssignmentSubmission());

            try {
                submission.getClass().getMethod("setAssignmentsIdAssignments", Long.class).invoke(submission, request.getAssignmentId());
            } catch (Exception e1) {
                try {
                    submission.getClass().getMethod("setAssignmentId", Long.class).invoke(submission, request.getAssignmentId());
                } catch (Exception e2) {}
            }

            try {
                submission.getClass().getMethod("setStudentProfilesIdStudentProfiles", Long.class).invoke(submission, targetStudentProfileId);
            } catch (Exception e1) {
                try {
                    submission.getClass().getMethod("setStudentProfileId", Long.class).invoke(submission, targetStudentProfileId);
                } catch (Exception e2) {}
            }

            try {
                submission.getClass().getMethod("setSubmittedFile", String.class).invoke(submission, request.getFileUrl());
            } catch (Exception e1) {
                try {
                    submission.getClass().getMethod("setFileUrl", String.class).invoke(submission, request.getFileUrl());
                } catch (Exception e2) {}
            }

            try {
                submission.getClass().getMethod("setSubmittedAt", LocalDateTime.class).invoke(submission, LocalDateTime.now());
            } catch (Exception e) {}

            try {
                String st = request.getStatus() != null ? request.getStatus() : "SUBMITTED";
                submission.getClass().getMethod("setStatus", String.class).invoke(submission, st);
            } catch (Exception e) {}

            AssignmentSubmission saved = submissionRepository.save(submission);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi lưu bài nộp: " + e.getMessage());
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
            StudentProfile profile = studentProfileRepository.findByUserId(request.getUserId()).orElse(null);
            if (profile != null) {
                targetProfileId = profile.getIdStudentProfiles();
            }

            AssignmentSubmission submission = submissionRepository
                    .findSubmissionByCustom(request.getAssignmentId(), targetProfileId)
                    .orElse(null);

            if (submission == null) {
                submission = submissionRepository
                        .findSubmissionByCustom(request.getAssignmentId(), request.getUserId())
                        .orElse(new AssignmentSubmission());
            }

            try {
                submission.getClass().getMethod("setAssignmentsIdAssignments", Long.class).invoke(submission, request.getAssignmentId());
            } catch (Exception e1) {
                try {
                    submission.getClass().getMethod("setAssignmentId", Long.class).invoke(submission, request.getAssignmentId());
                } catch (Exception e2) {}
            }

            try {
                submission.getClass().getMethod("setStudentProfilesIdStudentProfiles", Long.class).invoke(submission, targetProfileId);
            } catch (Exception e1) {
                try {
                    submission.getClass().getMethod("setStudentProfileId", Long.class).invoke(submission, targetProfileId);
                } catch (Exception e2) {}
            }

            // GÁN TRỰC TIẾP ĐIỂM VÀO CẢ 2 HÀM SETTER ĐỂ ĐẢM BẢO LƯU VÀO CSDL
            try {
                submission.getClass().getMethod("setGrade", Double.class).invoke(submission, finalScore);
            } catch (Exception e1) {}

            try {
                submission.getClass().getMethod("setScore", Double.class).invoke(submission, finalScore);
            } catch (Exception e1) {}

            try {
                submission.getClass().getMethod("setStatus", String.class).invoke(submission, "GRADED");
            } catch (Exception e) {}

            try {
                submission.getClass().getMethod("setGradedAt", LocalDateTime.class).invoke(submission, LocalDateTime.now());
            } catch (Exception e) {}

            AssignmentSubmission saved = submissionRepository.save(submission);
            return ResponseEntity.ok(saved);
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
            StudentProfile profile = studentProfileRepository.findByUserId(userId).orElse(null);
            if (profile != null) {
                profileId = profile.getIdStudentProfiles();
            }

            AssignmentSubmission submission = submissionRepository
                    .findSubmissionByCustom(assignmentId, profileId)
                    .orElse(null);

            if (submission == null && !profileId.equals(userId)) {
                submission = submissionRepository
                        .findSubmissionByCustom(assignmentId, userId)
                        .orElse(null);
            }

            if (submission != null) {
                return ResponseEntity.ok(submission);
            }
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            return ResponseEntity.ok().body(null);
        }
    }
}