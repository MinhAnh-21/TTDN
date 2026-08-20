package com.example.TTDN.controller;

import com.example.TTDN.dto.CreateExamRequest;
import com.example.TTDN.dto.CreateQuestionRequest;
import com.example.TTDN.dto.ExamDetailResponse;
import com.example.TTDN.dto.SubmitExamRequest;
import com.example.TTDN.entity.Exam;
import com.example.TTDN.entity.ExamSubmission;
import com.example.TTDN.entity.Question;
import com.example.TTDN.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    @Autowired
    private ExamService examService;

    @PostMapping("/questions")
    public ResponseEntity<Question> createQuestion(@RequestBody CreateQuestionRequest request) {
        return ResponseEntity.ok(examService.createQuestion(request));
    }

    // Nhận trực tiếp FormData chứa thông tin đề thi và 2 file đính kèm
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<Exam> createExam(
            @RequestParam("examName") String examName,
            @RequestParam(value = "grade", required = false) String grade,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "examType", required = false) String examType,
            @RequestParam(value = "durationMinutes", required = false, defaultValue = "45") Integer durationMinutes,
            @RequestParam(value = "examFormat", required = false, defaultValue = "MULTIPLE_CHOICE") String examFormat,
            @RequestParam(value = "totalQuestions", required = false, defaultValue = "4") Integer totalQuestions,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "note", required = false) String note,
            @RequestParam(value = "viewResult", required = false) String viewResult,
            @RequestParam(value = "classes", required = false) List<Long> classes,
            @RequestParam(value = "examFile", required = false) MultipartFile examFile,
            @RequestParam(value = "answerFile", required = false) MultipartFile answerFile) {

        CreateExamRequest request = new CreateExamRequest();
        request.setExamName(examName);
        request.setGrade(grade);
        request.setSubject(subject);
        request.setExamType(examType);
        request.setDurationMinutes(durationMinutes);
        request.setExamFormat(examFormat);
        request.setTotalQuestions(totalQuestions);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setNote(note);
        request.setViewResult(viewResult);
        request.setCourseId(1L);

        return ResponseEntity.ok(examService.createExamWithTwoFiles(request, examFile, answerFile));
    }

    // Endpoint fallback nhận JSON thuần khi không gửi kèm file
    @PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<Exam> createExamJson(@RequestBody CreateExamRequest request) {
        return ResponseEntity.ok(examService.createExamWithTwoFiles(request, null, null));
    }

    @GetMapping
    public ResponseEntity<List<Exam>> getAllExams() {
        return ResponseEntity.ok(examService.getAllExams());
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<ExamDetailResponse> getExamDetail(@PathVariable Long id) {
        return ResponseEntity.ok(examService.getExamDetail(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/submit")
    public ResponseEntity<ExamSubmission> submitExam(@RequestBody SubmitExamRequest request) {
        return ResponseEntity.ok(examService.submitExam(request));
    }

    @GetMapping("/check-submission")
    public ResponseEntity<Map<String, Object>> checkSubmission(
            @RequestParam Long examId,
            @RequestParam(required = false) Long studentProfileId) {
        return ResponseEntity.ok(examService.getSubmissionStatus(examId, studentProfileId));
    }

    @GetMapping("/{id}/submissions")
    public ResponseEntity<List<Map<String, Object>>> getExamSubmissions(@PathVariable Long id) {
        return ResponseEntity.ok(examService.getExamSubmissions(id));
    }

    @PutMapping("/submissions/{submissionId}/score")
    public ResponseEntity<Map<String, Object>> updateSubmissionScore(
            @PathVariable Long submissionId,
            @RequestBody Map<String, Object> payload) {
        Double newScore = Double.valueOf(payload.get("score").toString());
        examService.updateSubmissionScore(submissionId, newScore);
        return ResponseEntity.ok(Map.of("message", "Cập nhật điểm thành công!", "score", newScore));
    }
}