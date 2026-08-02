package com.example.TTDN.service;

import com.example.TTDN.dto.AuthResponse;
import com.example.TTDN.dto.LoginRequest;
import com.example.TTDN.dto.RegisterRequest;
import com.example.TTDN.entity.StudentProfile;
import com.example.TTDN.entity.TeacherProfile;
import com.example.TTDN.entity.User;
import com.example.TTDN.repository.StudentProfileRepository;
import com.example.TTDN.repository.TeacherProfileRepository;
import com.example.TTDN.repository.UserRepository;
import com.example.TTDN.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 1. Logic Đăng ký tài khoản mới (Tự động sinh Profile tương ứng trong CSDL)
    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Lỗi: Username đã tồn tại!");
        }

        // Tạo bản ghi User chính
        User user = new User();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        String role = request.getRole() != null ? request.getRole().toUpperCase() : "ROLE_STUDENT";
        user.setRole(role);
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());

        // Lưu user vào bảng `users` và lấy lại entity đã lưu (để lấy id_user)
        User savedUser = userRepository.save(user);

        // TỰ ĐỘNG CHÈN DỮ LIỆU VÀO BẢNG TƯƠNG ỨNG THEO ROLE (Dùng setUserId)
        if (role.contains("TEACHER") || role.contains("GIÁO VIÊN")) {
            TeacherProfile teacherProfile = new TeacherProfile();
            teacherProfile.setUserId(savedUser.getIdUser()); // Gán ID của User vừa tạo
            teacherProfile.setTeacherCode("GV" + String.format("%03d", savedUser.getIdUser())); // Sinh mã dạng GV005
            teacherProfile.setCreatedAt(LocalDateTime.now());

            teacherProfileRepository.save(teacherProfile);
        } else {
            StudentProfile studentProfile = new StudentProfile();
            studentProfile.setUserId(savedUser.getIdUser()); // Gán ID của User vừa tạo
            studentProfile.setStudentCode("HS" + String.format("%03d", savedUser.getIdUser())); // Sinh mã dạng HS006
            studentProfile.setGender(savedUser.getGender() != null ? savedUser.getGender() : "Nam");
            studentProfile.setCreatedAt(LocalDateTime.now());

            studentProfileRepository.save(studentProfile);
        }

        return "Đăng ký tài khoản thành công!";
    }

    // 2. Logic Đăng nhập & Tạo JWT Token
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy username!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Lỗi: Mật khẩu không chính xác!");
        }

        String token = tokenProvider.generateToken(user.getUsername(), user.getRole());

        return new AuthResponse(token, "Bearer", user.getUsername(), user.getRole());
    }
}