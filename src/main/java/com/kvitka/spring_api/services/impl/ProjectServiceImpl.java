package com.kvitka.spring_api.services.impl;

import com.kvitka.spring_api.entities.Project;
import com.kvitka.spring_api.entities.ProjectUser;
import com.kvitka.spring_api.entities.User;
import com.kvitka.spring_api.enums.ProjectRole;
import com.kvitka.spring_api.repositories.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl {
    private final ProjectRepository projectRepository;

    public Project findById(Long projectId) {
        return projectRepository.findById(projectId).orElse(null);
    }

    public Project findByUUID(String projectUUID) {
        return projectRepository.findByProjectUUID(projectUUID);
    }

    public boolean acceptPermission(Project project, User user, ProjectRole.Permission permission) {
        List<ProjectUser> projectUsers = project.getProjectUsers();
        ProjectRole projectRole = projectUsers.stream()
                .filter(projectUser -> projectUser.getUser().getUsername().equals(user.getUsername()))
                .map(ProjectUser::getProjectRole)
                .findFirst()
                .orElse(null);
        return permission.accept(projectRole);
    }

    public Project save(Project project) {
        return projectRepository.save(project);
    }

    public void delete(String projectUUID) {
        projectRepository.deleteByProjectUUID(projectUUID);
    }
}
