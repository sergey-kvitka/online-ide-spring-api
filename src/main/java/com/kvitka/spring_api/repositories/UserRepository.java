package com.kvitka.spring_api.repositories;

import com.kvitka.spring_api.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByUsernameContaining(String username);
}