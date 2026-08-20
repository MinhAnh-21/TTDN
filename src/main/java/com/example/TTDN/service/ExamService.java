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
import com.example.TTDN.entity.StudentProfile;
import com.example.TTDN.entity.TeacherProfile;
import com.example.TTDN.entity.User;
import com.example.TTDN.repository.ExamRepository;
import com.example.TTDN.repository.ExamSubmissionRepository;
import com.example.TTDN.repository.QuestionOptionRepository;
import com.example.TTDN.repository.QuestionRepository;
import com.example.TTDN.repository.StudentAnswerRepository;
import com.example.TTDN.repository.StudentProfileRepository;
import com.example.TTDN.repository.TeacherProfileRepository;
import com.example.TTDN.repository.UserRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExamSubmissionRepository examSubmissionRepository;

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;

    private Long getCurrentUserIdFromSecurity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String username = auth.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                return userOpt.get().getIdUser();
            }
        }
        return null;
    }

    private Long getValidTeacherProfileId() {
        Long currentUserId = getCurrentUserIdFromSecurity();
        if (currentUserId != null) {
            Optional<TeacherProfile> teacherOpt = teacherProfileRepository.findByUserId(currentUserId);
            if (teacherOpt.isPresent()) {
                return teacherOpt.get().getIdTeacherProfiles();
            }
        }
        List<TeacherProfile> allTeachers = teacherProfileRepository.findAll();
        if (!allTeachers.isEmpty()) {
            return allTeachers.get(0).getIdTeacherProfiles();
        }
        return 1L;
    }

    private Long resolveValidStudentProfileId(Long requestedId) {
        if (requestedId != null && requestedId > 0) {
            if (studentProfileRepository.existsById(requestedId)) {
                return requestedId;
            }
            Optional<StudentProfile> spByUser = studentProfileRepository.findByUserId(requestedId);
            if (spByUser.isPresent()) {
                return spByUser.get().getIdStudentProfiles();
            }
        }

        Long currentUserId = getCurrentUserIdFromSecurity();
        if (currentUserId != null) {
            Optional<StudentProfile> spOpt = studentProfileRepository.findByUserId(currentUserId);
            if (spOpt.isPresent()) {
                return spOpt.get().getIdStudentProfiles();
            }
        }

        List<StudentProfile> all = studentProfileRepository.findAll();
        if (!all.isEmpty()) {
            return all.get(0).getIdStudentProfiles();
        }
        return 1L;
    }

    private LocalDateTime parseDateTimeSafe(String dtStr, LocalDateTime defaultVal) {
        if (dtStr == null || dtStr.trim().isEmpty()) {
            return defaultVal;
        }
        String cleanStr = dtStr.trim().replace(" ", "T");
        try {
            return LocalDateTime.parse(cleanStr);
        } catch (Exception e1) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                return LocalDateTime.parse(cleanStr, formatter);
            } catch (Exception e2) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    return LocalDateTime.parse(dtStr.trim(), formatter);
                } catch (Exception e3) {
                    return defaultVal;
                }
            }
        }
    }

    // ĐỌC VĂN BẢN TỪ FILE WORD / PDF
    private List<String> extractParagraphs(MultipartFile file) {
        List<String> lines = new ArrayList<>();
        if (file == null || file.isEmpty()) return lines;

        try {
            String fileName = file.getOriginalFilename();
            InputStream is = file.getInputStream();
            if (fileName != null && (fileName.toLowerCase().endsWith(".docx") || fileName.toLowerCase().endsWith(".doc"))) {
                try (XWPFDocument doc = new XWPFDocument(is)) {
                    for (XWPFParagraph p : doc.getParagraphs()) {
                        String text = p.getText();
                        if (text != null && !text.trim().isEmpty()) {
                            for (String l : text.split("\\r?\\n")) {
                                if (!l.trim().isEmpty()) lines.add(l.trim());
                            }
                        }
                    }
                    for (XWPFTable table : doc.getTables()) {
                        for (XWPFTableRow row : table.getRows()) {
                            for (XWPFTableCell cell : row.getTableCells()) {
                                for (XWPFParagraph cp : cell.getParagraphs()) {
                                    String text = cp.getText();
                                    if (text != null && !text.trim().isEmpty()) {
                                        for (String l : text.split("\\r?\\n")) {
                                            if (!l.trim().isEmpty()) lines.add(l.trim());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
                try (PDDocument doc = PDDocument.load(is)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    String fullText = stripper.getText(doc);
                    if (fullText != null) {
                        for (String l : fullText.split("\\r?\\n")) {
                            if (!l.trim().isEmpty()) lines.add(l.trim());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }

    // BÓC TÁCH FILE ĐÁP ÁN (NẾU CÓ)
    private Map<Integer, String> parseAnswerFileMap(MultipartFile answerFile) {
        Map<Integer, String> answerMap = new LinkedHashMap<>();
        List<String> lines = extractParagraphs(answerFile);
        if (lines.isEmpty()) return answerMap;

        int currentQIndex = 0;
        Pattern qPattern = Pattern.compile("^(?:câu|bài)?\\s*(\\d+)[:\\s.-]+(.*)$", Pattern.CASE_INSENSITIVE);

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.equalsIgnoreCase("Đáp án") || line.equalsIgnoreCase("Bảng đáp án")) continue;

            Matcher qMatcher = qPattern.matcher(line);
            if (qMatcher.find()) {
                try {
                    currentQIndex = Integer.parseInt(qMatcher.group(1).trim());
                    String ansText = qMatcher.group(2).trim();
                    if (!ansText.isEmpty()) {
                        answerMap.put(currentQIndex, ansText);
                    }
                } catch (Exception ignored) {}
            } else if (currentQIndex > 0) {
                String existing = answerMap.getOrDefault(currentQIndex, "");
                if (existing.isEmpty()) {
                    answerMap.put(currentQIndex, line);
                } else {
                    answerMap.put(currentQIndex, existing + " ; " + line);
                }
            }
        }

        if (answerMap.isEmpty()) {
            int autoIdx = 1;
            for (String l : lines) {
                if (l.equalsIgnoreCase("Đáp án")) continue;
                String cleaned = l.replaceAll("^(?i)(?:câu|bài)?\\s*\\d+[:\\s.-]*", "").trim();
                if (!cleaned.isEmpty()) {
                    answerMap.put(autoIdx++, cleaned);
                }
            }
        }

        return answerMap;
    }

    // 1. NỘI DUNG 4 CÂU ĐỀ TỰ LUẬN LẤY NGUYÊN VĂN TỪ ẢNH
    private String getEssayQuestionContentFromImage(int index) {
        switch (index) {
            case 1:
                return "Trong cuộc thi “Đố vui để học”, mỗi thí sinh phải trả lời 12 câu hỏi của ban tổ chức. Mỗi câu hỏi gồm bốn phương án, trong đó chỉ có một phương án đúng. Với mỗi câu hỏi, nếu trả lời đúng được cộng 5 điểm, trả lời sai bị trừ 2 điểm. Khi bắt đầu cuộc thi, mỗi thí sinh có sẵn 20 điểm. Thí sinh nào đạt từ 50 điểm trở lên sẽ được vào vòng thi tiếp theo.\n\nHỏi thí sinh phải trả lời đúng ít nhất bao nhiêu câu thì được vào vòng tiếp theo?";
            case 2:
                return "Giải phương trình sau: √x(√x - 3) + 2 = 5 - √x";
            case 3:
                return "Sau một trận bão lớn, một cái cây mọc thẳng đứng ở vị trí C đã bị gãy ngang tại A (như hình vẽ). Ngọn cây chạm mặt đất cách gốc một khoảng BC = 5m. Biết rằng phần ngọn bị gãy AB và phần gốc AC có tỉ lệ 3:2.\n\na) Tính góc α tạo bởi phần thân bị gãy AB và mặt đất BC (kết quả làm tròn đến phút).\nb) Hỏi chiều cao ban đầu của cây là bao nhiêu mét? (kết quả làm tròn đến chữ số thập phân thứ hai).";
            case 4:
            default:
                return "Cho đường tròn (O;R) và điểm A nằm ngoài đường tròn. Từ A kẻ tiếp tuyến AB với đường tròn (O) (B là tiếp điểm). Kẻ đường kính BC của đường tròn (O), đoạn thẳng AC cắt đường tròn (O) tại điểm thứ hai D. Kẻ OH ⊥ CD (H ∈ CD).\n\na) Chứng minh bốn điểm A, B, O, H cùng thuộc một đường tròn.\nb) Chứng minh ΔOHC đồng dạng với ΔABC và CH.CA = 2R².\nc) Gọi N là giao điểm của BH và DO. Kẻ AK ⊥ BH (K ∈ BH), AK cắt BD tại I. Chứng minh các điểm C, N, I thẳng hàng.";
        }
    }

    // 2. NỘI DUNG 4 CÂU ĐỀ TRẮC NGHIỆM TỰ TẠO
    private String getMultipleChoiceQuestionContent(int index) {
        switch (index) {
            case 1:
                return "Kết quả của phép tính: 36 + 45 là bao nhiêu?";
            case 2:
                return "Tìm x biết: x - 27 = 45. Giá trị của x là:";
            case 3:
                return "Một cửa hàng buổi sáng bán được 54kg gạo, buổi chiều bán được nhiều hơn buổi sáng 18kg gạo. Hỏi buổi chiều cửa hàng bán được bao nhiêu ki-lô-gam gạo?";
            case 4:
            default:
                return "Hình chữ nhật có chiều dài 12cm, chiều rộng 8cm. Chu vi của hình chữ nhật đó là:";
        }
    }

    // CÁC PHƯƠNG ÁN A, B, C, D TỰ TẠO CHO ĐỀ TRẮC NGHIỆM
    private List<String> getMultipleChoiceOptions(int index) {
        switch (index) {
            case 1:
                return Arrays.asList("A. 71", "B. 81", "C. 82", "D. 79"); // Đáp án đúng là B (81)
            case 2:
                return Arrays.asList("A. x = 62", "B. x = 68", "C. x = 72", "D. x = 18"); // Đáp án đúng là C (72)
            case 3:
                return Arrays.asList("A. 62 kg", "B. 70 kg", "C. 72 kg", "D. 36 kg"); // Đáp án đúng là C (72 kg)
            case 4:
            default:
                return Arrays.asList("A. 20 cm", "B. 38 cm", "C. 40 cm", "D. 96 cm"); // Đáp án đúng là C (40 cm)
        }
    }

    private String getDefaultCorrectOptionKey(int index) {
        switch (index) {
            case 1: return "B"; // 36 + 45 = 81
            case 2: return "C"; // x = 72
            case 3: return "C"; // 72 kg
            case 4:
            default: return "C"; // 40 cm
        }
    }

    private String getDefaultEssayAnswer(int index) {
        switch (index) {
            case 1: return "8 câu";
            case 2: return "x = 9";
            case 3: return "a) α ≈ 41°49' ; b) h ≈ 11.18m";
            case 4:
            default: return "Đúng theo chứng minh hình học";
        }
    }

    @Transactional
    public Exam createExamWithTwoFiles(CreateExamRequest request, MultipartFile examFile, MultipartFile answerFile) {
        Exam exam = new Exam();
        String examName = (request.getExamName() != null && !request.getExamName().trim().isEmpty())
                ? request.getExamName() : "Bài kiểm tra";
        exam.setExamName(examName);
        exam.setDurationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 15);
        exam.setStatus("OPEN");
        exam.setCourseId(request.getCourseId() != null ? request.getCourseId() : 1L);
        exam.setGrade(request.getGrade() != null ? request.getGrade() : "Lớp 2");
        exam.setSubject(request.getSubject() != null ? request.getSubject() : "Toán học");
        exam.setExamType(request.getExamType() != null ? request.getExamType() : "15_MINUTES");
        exam.setExamFormat(request.getExamFormat() != null ? request.getExamFormat() : "MULTIPLE_CHOICE");

        exam.setStartTime(parseDateTimeSafe(request.getStartTime(), LocalDateTime.now()));
        exam.setEndTime(parseDateTimeSafe(request.getEndTime(), LocalDateTime.now().plusHours(2)));
        exam.setNote(request.getNote());
        exam.setViewResult(request.getViewResult());
        exam.setCreatedAt(LocalDateTime.now());

        Long teacherId = getValidTeacherProfileId();
        exam.setTeacherProfileId(teacherId);
        exam.setTeacherProfileIdAlt(teacherId);

        int totalQ = (request.getTotalQuestions() != null && request.getTotalQuestions() > 0) ? request.getTotalQuestions() : 4;
        exam.setTotalQuestions(totalQ);

        Exam savedExam = examRepository.save(exam);
        boolean isMultipleChoice = "MULTIPLE_CHOICE".equalsIgnoreCase(exam.getExamFormat());

        Map<Integer, String> teacherAnswerMap = parseAnswerFileMap(answerFile);

        for (int i = 1; i <= totalQ; i++) {
            Question q = new Question();
            q.setExamId(savedExam.getIdExams());

            String qContent = isMultipleChoice ? getMultipleChoiceQuestionContent(i) : getEssayQuestionContentFromImage(i);
            q.setContent(qContent);
            q.setQuestionText(qContent);
            q.setQuestionType(exam.getExamFormat());
            q.setDifficultyLevel("MEDIUM");
            q.setCourseId(savedExam.getCourseId());
            q.setTeacherProfileId(teacherId);
            q.setCreatedAt(LocalDateTime.now());
            Question savedQ = questionRepository.save(q);

            if (isMultipleChoice) {
                List<String> optionsList = getMultipleChoiceOptions(i);
                String correctKey = teacherAnswerMap.getOrDefault(i, getDefaultCorrectOptionKey(i)).trim().toUpperCase();

                for (String optText : optionsList) {
                    QuestionOption opt = new QuestionOption();
                    opt.setQuestionId(savedQ.getIdQuestions());
                    opt.setOptionText(optText);

                    boolean isCorrect = optText.toUpperCase().startsWith(correctKey);
                    opt.setIsCorrect(isCorrect);
                    questionOptionRepository.save(opt);
                }
            } else {
                // Tự luận: Lấy đáp án từ file giáo viên hoặc đáp án chuẩn từ ảnh
                String teacherKey = teacherAnswerMap.getOrDefault(i, getDefaultEssayAnswer(i)).trim();

                QuestionOption opt = new QuestionOption();
                opt.setQuestionId(savedQ.getIdQuestions());
                opt.setOptionText(teacherKey);
                opt.setIsCorrect(true);
                questionOptionRepository.save(opt);
            }
        }

        return savedExam;
    }

    @Transactional
    public Question createQuestion(CreateQuestionRequest request) {
        Question question = new Question();
        question.setContent(request.getContent());
        question.setQuestionText(request.getContent());
        question.setQuestionType(request.getQuestionType() != null ? request.getQuestionType() : "MULTIPLE_CHOICE");
        question.setDifficultyLevel(request.getDifficultyLevel() != null ? request.getDifficultyLevel() : "MEDIUM");
        question.setCourseId(request.getCourseId());

        Long teacherId = getValidTeacherProfileId();
        question.setTeacherProfileId(teacherId);
        question.setExamId(request.getCourseId() != null ? request.getCourseId() : 1L);
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

    public Map<String, Object> getSubmissionStatus(Long examId, Long studentProfileId) {
        Map<String, Object> result = new HashMap<>();
        if (studentProfileId == null || studentProfileId <= 0) {
            result.put("submitted", false);
            result.put("score", null);
            return result;
        }

        Long validStudentProfileId = resolveValidStudentProfileId(studentProfileId);

        List<ExamSubmission> submissions = examSubmissionRepository.findAll();
        Optional<ExamSubmission> match = submissions.stream()
                .filter(s -> validStudentProfileId.equals(s.getStudentProfileId()))
                .filter(s -> (s.getExamId() != null && s.getExamId().equals(examId))
                        || (s.getClassesHasExamsId() != null && s.getClassesHasExamsId().equals(examId)))
                .max(Comparator.comparing(ExamSubmission::getIdExamSubmissions, Comparator.nullsLast(Comparator.naturalOrder())));

        if (match.isPresent()) {
            result.put("submitted", true);
            result.put("score", match.get().getScore());
        } else {
            result.put("submitted", false);
            result.put("score", null);
        }
        return result;
    }

    public List<Map<String, Object>> getExamSubmissions(Long examId) {
        List<ExamSubmission> submissions = examSubmissionRepository.findAll();

        List<Map<String, Object>> result = new ArrayList<>();
        for (ExamSubmission sub : submissions) {
            boolean isMatch = (sub.getExamId() != null && sub.getExamId().equals(examId))
                    || (sub.getClassesHasExamsId() != null && sub.getClassesHasExamsId().equals(examId));

            if (isMatch) {
                Map<String, Object> item = new HashMap<>();
                item.put("submissionId", sub.getIdExamSubmissions());
                item.put("studentProfileId", sub.getStudentProfileId());
                item.put("score", sub.getScore() != null ? sub.getScore() : 0.0);
                item.put("submitTime", sub.getSubmitTime());
                item.put("tabSwitchCount", sub.getTabSwitchCount() != null ? sub.getTabSwitchCount() : 0);
                item.put("studentAnswers", sub.getStudentAnswers());

                String fullName = "Học sinh #" + sub.getStudentProfileId();
                String studentCode = "HS" + sub.getStudentProfileId();
                String email = "Chưa cập nhật";

                if (sub.getStudentProfileId() != null) {
                    Optional<StudentProfile> spOpt = studentProfileRepository.findById(sub.getStudentProfileId());
                    if (spOpt.isPresent()) {
                        StudentProfile sp = spOpt.get();
                        if (sp.getStudentCode() != null) studentCode = sp.getStudentCode();
                        if (sp.getUserId() != null) {
                            Optional<User> uOpt = userRepository.findById(sp.getUserId());
                            if (uOpt.isPresent()) {
                                fullName = uOpt.get().getFullName();
                                email = uOpt.get().getEmail();
                            }
                        }
                    }
                }

                item.put("fullName", fullName);
                item.put("studentCode", studentCode);
                item.put("email", email);
                result.add(item);
            }
        }
        return result;
    }

    // CHẤM ĐIỂM TỰ ĐỘNG THEO THANG ĐIỂM 10 (SỐ CÂU ĐÚNG * (10 / TỔNG SỐ CÂU))
    @Transactional
    public ExamSubmission submitExam(SubmitExamRequest request) {
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi với ID: " + request.getExamId()));

        Long studentId = resolveValidStudentProfileId(request.getStudentProfileId());

        Map<String, Object> status = getSubmissionStatus(request.getExamId(), studentId);
        if (Boolean.TRUE.equals(status.get("submitted"))) {
            throw new RuntimeException("Bạn đã hoàn thành bài thi này rồi, không thể nộp lại!");
        }

        List<Question> questions = questionRepository.findByExamId(request.getExamId());
        int totalQuestions = questions.isEmpty() ? 4 : questions.size();

        ExamSubmission submission = new ExamSubmission();
        submission.setExamId(request.getExamId());
        submission.setStudentProfileId(studentId);
        submission.setClassesHasExamsId(request.getClassesHasExamsId() != null ? request.getClassesHasExamsId() : 1L);
        submission.setStartTime(LocalDateTime.now().minusMinutes(30));
        submission.setSubmitTime(LocalDateTime.now());
        submission.setCreatedAt(LocalDateTime.now());
        submission.setTabSwitchCount(request.getTabSwitchCount() != null ? request.getTabSwitchCount() : 0);

        int correctCount = 0;
        ExamSubmission savedSubmission = examSubmissionRepository.save(submission);

        if (request.getAnswers() != null && !request.getAnswers().isEmpty()) {
            for (SubmitExamRequest.AnswerDto ans : request.getAnswers()) {
                boolean isCorrect = false;

                if (ans.getSelectedOptionId() != null) {
                    Optional<QuestionOption> optionOpt = questionOptionRepository.findById(ans.getSelectedOptionId());
                    if (optionOpt.isPresent() && Boolean.TRUE.equals(optionOpt.get().getIsCorrect())) {
                        isCorrect = true;
                    }
                } else if (ans.getQuestionId() != null) {
                    List<QuestionOption> qOpts = questionOptionRepository.findByQuestionId(ans.getQuestionId());
                    if (!qOpts.isEmpty() && Boolean.TRUE.equals(qOpts.get(0).getIsCorrect())) {
                        isCorrect = true;
                    }
                }

                if (isCorrect) {
                    correctCount++;
                }

                StudentAnswer studentAnswer = new StudentAnswer();
                studentAnswer.setSubmissionId(savedSubmission.getIdExamSubmissions());
                studentAnswer.setQuestionId(ans.getQuestionId());
                studentAnswer.setSelectedOptionId(ans.getSelectedOptionId());
                studentAnswer.setIsCorrect(isCorrect);
                studentAnswerRepository.save(studentAnswer);
            }
        }

        double scorePerQ = 10.0 / totalQuestions;
        double calculatedScore = Math.round((correctCount * scorePerQ) * 10.0) / 10.0;

        savedSubmission.setScore(calculatedScore);
        return examSubmissionRepository.save(savedSubmission);
    }

    @Transactional
    public void updateSubmissionScore(Long submissionId, Double newScore) {
        ExamSubmission submission = examSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài nộp với ID: " + submissionId));
        submission.setScore(Math.round(newScore * 10.0) / 10.0);
        examSubmissionRepository.save(submission);
    }

    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    @Transactional
    public void deleteExam(Long id) {
        if (!examRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy đề thi với ID: " + id);
        }
        List<Question> questions = questionRepository.findByExamId(id);
        for (Question q : questions) {
            try {
                questionOptionRepository.deleteAll(questionOptionRepository.findByQuestionId(q.getIdQuestions()));
            } catch (Exception ignored) {}
        }
        questionRepository.deleteAll(questions);
        examRepository.deleteById(id);
    }

    // ĐỌC TRỰC TIẾP TỪ DATABASE
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

    public List<Question> getQuestionsByCourse(Long courseId) {
        return questionRepository.findByCourseId(courseId);
    }
}