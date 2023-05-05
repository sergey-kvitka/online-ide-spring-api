package com.kvitka.spring_api.controllers;

import com.kvitka.spring_api.dtos.ChangeRoleDto;
import com.kvitka.spring_api.dtos.ProjectRoleDto;
import com.kvitka.spring_api.dtos.ProjectUserDeleteDto;
import com.kvitka.spring_api.dtos.ProjectUserDto;
import com.kvitka.spring_api.entities.Project;
import com.kvitka.spring_api.entities.ProjectUser;
import com.kvitka.spring_api.entities.User;
import com.kvitka.spring_api.enums.ProjectRole;
import com.kvitka.spring_api.enums.ProjectType;
import com.kvitka.spring_api.security.jwt.JwtTokenProvider;
import com.kvitka.spring_api.services.impl.ProjectServiceImpl;
import com.kvitka.spring_api.services.impl.ProjectUserServiceImpl;
import com.kvitka.spring_api.services.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.kvitka.spring_api.enums.ProjectRole.CREATOR;
import static com.kvitka.spring_api.enums.ProjectRole.PROJECT_ADMIN;

@Slf4j
@RestController
@RequestMapping("/projectUser")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class ProjectUserController {

    private final JwtTokenProvider jwtTokenProvider;

    private final ProjectUserServiceImpl projectUserService;
    private final ProjectServiceImpl projectService;
    private final UserServiceImpl userService;

    @PostMapping("/setRole")
    public ResponseEntity<?> setRole(@RequestBody ChangeRoleDto changeRoleDto,
                                     @RequestHeader("Authorization") String bearerToken) {
        log.info("[setRole] method started");
        Function<String, ResponseEntity<Map<String, String>>> error =
                str -> {
                    log.info("Ошибка `{}` (ChangeRoleDto={})", str, changeRoleDto.toString());
                    return ResponseEntity.badRequest().body(Map.of("message", str));
                };

        if (!changeRoleDto.nonNullFields())
            return error.apply("Неверно составлено тело запроса");

        Project project = projectService.findByUUID(changeRoleDto.getProjectUUID());

        if (project == null)
            return error.apply("Проекта с таким UUID не существует");

        ProjectType projectType = project.getProjectType();

        ProjectRole newProjectRole = changeRoleDto.getNewProjectRole();

        ProjectRole defaultProjectRole = projectType.getDefaultProjectRole();

        if (newProjectRole == null) {
            if (defaultProjectRole == null) error.apply(
                    "Необходимо указать роль пользователя (тип проекта: %s)".formatted(projectType));
            newProjectRole = defaultProjectRole;
        }

        User assignee = jwtTokenProvider.getUserByBearerToken(bearerToken);

        ProjectRole assigneeProjectRole = project.getProjectUsers().stream()
                .filter(projectUser -> assignee.getUserId().equals(projectUser.getUser().getUserId()))
                .findFirst()
                .map(ProjectUser::getProjectRole)
                .orElse(null);

        if (assigneeProjectRole == null)
            return error.apply("Вы не являетесь участником данного проекта " +
                    "и не можете назначать его пользователям роли");

        if (newProjectRole == CREATOR)
            return error.apply("Невозможно назначить пользователю роль %s".formatted(newProjectRole));

        String username = changeRoleDto.getUsername();
        User user = userService.findByUsername(username);

        if (user == null)
            return error.apply("Пользователя с таким именем пользователя (%s) не существует".formatted(username));

        ProjectUser projectUser = projectUserService.findByUserIdAndProjectUUID(
                user.getUserId(), project.getProjectUUID());

        if (
                ((assigneeProjectRole.hasPermission(ProjectRole.Permission.SET_ADMIN_ROLE)
                        && newProjectRole == ProjectRole.PROJECT_ADMIN)
                        == (assigneeProjectRole.hasPermission(ProjectRole.Permission.SET_ROLES)
                        && !newProjectRole.hasPermission(ProjectRole.Permission.SET_ROLES)))
                        && !((projectType == ProjectType.PUBLIC_EDIT || projectType == ProjectType.PUBLIC_WATCH)
                        && newProjectRole == defaultProjectRole && projectUser == null)
        )
            return error.apply(("Вы не имеете права назначить данную роль пользователю (ваша роль: %s, " +
                    "присваиваемая пользователю роль: %s, тип проекта: %s)")
                    .formatted(assigneeProjectRole, newProjectRole, projectType));

        if (projectUser != null && user.getUserId().equals(assignee.getUserId()))
            return error.apply("Вы не можете назначить роль самому себе");

        ProjectRole oldProjectRole = (projectUser == null ? null : projectUser.getProjectRole());

        if (oldProjectRole == CREATOR)
            return error.apply("Невозможно назначать роль пользователю с ролью %s".formatted(oldProjectRole));

        if (oldProjectRole == PROJECT_ADMIN
                && !assigneeProjectRole.hasPermission(ProjectRole.Permission.SET_ADMIN_ROLE))
            return error.apply("Вы не имеете права изменить роль пользователю с ролью %s (ваша роль: %s)"
                    .formatted(oldProjectRole, assigneeProjectRole));

        if (projectUser == null) {
            projectUser = new ProjectUser();
            projectUser.setProject(project);
            projectUser.setUser(user);
        }
        projectUser.setProjectRole(newProjectRole);
        projectUser = projectUserService.save(projectUser);

        log.info("({}): {} —> {}", assigneeProjectRole, oldProjectRole, newProjectRole);
        log.info("[setRole] method finished (result: {})", projectUser);
        return ResponseEntity.ok(ProjectUserDto.from(projectUser));
    }

    @GetMapping("/projectRole/{projectUUID}")
    public ProjectRoleDto getProjectRole(@PathVariable String projectUUID,
                                         @RequestHeader("Authorization") String bearerToken) {
        User user = jwtTokenProvider.getUserByBearerToken(bearerToken);
        ProjectUser projectUser = projectUserService.findByUserIdAndProjectUUID(user.getUserId(), projectUUID);
        if (projectUser == null) return null;
        ProjectRole projectRole = projectUser.getProjectRole();
        return new ProjectRoleDto(projectRole, projectRole.getPermissions());
    }

    @Transactional
    @PostMapping("/delete")
    public ResponseEntity<?> deleteProjectUser(@RequestBody ProjectUserDeleteDto projectUserDeleteDto,
                                               @RequestHeader("Authorization") String bearerToken) {
        System.out.println("hello 1");
        User assignee = jwtTokenProvider.getUserByBearerToken(bearerToken);
        Project project = projectService.findByUUID(projectUserDeleteDto.getProjectUUID());

        ProjectRole assigneeProjectRole = project.getProjectUsers().stream()
                .filter(pu -> assignee.getUserId().equals(pu.getUser().getUserId()))
                .findFirst()
                .map(ProjectUser::getProjectRole)
                .orElse(null);

        if (assigneeProjectRole == null) return ResponseEntity.badRequest()
                .body("Вы не можете удалить участника проекта, так как сами не являетесь его участником");

        User user = userService.findByUsername(projectUserDeleteDto.getUsername());

        ProjectUser projectUser = project.getProjectUsers().stream()
                .filter(pu -> user.getUserId().equals(pu.getUser().getUserId()))
                .findFirst()
                .orElse(null);

        if (projectUser == null) return ResponseEntity.badRequest()
                .body("Данный пользователь уже не является участником данного проекта");

        boolean sameUser = Objects.equals(assignee.getUserId(), user.getUserId());

        if (!(assigneeProjectRole.hasPermission(ProjectRole.Permission.ADD_AND_DELETE_USERS) || sameUser))
            return ResponseEntity.badRequest().body("Вы не имеете права удалять других участников проекта");

        ProjectRole userProjectRole = projectUser.getProjectRole();

        if ((userProjectRole == CREATOR && !sameUser) || (userProjectRole == PROJECT_ADMIN
                && !assigneeProjectRole.hasPermission(ProjectRole.Permission.SET_ADMIN_ROLE))) {
            return ResponseEntity.badRequest()
                    .body("Вы не имеете права удалить данного участника проекта " +
                            "(ваша роль: %s, роль удаляемого участника: %s)"
                                    .formatted(assigneeProjectRole, userProjectRole));
        }

        try {
            System.out.println(projectUser);
            projectUserService.deleteProjectUser(projectUser.getProjectUserId());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Произошла ошибка при попытке удалить пользователя. " +
                    "Попробуйте обновить страницу или повторить действие позже");
        }

        System.out.println("hello 2");
        return ResponseEntity.ok("");
    }
}
