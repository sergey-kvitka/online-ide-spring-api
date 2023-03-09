package com.kvitka.spring_api.repositories;

import com.kvitka.spring_api.entities.ProjectFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectFileRepository extends JpaRepository<ProjectFile, Long> {
}