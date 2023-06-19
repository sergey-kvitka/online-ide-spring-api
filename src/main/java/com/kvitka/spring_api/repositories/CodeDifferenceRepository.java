package com.kvitka.spring_api.repositories;

import com.kvitka.spring_api.entities.CodeDifference;
import com.kvitka.spring_api.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeDifferenceRepository extends JpaRepository<CodeDifference, Long> {

    List<CodeDifference> findByProjectAndFileId(
            Project project, Long fileId);

    void deleteByProject_ProjectUUID(String projectUUID);

    void deleteByFileId(Long fileId);

    void deleteByFileIdIn(List<Long> fileIds);
}