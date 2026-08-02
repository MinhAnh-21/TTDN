package com.example.TTDN.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_student_profiles")
    private Long idStudentProfiles;

    @Column(name = "Users_id_user", nullable = false)
    private Long userId;

    @Column(name = "student_code", length = 45)
    private String studentCode;

    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "enrollment_year")
    private Integer enrollmentYear;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}