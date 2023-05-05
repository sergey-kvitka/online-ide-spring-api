package com.kvitka.spring_api.repositories;

import com.kvitka.spring_api.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByUsernameContainingIgnoreCase(String username);

    @Query(nativeQuery = true, value =
            "select pu.last_change from project_users pu where pu.user_id = ? order by pu.last_change desc limit 1")
    Timestamp findLastChangeByUserId(@Param("id") Long id);
}