package com.example.TTDN.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "class_students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "classes_id_classes", nullable = false)
    private Long classesId;

    @Column(name = "student_profiles_id_student_profiles", nullable = false)
    private Long studentProfileId;
}