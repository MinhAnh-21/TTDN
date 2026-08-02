package com.example.TTDN.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "class_teachers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassTeacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "classes_id_classes", nullable = false)
    private Long classesId;

    @Column(name = "teacher_profiles_id_teacher_profiles", nullable = false)
    private Long teacherProfileId;
}