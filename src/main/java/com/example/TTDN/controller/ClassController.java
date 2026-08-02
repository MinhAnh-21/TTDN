package com.example.TTDN.controller;

import com.example.TTDN.dto.AddStudentToClassRequest;
import com.example.TTDN.dto.ClassRequest;
import com.example.TTDN.entity.Assignment;
import com.example.TTDN.entity.ClassEntity;
import com.example.TTDN.entity.ClassStudent;
import com.example.TTDN.entity.StudentProfile;
import com.example.TTDN.entity.TeacherProfile;
import com.example.TTDN.entity.User;
import com.example.TTDN.repository.AssignmentRepository;
import com.example.TTDN.repository.ClassRepository;
import com.example.TTDN.repository.ClassStudentRepository;
import com.example.TTDN.repository.StudentProfileRepository;
import com.example.TTDN.repository.TeacherProfileRepository;
import com.example.TTDN.repository.UserRepository;
import com.example.TTDN.service.ClassService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class ClassController {

    @Autowired
    private ClassService classService;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private ClassStudentRepository classStudentRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Getter
    @Setter
    public static class AssignmentRequest {
        private String title;
        private String description;
        private String fileUrl;
        private String dueDate;
        private Long teacherProfileId;
    }

    @PostMapping
    public ResponseEntity<ClassEntity> createClass(@RequestBody ClassRequest request) {
        return ResponseEntity.ok(classService.createClass(request));
    }

    @GetMapping
    public ResponseEntity<List<ClassEntity>> getAllClasses() {
        return ResponseEntity.ok(classService.getAllClasses());
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ClassEntity>> getClassesByCourseId(@PathVariable Long courseId) {
        List<ClassEntity> classes = classRepository.findByCourseId(courseId);
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassEntity> getClassById(@PathVariable Long id) {
        return ResponseEntity.ok(classService.getClassById(id));
    }

    @GetMapping("/student/{userId}")
    public ResponseEntity<?> getClassesByStudent(@PathVariable Long userId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            return ResponseEntity.ok(List.of());
        }

        List<ClassStudent> relations = classStudentRepository.findByStudentProfileId(profile.getIdStudentProfiles());
        if (relations.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Long> classIds = relations.stream().map(ClassStudent::getClassesId).toList();
        List<ClassEntity> classes = classRepository.findAllById(classIds);
        return ResponseEntity.ok(classes);
    }

    @PostMapping("/add-student")
    public ResponseEntity<String> addStudentToClass(@RequestBody AddStudentToClassRequest request) {
        return ResponseEntity.ok(classService.addStudentToClass(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateClass(@PathVariable Long id, @RequestBody ClassRequest request) {
        ClassEntity classEntity = classRepository.findById(id).orElse(null);
        if (classEntity == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy lớp học!");
        }

        if (request.getClassName() != null) {
            classEntity.setClassName(request.getClassName());
        }
        if (request.getSchoolYear() != null) {
            classEntity.setSchoolYear(request.getSchoolYear());
        }

        classRepository.save(classEntity);
        return ResponseEntity.ok(classEntity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClass(@PathVariable Long id) {
        if (!classRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Lớp học không tồn tại!");
        }
        classRepository.deleteById(id);
        return ResponseEntity.ok("Xóa lớp học thành công!");
    }

    @GetMapping("/available-students")
    public ResponseEntity<?> getAvailableStudents() {
        List<User> students = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && (u.getRole().toUpperCase().contains("STUDENT") || u.getRole().toUpperCase().contains("HỌC SINH") || u.getRole().toUpperCase().contains("HS")))
                .toList();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{classId}/students")
    public ResponseEntity<?> getStudentsByClass(@PathVariable Long classId) {
        List<ClassStudent> relations = classStudentRepository.findByClassesId(classId);
        List<Long> profileIds = relations.stream().map(ClassStudent::getStudentProfileId).toList();

        if (profileIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<StudentProfile> profiles = studentProfileRepository.findAllById(profileIds);
        List<Long> userIds = profiles.stream().map(StudentProfile::getUserId).toList();
        List<User> users = userRepository.findAllById(userIds);

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{classId}/student-count")
    public ResponseEntity<Integer> getStudentCountByClass(@PathVariable Long classId) {
        int count = classStudentRepository.findByClassesId(classId).size();
        return ResponseEntity.ok(count);
    }

    @PostMapping("/{classId}/add-student/{userId}")
    public ResponseEntity<?> addStudentToClassDb(@PathVariable Long classId, @PathVariable Long userId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            profile = new StudentProfile();
            profile.setUserId(userId);
            profile.setStudentCode("HS" + String.format("%03d", userId));
            profile.setCreatedAt(LocalDateTime.now());
            studentProfileRepository.save(profile);
        }

        boolean exists = classStudentRepository.existsByClassesIdAndStudentProfileId(classId, profile.getIdStudentProfiles());
        if (!exists) {
            ClassStudent cs = new ClassStudent();
            cs.setClassesId(classId);
            cs.setStudentProfileId(profile.getIdStudentProfiles());
            cs.setStatus("ACTIVE");
            cs.setJoinedAt(LocalDateTime.now());
            classStudentRepository.save(cs);
        }

        return ResponseEntity.ok("Thêm học sinh vào lớp thành công!");
    }

    @DeleteMapping("/{classId}/remove-student/{userId}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> removeStudentFromClass(@PathVariable Long classId, @PathVariable Long userId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId).orElse(null);
        if (profile != null) {
            classStudentRepository.deleteByClassesIdAndStudentProfileId(classId, profile.getIdStudentProfiles());
        }
        return ResponseEntity.ok("Đã xóa học sinh khỏi lớp!");
    }

    @DeleteMapping("/{classId}/remove-all-students")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> removeAllStudentsFromClass(@PathVariable Long classId) {
        classStudentRepository.deleteByClassesId(classId);
        return ResponseEntity.ok("Đã xóa toàn bộ học sinh khỏi lớp!");
    }

    @GetMapping("/{classId}/assignments")
    public ResponseEntity<?> getAssignmentsByClass(@PathVariable Long classId) {
        List<Assignment> list = assignmentRepository.findByClassesId(classId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{classId}/assignments")
    public ResponseEntity<?> createAssignment(@PathVariable Long classId, @RequestBody AssignmentRequest request) {
        try {
            Assignment assignment = new Assignment();
            assignment.setTitle(request.getTitle());
            assignment.setDescription(request.getDescription());

            // Đảm bảo nhận và gán chính xác fileUrl từ request gửi lên
            assignment.setFileUrl(request.getFileUrl());

            assignment.setClassesId(classId);
            assignment.setCreatedAt(LocalDateTime.now());

            if (request.getDueDate() != null && !request.getDueDate().isEmpty()) {
                try {
                    // Xử lý linh hoạt định dạng ngày giờ gửi lên từ input datetime-local
                    String dateStr = request.getDueDate();
                    if (dateStr.length() == 16) { // dạng "YYYY-MM-DDTHH:mm"
                        dateStr += ":00";
                    }
                    assignment.setDueDate(LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                } catch (Exception e) {
                    assignment.setDueDate(null);
                }
            }

            List<TeacherProfile> teachers = teacherProfileRepository.findAll();
            Long teacherId;
            if (teachers.isEmpty()) {
                TeacherProfile newTeacher = new TeacherProfile();
                List<User> users = userRepository.findAll();
                if (!users.isEmpty()) {
                    newTeacher.setUserId(users.get(0).getIdUser());
                }
                newTeacher = teacherProfileRepository.save(newTeacher);
                teacherId = newTeacher.getIdTeacherProfiles();
            } else {
                teacherId = teachers.get(0).getIdTeacherProfiles();
            }
            assignment.setTeacherProfileId(teacherId);

            Assignment saved = assignmentRepository.save(assignment);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi lưu CSDL: " + e.getMessage());
        }
    }

    @DeleteMapping("/assignments/{assignmentId}")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long assignmentId) {
        if (!assignmentRepository.existsById(assignmentId)) {
            return ResponseEntity.badRequest().body("Bài tập không tồn tại!");
        }
        assignmentRepository.deleteById(assignmentId);
        return ResponseEntity.ok("Xóa bài tập thành công!");
    }
}