package com.example.TTDN.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_materials")
    private Long idMaterials;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "file_url", nullable = false, length = 255)
    private String fileUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "classes_id_classes", nullable = false)
    private Long classId;

    @Column(name = "teacher_profiles_id_teacher_profiles", nullable = false)
    private Long teacherProfileId;
}