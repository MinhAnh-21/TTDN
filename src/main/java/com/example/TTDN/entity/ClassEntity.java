package com.example.TTDN.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_classes")
    private Long idClasses;

    @Column(name = "class_name", nullable = false, length = 45)
    private String className;

    @Column(name = "school_year", nullable = false, length = 45)
    private String schoolYear;

    @Column(name = "enroll_code", nullable = false, unique = true, length = 45)
    private String enrollCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "courses_id_courses", nullable = false)
    private Long courseId;
}