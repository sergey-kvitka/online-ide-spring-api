package com.kvitka.spring_api.services.impl;

import com.kvitka.spring_api.entities.CodeDifference;
import com.kvitka.spring_api.entities.Project;
import com.kvitka.spring_api.repositories.CodeDifferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CodeDifferenceServiceImpl {

    private final CodeDifferenceRepository codeDifferenceRepository;

    public List<CodeDifference> findByProjectAndProjectUser(Project project, Long fileId) {
        return codeDifferenceRepository.findByProjectAndFileId(
                project, fileId);
    }

    public CodeDifference save(CodeDifference codeDifference) {
        return codeDifferenceRepository.save(codeDifference);
    }

    public void deleteByProjectUUID(String projectUUID) {
        codeDifferenceRepository.deleteByProject_ProjectUUID(projectUUID);
    }

    public void deleteByFileId(Long fileId) {
        codeDifferenceRepository.deleteByFileId(fileId);
    }

    public void deleteByListOfFileIds(List<Long> fileIds) {
        codeDifferenceRepository.deleteByFileIdIn(fileIds);
    }
}
