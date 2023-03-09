package com.kvitka.spring_api.controllers;

import com.kvitka.spring_api.dtos.ProjectInfoDto;
import com.kvitka.spring_api.dtos.ProjectListItemDto;
import com.kvitka.spring_api.dtos.ProjectUserDto;
import com.kvitka.spring_api.entities.Project;
import com.kvitka.spring_api.entities.User;
import com.kvitka.spring_api.enums.ProjectRole;
import com.kvitka.spring_api.exceptions.ProjectRoleException;
import com.kvitka.spring_api.security.jwt.JwtTokenProvider;
import com.kvitka.spring_api.services.impl.ProjectServiceImpl;
import com.kvitka.spring_api.services.impl.ProjectUserServiceImpl;
import com.kvitka.spring_api.services.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/project")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class ProjectController {

    private final JwtTokenProvider jwtTokenProvider;

    private final UserServiceImpl userService;
    private final ProjectServiceImpl projectService;
    private final ProjectUserServiceImpl projectUserService;

    @GetMapping("yourProjects")
    public List<ProjectListItemDto> getProjectList(@RequestHeader("Authorization") String bearerToken) {
        User user = userService.findByUsername(jwtTokenProvider.getUsername(jwtTokenProvider.getToken(bearerToken)));
        return user.getProjectUsers().stream()
                .map(ProjectListItemDto::from)
                .collect(Collectors.toList());
    }

    @GetMapping("{projectUUID}/projectUsers")
    public List<ProjectUserDto> getProjectUsers(@RequestHeader("Authorization") String bearerToken,
                                                @PathVariable String projectUUID) {
        User user = jwtTokenProvider.getUserByBearerToken(bearerToken);
        Project project = projectService.findByUUID(projectUUID);
        if (project == null) throw new IllegalArgumentException("По данному адресу проект не найден");

        ProjectRole defaultProjectRole = project.getProjectType().getDefaultProjectRole();
        if (defaultProjectRole != null) projectUserService.addParticipantIfDoesNotExist(user, project);

        if (!projectService.acceptPermission(project, user, ProjectRole.Permission.BE_PARTICIPANT)) {
            throw new ProjectRoleException("Для просмотра участников проекта необходимо самому быть его участником");
        }

        return project.getProjectUsers().stream()
                .map(ProjectUserDto::from)
                .collect(Collectors.toList());
    }

    @GetMapping("{projectUUID}/info")
    public ProjectInfoDto getProjectInfo(@RequestHeader("Authorization") String bearerToken,
                                         @PathVariable String projectUUID) {
        Project project = projectService.findByUUID(projectUUID);
        User user = jwtTokenProvider.getUserByBearerToken(bearerToken);
        if (project == null) throw new IllegalArgumentException("По данному адресу проект не найден");

        ProjectRole defaultProjectRole = project.getProjectType().getDefaultProjectRole();
        if (defaultProjectRole != null) projectUserService.addParticipantIfDoesNotExist(user, project);

        if (!projectService.acceptPermission(project, user, ProjectRole.Permission.BE_PARTICIPANT)) {
            throw new ProjectRoleException("Для просмотра информации о проекте необходимо самому быть его участником");
        }

        return ProjectInfoDto.from(project);
    }


}
