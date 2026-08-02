package com.example.TTDN.repository;

import com.example.TTDN.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Hàm tìm kiếm người dùng theo username
    Optional<User> findByUsername(String username);
}