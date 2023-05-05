package com.kvitka.spring_api.repositories;

import com.kvitka.spring_api.entities.ProjectFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectFileRepository extends JpaRepository<ProjectFile, Long> {
    void deleteByIdIn(List<Long> projectFileIds);
}