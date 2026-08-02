package com.example.TTDN.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Long idUser;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "username", nullable = false, unique = true, length = 45)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 15)
    private String phone;

    // Bổ sung thuộc tính gender ánh xăm cột gender trong DB
    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "avata", length = 255)
    private String avata;

    @Column(name = "role", nullable = false, length = 45)
    private String role;

    @Column(name = "status", length = 45)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}