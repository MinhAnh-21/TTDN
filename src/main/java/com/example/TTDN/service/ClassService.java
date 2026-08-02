package com.example.TTDN.service;

import com.example.TTDN.dto.AddStudentToClassRequest;
import com.example.TTDN.dto.ClassRequest;
import com.example.TTDN.entity.ClassEntity;
import com.example.TTDN.entity.ClassStudent;
import com.example.TTDN.repository.ClassRepository;
import com.example.TTDN.repository.ClassStudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClassService {

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private ClassStudentRepository classStudentRepository;

    // 1. Tạo lớp học mới
    public ClassEntity createClass(ClassRequest request) {
        if (classRepository.findByEnrollCode(request.getEnrollCode()).isPresent()) {
            throw new RuntimeException("Lỗi: Mã tham gia lớp (enrollCode) đã tồn tại!");
        }

        ClassEntity classEntity = new ClassEntity();
        classEntity.setClassName(request.getClassName());
        classEntity.setSchoolYear(request.getSchoolYear());
        classEntity.setEnrollCode(request.getEnrollCode());
        classEntity.setCourseId(request.getCourseId());
        classEntity.setCreatedAt(LocalDateTime.now());

        return classRepository.save(classEntity);
    }

    // 2. Lấy tất cả danh sách lớp
    public List<ClassEntity> getAllClasses() {
        return classRepository.findAll();
    }

    // 3. Lấy thông tin lớp theo ID
    public ClassEntity getClassById(Long id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy lớp học với ID = " + id));
    }

    // 4. Thêm học sinh vào lớp học
    public String addStudentToClass(AddStudentToClassRequest request) {
        // Kiểm tra xem học sinh đã có trong lớp chưa
        if (classStudentRepository.existsByClassesIdAndStudentProfileId(
                request.getClassId(), request.getStudentProfileId())) {
            throw new RuntimeException("Lỗi: Học sinh này đã có trong lớp rồi!");
        }

        ClassStudent classStudent = new ClassStudent();
        classStudent.setClassesId(request.getClassId());
        classStudent.setStudentProfileId(request.getStudentProfileId());
        classStudent.setStatus("JOINED");
        classStudent.setJoinedAt(LocalDateTime.now());

        classStudentRepository.save(classStudent);
        return "Thêm học sinh vào lớp thành công!";
    }
}