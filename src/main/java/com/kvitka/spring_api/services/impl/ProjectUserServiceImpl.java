package com.kvitka.spring_api.services.impl;

import com.kvitka.spring_api.entities.Project;
import com.kvitka.spring_api.entities.ProjectUser;
import com.kvitka.spring_api.entities.User;
import com.kvitka.spring_api.enums.ProjectRole;
import com.kvitka.spring_api.repositories.ProjectUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectUserServiceImpl {

    private final ProjectUserRepository projectUserRepository;

    public boolean existsByProjectAndUser(Project project, User user) {
        return projectUserRepository.existsByProjectAndUser(project, user);
    }

    public void addParticipantIfDoesNotExist(User user, Project project) {
        addParticipantIfDoesNotExist(user, project, project.getProjectType().getDefaultProjectRole());
    }

    private void addParticipantIfDoesNotExist(User user, Project project, ProjectRole projectRole) {
        if (existsByProjectAndUser(project, user)) return;
        projectUserRepository.save(ProjectUser.builder()
                .user(user)
                .project(project)
                .projectRole(projectRole)
                .build());
    }

    public ProjectUser findByUserIdAndProjectUUID(Long userId, String projectUUID) {
        return projectUserRepository.findByUserIdAndProjectUUID(userId, projectUUID);
    }

    public ProjectUser save(ProjectUser projectUser) {
        return projectUserRepository.save(projectUser);
    }

    public void deleteProjectUser(Long projectUserId) {
        projectUserRepository.deleteByProjectUserId(projectUserId);
    }

    public void deleteByListOfId(List<Long> projectUserIds) {
        projectUserRepository.deleteByProjectUserIdIn(projectUserIds);
    }

    public void updateLastChange(Long projectUserId) {
        ProjectUser projectUser = projectUserRepository.findById(projectUserId).orElse(null);
        if (projectUser == null) return;
        projectUser.setLastChange(ZonedDateTime.now());
        projectUserRepository.save(projectUser);
    }

    public void deleteById(Long projectUserId) {
        projectUserRepository.deleteByProjectUserId(projectUserId);
    }
}
