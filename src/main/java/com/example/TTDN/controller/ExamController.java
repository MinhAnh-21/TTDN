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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    @Autowired
    private ExamService examService;

    @PostMapping("/questions")
    public ResponseEntity<Question> createQuestion(@RequestBody CreateQuestionRequest request) {
        Question question = examService.createQuestion(request);
        return ResponseEntity.ok(question);
    }

    @PostMapping
    public ResponseEntity<Exam> createExam(@RequestBody CreateExamRequest request) {
        Exam exam = examService.createExam(request);
        return ResponseEntity.ok(exam);
    }

    @GetMapping
    public ResponseEntity<List<Exam>> getAllExams() {
        List<Exam> exams = examService.getAllExams();
        return ResponseEntity.ok(exams);
    }

    // API gán một câu hỏi vào đề thi
    @PostMapping("/{examId}/questions/{questionId}")
    public ResponseEntity<Question> addQuestionToExam(
            @PathVariable Long examId,
            @PathVariable Long questionId) {
        Question updatedQuestion = examService.addQuestionToExam(examId, questionId);
        return ResponseEntity.ok(updatedQuestion);
    }

    // API xem chi tiết đề thi kèm câu hỏi và đáp án
    @GetMapping("/{id}/detail")
    public ResponseEntity<ExamDetailResponse> getExamDetail(@PathVariable Long id) {
        ExamDetailResponse detail = examService.getExamDetail(id);
        return ResponseEntity.ok(detail);
    }

    // API nộp bài thi và chấm điểm tự động
    @PostMapping("/submit")
    public ResponseEntity<ExamSubmission> submitExam(@RequestBody SubmitExamRequest request) {
        ExamSubmission submission = examService.submitExam(request);
        return ResponseEntity.ok(submission);
    }
}