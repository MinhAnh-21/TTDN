package com.example.TTDN.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_teacher_profiles")
    private Long idTeacherProfiles;

    @Column(name = "Users_id_user", nullable = false)
    private Long userId;

    @Column(name = "teacher_code", length = 45)
    private String teacherCode;

    @Column(name = "degree", length = 45)
    private String degree;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "departments_id_departments")
    private Long departmentId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}