package com.kvitka.spring_api.services.impl;

import com.kvitka.spring_api.entities.ProjectFile;
import com.kvitka.spring_api.repositories.ProjectFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectFileServiceImpl {

    private final ProjectFileRepository projectFileRepository;

    public void save(ProjectFile projectFile) {
        projectFileRepository.save(projectFile);
    }

    public void deleteByListOfId(List<Long> projectFileIds) {
        projectFileRepository.deleteByIdIn(projectFileIds);
    }

    public void saveAll(List<ProjectFile> projectUsers) {
        projectFileRepository.saveAll(projectUsers);
    }
}
