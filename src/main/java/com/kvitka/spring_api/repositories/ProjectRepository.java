package com.kvitka.spring_api.repositories;

import com.kvitka.spring_api.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Project findByProjectUUID(String projectUUID);
}