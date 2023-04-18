package com.kvitka.spring_api.controllers;

import com.kvitka.spring_api.dtos.ChangeRoleDto;
import com.kvitka.spring_api.dtos.ProjectRoleDto;
import com.kvitka.spring_api.entities.Project;
import com.kvitka.spring_api.entities.ProjectUser;
import com.kvitka.spring_api.entities.User;
import com.kvitka.spring_api.enums.ProjectRole;
import com.kvitka.spring_api.security.jwt.JwtTokenProvider;
import com.kvitka.spring_api.services.impl.ProjectServiceImpl;
import com.kvitka.spring_api.services.impl.ProjectUserServiceImpl;
import com.kvitka.spring_api.services.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
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

    @PutMapping("/setRole")
    public ResponseEntity<Map<String, String>> setRole(
            @RequestBody ChangeRoleDto changeRoleDto,
            @RequestHeader("Authorization") String bearerToken) {

        log.info("[setRole] method started");
        Function<String, ResponseEntity<Map<String, String>>> error =
                str -> ResponseEntity.badRequest().body(Map.of("message", str));

        if (!changeRoleDto.nonNullFields())
            return error.apply("Неверно составлено тело запроса");

        Project project = projectService.findByUUID(changeRoleDto.getProjectUUID());

        if (project == null)
            return error.apply("Проекта с таким UUID не существует");

        User assignee = jwtTokenProvider.getUserByBearerToken(bearerToken);

        ProjectRole assigneeProjectRole = project.getProjectUsers().stream()
                .filter(projectUser -> assignee.getUserId().equals(projectUser.getUser().getUserId()))
                .findFirst()
                .map(ProjectUser::getProjectRole)
                .orElse(null);

        if (assigneeProjectRole == null)
            return error.apply("Вы не являетесь участником данного проекта " +
                    "и не можете назначать его пользователям роли");

        ProjectRole newProjectRole = changeRoleDto.getNewProjectRole();

        if (newProjectRole == CREATOR)
            return error.apply("Невозможно назначить пользователю роль %s".formatted(newProjectRole));

        if ((assigneeProjectRole.hasPermission(ProjectRole.Permission.SET_ADMIN_ROLE)
                && newProjectRole == ProjectRole.PROJECT_ADMIN)
                == (assigneeProjectRole.hasPermission(ProjectRole.Permission.SET_ROLES)
                && !newProjectRole.hasPermission(ProjectRole.Permission.SET_ROLES)))
            return error.apply(("Вы не имеете права назначить данную роль пользователю (ваша роль: %s, " +
                    "присваиваемая пользователю роль: %s)").formatted(assigneeProjectRole, newProjectRole));

        String username = changeRoleDto.getUsername();
        User user = userService.findByUsername(username);

        if (user == null)
            return error.apply("Пользователя с таким именем пользователя (%s) не существует".formatted(username));

        if (user.getUserId().equals(assignee.getUserId()))
            return error.apply("Вы не можете назначить роль самому себе");

        ProjectUser projectUser = projectUserService.findByUserIdAndProjectUUID(
                user.getUserId(), project.getProjectUUID());

        if (projectUser == null)
            return error.apply("Данный пользователь не является участником проекта");

        ProjectRole oldProjectRole = projectUser.getProjectRole();

        if (oldProjectRole == CREATOR)
            return error.apply("Невозможно назначать роль пользователю с ролью %s".formatted(oldProjectRole));

        if (oldProjectRole == PROJECT_ADMIN
                && !assigneeProjectRole.hasPermission(ProjectRole.Permission.SET_ADMIN_ROLE))
            return error.apply("Вы не имеете права изменить роль пользователю с ролью %s (ваша роль: %s)"
                    .formatted(oldProjectRole, assigneeProjectRole));

        projectUser.setProjectRole(newProjectRole);
        projectUser = projectUserService.save(projectUser);

        log.info("({}): {} —> {}", assigneeProjectRole, oldProjectRole, newProjectRole);
        log.info("[setRole] method finished (result: {})", projectUser);
        return ResponseEntity.ok(new HashMap<>());
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
}
