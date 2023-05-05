package com.kvitka.spring_api.controllers;

import com.kvitka.spring_api.dtos.ProjectChangeDto;
import com.kvitka.spring_api.dtos.ProjectInfoDto;
import com.kvitka.spring_api.dtos.ProjectListItemDto;
import com.kvitka.spring_api.dtos.ProjectUserDto;
import com.kvitka.spring_api.entities.Project;
import com.kvitka.spring_api.entities.ProjectFile;
import com.kvitka.spring_api.entities.ProjectUser;
import com.kvitka.spring_api.entities.User;
import com.kvitka.spring_api.enums.ProjectRole;
import com.kvitka.spring_api.exceptions.ProjectRoleException;
import com.kvitka.spring_api.security.jwt.JwtTokenProvider;
import com.kvitka.spring_api.services.impl.ProjectFileServiceImpl;
import com.kvitka.spring_api.services.impl.ProjectServiceImpl;
import com.kvitka.spring_api.services.impl.ProjectUserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/project")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class ProjectController {

    private final JwtTokenProvider jwtTokenProvider;

    private final ProjectServiceImpl projectService;
    private final ProjectUserServiceImpl projectUserService;
    private final ProjectFileServiceImpl projectFileService;

    @GetMapping("/yourProjects")
    public List<ProjectListItemDto> getProjectList(@RequestHeader("Authorization") String bearerToken) {
        User user = jwtTokenProvider.getUserByBearerToken(bearerToken);
        return user.getProjectUsers().stream()
                .map(ProjectListItemDto::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{projectUUID}/projectUsers")
    public List<ProjectUserDto> getProjectUsers(@RequestHeader("Authorization") String bearerToken,
                                                @PathVariable String projectUUID) {
        Project project = projectService.findByUUID(projectUUID);
        if (project == null) throw new IllegalArgumentException("По данному адресу проект не найден");
        User user = jwtTokenProvider.getUserByBearerToken(bearerToken);

        ProjectRole defaultProjectRole = project.getProjectType().getDefaultProjectRole();
        if (defaultProjectRole != null) projectUserService.addParticipantIfDoesNotExist(user, project);

        if (!projectService.acceptPermission(project, user, ProjectRole.Permission.BE_PARTICIPANT)) {
            throw new ProjectRoleException("Для просмотра участников проекта необходимо самому быть его участником");
        }

        return project.getProjectUsers().stream()
                .map(ProjectUserDto::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{projectUUID}/info")
    public ProjectInfoDto getProjectInfo(@RequestHeader("Authorization") String bearerToken,
                                         @PathVariable String projectUUID) {
        Project project = projectService.findByUUID(projectUUID);
        if (project == null) throw new IllegalArgumentException("По данному адресу проект не найден");
        User user = jwtTokenProvider.getUserByBearerToken(bearerToken);

        ProjectRole defaultProjectRole = project.getProjectType().getDefaultProjectRole();
        if (defaultProjectRole != null) projectUserService.addParticipantIfDoesNotExist(user, project);

        if (!projectService.acceptPermission(project, user, ProjectRole.Permission.BE_PARTICIPANT)) {
            throw new ProjectRoleException("Для просмотра информации о проекте необходимо самому быть его участником");
        }

        return ProjectInfoDto.from(project);
    }

    @PutMapping("/{projectUUID}/edit")
    public ResponseEntity<Map<String, String>> editProject(@RequestHeader("Authorization") String bearerToken,
                                                           @PathVariable String projectUUID,
                                                           @RequestBody ProjectChangeDto projectChangeDto) {
        Function<String, ResponseEntity<Map<String, String>>> error =
                str -> ResponseEntity.badRequest().body(Map.of("message", str));

        Project project = projectService.findByUUID(projectUUID);
        if (project == null)
            return error.apply("Проекта с таким UUID не существует");

        User user = jwtTokenProvider.getUserByBearerToken(bearerToken);
        ProjectUser projectUser = projectUserService.findByUserIdAndProjectUUID(user.getUserId(), projectUUID);

        if (projectUser == null)
            return error.apply("Вы не имеете права изменять настройки проекта, " +
                    "так как не являетесь участником проекта");

        if (!projectUser.getProjectRole().hasPermission(ProjectRole.Permission.PROJECT_SETTINGS))
            return error.apply("Вы не имеете права изменять настройки проекта");

        project.setName(projectChangeDto.getName());
        project.setDescription(projectChangeDto.getDescription());
        project.setProjectType(projectChangeDto.getProjectType());

        projectService.save(project);

        return ResponseEntity.ok(new HashMap<>());
    }

    @PostMapping("/create")
    public ResponseEntity<String> createProject(@RequestHeader("Authorization") String bearerToken,
                                                @RequestBody ProjectChangeDto projectChangeDto) {
        User user = jwtTokenProvider.getUserByBearerToken(bearerToken);

        Project project = new Project();

        project.setName(projectChangeDto.getName());
        project.setDescription(projectChangeDto.getDescription());
        project.setProjectType(projectChangeDto.getProjectType());
        project.setCreated(ZonedDateTime.now());

        project = projectService.save(project);
        String uuid = project.getProjectUUID();

        try {
            projectUserService.save(new ProjectUser(
                    null, ProjectRole.CREATOR, null, user, project));
        } catch (Exception e) {
            projectService.delete(uuid);
            return ResponseEntity.badRequest().body("Ошибка создания проекта");
        }

        return ResponseEntity.ok(uuid);
    }

    @Transactional
    @GetMapping("/{projectUUID}/delete")
    public ResponseEntity<String> deleteProject(@RequestHeader("Authorization") String bearerToken,
                                                @PathVariable String projectUUID) {
        User user = jwtTokenProvider.getUserByBearerToken(bearerToken);

        ProjectUser projectUser = projectUserService.findByUserIdAndProjectUUID(user.getUserId(), projectUUID);

        if (projectUser == null) return ResponseEntity.badRequest().body("Вы не являетесь участником данного проекта");

        if (projectUser.getProjectRole() != ProjectRole.CREATOR)
            return ResponseEntity.badRequest().body("Вы не имеете права удалить этот проект");

        Project project = projectService.findByUUID(projectUUID);

        if (project == null) return ResponseEntity.badRequest().body("Проекта с таким UUID не существует");

        projectUserService.deleteByListOfId(project.getProjectUsers()
                .stream()
                .map(ProjectUser::getProjectUserId)
                .toList());
        projectFileService.deleteByListOfId(project.getProjectFiles()
                .stream()
                .map(ProjectFile::getId)
                .toList());

        projectService.delete(projectUUID);

        return ResponseEntity.ok(null);
    }
}