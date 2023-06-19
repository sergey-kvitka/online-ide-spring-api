package com.kvitka.spring_api.services.impl;

import com.kvitka.spring_api.entities.ProjectFolder;
import com.kvitka.spring_api.repositories.ProjectFolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectFolderServiceImpl {

    private final ProjectFolderRepository projectFolderRepository;

    public void save(ProjectFolder projectFolder) {
        projectFolderRepository.save(projectFolder);
    }

    public void deleteByListOfId(List<Long> projectFolderIds) {
        projectFolderRepository.deleteByProjectFolderIdIn(projectFolderIds);
    }

    public void saveAll(List<ProjectFolder> projectFolders) {
        projectFolderRepository.saveAll(projectFolders);
    }

    public ProjectFolder findById(Long projectFolderId) {
        return projectFolderRepository.findById(projectFolderId).orElse(null);
    }

    public boolean existsByPath(String path) {
        return projectFolderRepository.existsByPath(path);
    }
}
