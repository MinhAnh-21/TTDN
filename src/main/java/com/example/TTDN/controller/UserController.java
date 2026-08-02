package com.example.TTDN.controller;

import com.example.TTDN.entity.StudentProfile;
import com.example.TTDN.entity.TeacherProfile;
import com.example.TTDN.entity.User;
import com.example.TTDN.repository.StudentProfileRepository;
import com.example.TTDN.repository.TeacherProfileRepository;
import com.example.TTDN.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // API lấy toàn bộ danh sách người dùng trong DB
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // API Lấy thông tin tài khoản + Profile chi tiết trực tiếp từ CSDL
    @GetMapping("/profile/{username}")
    public ResponseEntity<?> getUserProfile(@PathVariable String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy người dùng!");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("idUser", user.getIdUser());
        result.put("username", user.getUsername());
        result.put("fullName", user.getFullName());
        result.put("email", user.getEmail());
        result.put("phone", user.getPhone());
        result.put("gender", user.getGender());
        result.put("role", user.getRole());

        String role = user.getRole() != null ? user.getRole().toUpperCase() : "";

        // Nếu là Giáo viên, lấy thêm thông tin từ teacher_profiles
        if (role.contains("TEACHER") || role.contains("GIÁO VIÊN") || role.contains("GV")) {
            TeacherProfile tProfile = teacherProfileRepository.findByUserId(user.getIdUser()).orElse(null);
            if (tProfile != null) {
                result.put("teacherCode", tProfile.getTeacherCode());
                result.put("degree", tProfile.getDegree());
                result.put("department", tProfile.getDepartment());
            } else {
                result.put("teacherCode", "GV" + String.format("%03d", user.getIdUser()));
            }
        }
        // Nếu là Học sinh, lấy thêm thông tin từ student_profiles
        else {
            StudentProfile sProfile = studentProfileRepository.findByUserId(user.getIdUser()).orElse(null);
            if (sProfile != null) {
                result.put("studentCode", sProfile.getStudentCode());
                result.put("enrollmentYear", sProfile.getEnrollmentYear());
                result.put("address", sProfile.getAddress());
                result.put("dateOfBirth", sProfile.getDateOfBirth());
                if (sProfile.getGender() != null) {
                    result.put("gender", sProfile.getGender());
                }
            } else {
                result.put("studentCode", "HS" + String.format("%03d", user.getIdUser()));
            }
        }

        return ResponseEntity.ok(result);
    }

    // 1. API Cập nhật thông tin cá nhân thực tế xuống CSDL MySQL
    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            String fullName = (String) request.get("fullName");
            String email = (String) request.get("email");
            String phone = (String) request.get("phone");
            String gender = (String) request.get("gender");

            if (username == null || username.isEmpty()) {
                return ResponseEntity.badRequest().body("Tên đăng nhập không hợp lệ!");
            }

            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body("Người dùng không tồn tại!");
            }

            // Cập nhật dữ liệu mới vào bảng Users
            if (fullName != null) user.setFullName(fullName);
            if (email != null) user.setEmail(email);
            if (phone != null) user.setPhone(phone);
            if (gender != null) user.setGender(gender);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            String role = user.getRole() != null ? user.getRole().toUpperCase() : "";

            // A. Cập nhật các trường riêng vào bảng teacher_profiles nếu là Giáo viên
            if (role.contains("TEACHER") || role.contains("GIÁO VIÊN") || role.contains("GV")) {
                TeacherProfile tProfile = teacherProfileRepository.findByUserId(user.getIdUser()).orElse(null);
                if (tProfile == null) {
                    tProfile = new TeacherProfile();
                    tProfile.setUserId(user.getIdUser());
                    tProfile.setTeacherCode("GV" + String.format("%03d", user.getIdUser()));
                    tProfile.setDepartmentId(1L);
                    tProfile.setCreatedAt(LocalDateTime.now());
                }

                if (tProfile.getDepartmentId() == null) {
                    tProfile.setDepartmentId(1L);
                }

                if (request.get("degree") != null) tProfile.setDegree((String) request.get("degree"));
                if (request.get("department") != null) tProfile.setDepartment((String) request.get("department"));
                tProfile.setUpdatedAt(LocalDateTime.now());
                teacherProfileRepository.save(tProfile);
            }
            // B. Cập nhật các trường riêng vào bảng student_profiles nếu là Học sinh
            else if (role.contains("STUDENT") || role.contains("HỌC SINH") || role.contains("HS")) {
                StudentProfile sProfile = studentProfileRepository.findByUserId(user.getIdUser()).orElse(null);
                if (sProfile == null) {
                    sProfile = new StudentProfile();
                    sProfile.setUserId(user.getIdUser());
                    sProfile.setStudentCode("HS" + String.format("%03d", user.getIdUser())); // Dùng idUser để sinh mã
                    sProfile.setCreatedAt(LocalDateTime.now());
                }
                if (request.get("studentCode") != null) sProfile.setStudentCode((String) request.get("studentCode"));
                if (request.get("address") != null) sProfile.setAddress((String) request.get("address"));
                if (gender != null) sProfile.setGender(gender);
                sProfile.setUpdatedAt(LocalDateTime.now());
                studentProfileRepository.save(sProfile);
            }

            return ResponseEntity.ok("Cập nhật thông tin tài khoản thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi Server: " + e.getMessage());
        }
    }

    // 2. API Đổi mật khẩu thực tế xuống CSDL MySQL
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body("Tên đăng nhập không hợp lệ!");
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("Người dùng không tồn tại!");
        }

        // Kiểm tra mật khẩu hiện tại có khớp với CSDL không
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu hiện tại không chính xác!");
        }

        // Mã hóa và lưu mật khẩu mới
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok("Đổi mật khẩu thành công!");
    }
}