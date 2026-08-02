package com.example.TTDN.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "classes_has_exams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassHasExam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "classes_id_classes", nullable = false)
    private Long classesId;

    @Column(name = "exams_id_exams", nullable = false)
    private Long examsId;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "status", length = 45)
    private String status;
}