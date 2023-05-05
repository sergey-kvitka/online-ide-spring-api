package com.kvitka.spring_api.services.impl;

import com.kvitka.spring_api.entities.Role;
import com.kvitka.spring_api.entities.User;
import com.kvitka.spring_api.repositories.RoleRepository;
import com.kvitka.spring_api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public void register(User user) {
        Role roleUser = roleRepository.findByName("ROLE_USER");
        List<Role> userRoles = new ArrayList<>();
        userRoles.add(roleUser);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user.setRoles(userRoles);
        user = userRepository.save(user);
        log.info("User registered ({})", user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> findByUsernameLike(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username);
    }

    public ZonedDateTime findLastChangeByUserId(Long userId) {
        Timestamp timestamp = userRepository.findLastChangeByUserId(userId);
        return (timestamp == null)
                ? null
                : ZonedDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
    }
}
