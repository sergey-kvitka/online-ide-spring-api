package com.kvitka.spring_api.services.impl;

import com.kvitka.spring_api.entities.Project;
import com.kvitka.spring_api.entities.User;
import com.kvitka.spring_api.enums.ProjectRole;
import com.kvitka.spring_api.repositories.ProjectUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public void addParticipantIfDoesNotExist(User user, Project project, ProjectRole projectRole) {
        if (existsByProjectAndUser(project, user)) return;

    }
}
