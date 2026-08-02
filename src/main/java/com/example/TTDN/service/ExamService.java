package com.example.TTDN.service;

import com.example.TTDN.dto.CreateExamRequest;
import com.example.TTDN.dto.CreateQuestionRequest;
import com.example.TTDN.dto.ExamDetailResponse;
import com.example.TTDN.dto.QuestionOptionDto;
import com.example.TTDN.dto.SubmitExamRequest;
import com.example.TTDN.entity.Exam;
import com.example.TTDN.entity.ExamSubmission;
import com.example.TTDN.entity.Question;
import com.example.TTDN.entity.QuestionOption;
import com.example.TTDN.entity.StudentAnswer;
import com.example.TTDN.entity.TeacherProfile;
import com.example.TTDN.entity.User;
import com.example.TTDN.repository.ExamRepository;
import com.example.TTDN.repository.ExamSubmissionRepository;
import com.example.TTDN.repository.QuestionOptionRepository;
import com.example.TTDN.repository.QuestionRepository;
import com.example.TTDN.repository.StudentAnswerRepository;
import com.example.TTDN.repository.TeacherProfileRepository;
import com.example.TTDN.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ExamService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionOptionRepository questionOptionRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExamSubmissionRepository examSubmissionRepository;

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;

    @Transactional
    public Question createQuestion(CreateQuestionRequest request) {
        Question question = new Question();

        question.setContent(request.getContent());
        question.setQuestionText(request.getContent());
        question.setQuestionType(request.getQuestionType() != null ? request.getQuestionType() : "MULTIPLE_CHOICE");
        question.setDifficultyLevel(request.getDifficultyLevel() != null ? request.getDifficultyLevel() : "MEDIUM");
        question.setCourseId(request.getCourseId());

        Long currentUserId = getCurrentUserIdFromSecurity();
        if (currentUserId != null) {
            Optional<TeacherProfile> teacherOpt = teacherProfileRepository.findByUserId(currentUserId);
            if (teacherOpt.isPresent()) {
                question.setTeacherProfileId(teacherOpt.get().getIdTeacherProfiles());
            } else {
                question.setTeacherProfileId(1L);
            }
        } else {
            question.setTeacherProfileId(1L);
        }

        question.setExamId(1L);
        question.setCreatedAt(LocalDateTime.now());

        Question savedQuestion = questionRepository.save(question);

        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            for (QuestionOptionDto optDto : request.getOptions()) {
                QuestionOption option = new QuestionOption();
                option.setOptionText(optDto.getOptionText());
                option.setIsCorrect(optDto.getIsCorrect() != null ? optDto.getIsCorrect() : false);
                option.setQuestionId(savedQuestion.getIdQuestions());
                questionOptionRepository.save(option);
            }
        }

        return savedQuestion;
    }

    private Long getCurrentUserIdFromSecurity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String username = auth.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                return userOpt.get().getIdUser();
            }
        }
        return 1L;
    }

    public List<Question> getQuestionsByCourse(Long courseId) {
        return questionRepository.findByCourseId(courseId);
    }

    public Exam createExam(CreateExamRequest request) {
        Exam exam = new Exam();
        exam.setExamName(request.getExamName());
        exam.setDurationMinutes(request.getDurationMinutes());
        exam.setTotalQuestions(request.getTotalQuestions());
        exam.setStatus(request.getStatus() != null ? request.getStatus() : "DRAFT");
        exam.setCourseId(request.getCourseId());
        exam.setCreatedAt(LocalDateTime.now());

        Long currentUserId = getCurrentUserIdFromSecurity();
        Long teacherId = 1L;
        if (currentUserId != null) {
            Optional<TeacherProfile> teacherOpt = teacherProfileRepository.findByUserId(currentUserId);
            if (teacherOpt.isPresent()) {
                teacherId = teacherOpt.get().getIdTeacherProfiles();
            }
        }

        exam.setTeacherProfileId(teacherId);
        exam.setTeacherProfileIdAlt(teacherId);

        return examRepository.save(exam);
    }

    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    public Question addQuestionToExam(Long examId, Long questionId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi với ID: " + examId));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi với ID: " + questionId));

        question.setExamId(exam.getIdExams());
        return questionRepository.save(question);
    }

    public ExamDetailResponse getExamDetail(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi với ID: " + examId));

        List<Question> questions = questionRepository.findByExamId(examId);

        List<ExamDetailResponse.QuestionDetailDto> questionDetailDtos = new ArrayList<>();
        for (Question q : questions) {
            ExamDetailResponse.QuestionDetailDto detailDto = new ExamDetailResponse.QuestionDetailDto();
            detailDto.setQuestion(q);

            List<QuestionOption> options = questionOptionRepository.findByQuestionId(q.getIdQuestions());
            detailDto.setOptions(options);

            questionDetailDtos.add(detailDto);
        }

        ExamDetailResponse response = new ExamDetailResponse();
        response.setExam(exam);
        response.setQuestions(questionDetailDtos);

        return response;
    }

    @Transactional
    public ExamSubmission submitExam(SubmitExamRequest request) {
        examRepository.findById(request.getExamId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi với ID: " + request.getExamId()));

        List<Question> questions = questionRepository.findByExamId(request.getExamId());
        int totalQuestions = questions.size();

        if (totalQuestions == 0) {
            throw new RuntimeException("Đề thi này không có câu hỏi nào để chấm điểm!");
        }

        int correctCount = 0;

        ExamSubmission submission = new ExamSubmission();
        submission.setStudentProfileId(request.getStudentProfileId() != null ? request.getStudentProfileId() : 1L);
        submission.setStartTime(LocalDateTime.now().minusMinutes(30));
        submission.setSubmitTime(LocalDateTime.now());
        submission.setCreatedAt(LocalDateTime.now());
        submission.setTabSwitchCount(0);
        submission.setClassesHasExamsId(request.getClassesHasExamsId() != null ? request.getClassesHasExamsId() : 1L);
        submission.setScore(0.0);

        ExamSubmission savedSubmission = examSubmissionRepository.save(submission);

        if (request.getAnswers() != null) {
            for (SubmitExamRequest.AnswerDto ans : request.getAnswers()) {
                boolean isCorrect = false;

                if (ans.getSelectedOptionId() != null) {
                    Optional<QuestionOption> optionOpt = questionOptionRepository.findById(ans.getSelectedOptionId());
                    if (optionOpt.isPresent() && Boolean.TRUE.equals(optionOpt.get().getIsCorrect())) {
                        isCorrect = true;
                        correctCount++;
                    }
                }

                StudentAnswer studentAnswer = new StudentAnswer();
                studentAnswer.setSubmissionId(savedSubmission.getIdExamSubmissions());
                studentAnswer.setQuestionId(ans.getQuestionId());
                studentAnswer.setSelectedOptionId(ans.getSelectedOptionId());
                studentAnswer.setIsCorrect(isCorrect);

                studentAnswerRepository.save(studentAnswer);
            }
        }

        double score = ((double) correctCount / totalQuestions) * 10;
        score = Math.round(score * 100.0) / 100.0;

        savedSubmission.setScore(score);
        return examSubmissionRepository.save(savedSubmission);
    }
}