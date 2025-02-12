package com.example.democrudproject.repository;

import com.example.democrudproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByNameContainingIgnoreCaseAndSurnameContainingIgnoreCase(String name, String surname);
}

