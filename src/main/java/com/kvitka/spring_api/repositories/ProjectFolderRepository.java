package com.kvitka.spring_api.repositories;

import com.kvitka.spring_api.entities.ProjectFolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectFolderRepository extends JpaRepository<ProjectFolder, Long> {
    void deleteByProjectFolderIdIn(List<Long> projectFolderIds);

    boolean existsByPath(String path);
}