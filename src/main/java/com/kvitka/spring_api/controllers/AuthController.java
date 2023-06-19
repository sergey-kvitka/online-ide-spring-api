package com.kvitka.spring_api.controllers;

import com.kvitka.spring_api.dtos.*;
import com.kvitka.spring_api.entities.Project;
import com.kvitka.spring_api.entities.ProjectUser;
import com.kvitka.spring_api.entities.Role;
import com.kvitka.spring_api.entities.User;
import com.kvitka.spring_api.security.jwt.JwtTokenProvider;
import com.kvitka.spring_api.services.impl.ProjectServiceImpl;
import com.kvitka.spring_api.services.impl.ProjectUserServiceImpl;
import com.kvitka.spring_api.services.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserServiceImpl userService;
    private final ProjectUserServiceImpl projectUserService;
    private final ProjectServiceImpl projectService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequestDto authenticationRequestDto) {
        log.info("login {}", authenticationRequestDto);
        try {
            String username = authenticationRequestDto.getUsername();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    username, authenticationRequestDto.getPassword()));
            User user = userService.findByUsername(username);
            if (user == null) throw new UsernameNotFoundException("");
            String token = jwtTokenProvider.createToken(username, user.getRoles());
            log.info("returning login response");
            return ResponseEntity.ok(new AuthenticationResponseDto(token));
        } catch (AuthenticationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Ошибка: неверные имя пользователя или пароль");
            error.put("cause", "username password");
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequestDto registrationRequestDto) {
        log.info("register {}", registrationRequestDto);
        Map<String, String> error = new HashMap<>();
        String username = registrationRequestDto.getUsername();
        if (userService.existsByUsername(username)) {
            error.put("message", "Ошибка: пользователь с таким именем пользователя уже существует");
            error.put("cause", "username");
            return ResponseEntity.badRequest().body(error);
        }
        String email = registrationRequestDto.getEmail();
        if (userService.existsByEmail(email)) {
            error.put("message", "Ошибка: пользователь с таким адресом электронной почты уже существует");
            error.put("cause", "email");
            return ResponseEntity.badRequest().body(error);
        }
        User user = User.builder()
                .username(username)
                .password(registrationRequestDto.getPassword())
                .firstName(registrationRequestDto.getFirstName())
                .lastName(registrationRequestDto.getLastName())
                .email(email)
                .build();
        userService.register(user);

        AuthenticationRequestDto authenticationRequestDto = new AuthenticationRequestDto(
                username, registrationRequestDto.getPassword());
        return login(authenticationRequestDto);
    }

    @GetMapping("validate")
    public List<String> validate(@RequestHeader("Authorization") String bearerToken) {
        String token = jwtTokenProvider.getToken(bearerToken);
        if (token == null || !jwtTokenProvider.validateToken(token)) return new ArrayList<>();
        User user = userService.findByUsername(jwtTokenProvider.getUsername(token));
        if (user == null) return new ArrayList<>();
        return user.getRoles().stream()
                .map(Role::getName)
                .toList();
    }

    @PostMapping("actualizeProjectInfo")
    public ResponseEntity<?> actualize(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody ActualizationRequestDto actualizationRequestDto) {
        User user = (bearerToken == null) ? null : jwtTokenProvider.getUserByBearerToken(bearerToken);
        if (user == null || actualizationRequestDto == null) return ResponseEntity.badRequest().body(null);

        Project project = projectService.findByUUID(actualizationRequestDto.getProjectUUID());

        if (project == null) return ResponseEntity.ok(
                new ActualizationResponseDto(false, false, true,
                        true, new ArrayList<>()));

        ProjectUser pUser = projectUserService.findByUserIdAndProjectUUID(user.getUserId(), project.getProjectUUID());

        if (pUser == null) return ResponseEntity.ok(
                new ActualizationResponseDto(true, false, true,
                        true, new ArrayList<>()));

        if (pUser.getProjectRole() != actualizationRequestDto.getProjectRole()) return ResponseEntity.ok(
                new ActualizationResponseDto(true, true, true,
                        true, new ArrayList<>()));

        boolean filesAndFoldersChanged = true;
        fileComparing:
        {
            List<AnyFileDto> currentFilesAndFolders = actualizationRequestDto.getCurrentFilesAndFolders();
            if (currentFilesAndFolders == null) {
                filesAndFoldersChanged = false;
                break fileComparing;
            }

            currentFilesAndFolders.sort(Comparator.comparing(AnyFileDto::getId));
            List<AnyFileDto> actualFilesAndFolders = Stream.concat(
                            project.getProjectFiles().stream().map(AnyFileDto::from),
                            project.getProjectFolders().stream().map(AnyFileDto::from)
                    )
                    .sorted(Comparator.comparing(AnyFileDto::getId))
                    .toList();

            int length = currentFilesAndFolders.size();
            if (length != actualFilesAndFolders.size()) break fileComparing;

            AnyFileDto current, actual;
            for (int i = 0; i < length; i++) {
                current = currentFilesAndFolders.get(i);
                actual = actualFilesAndFolders.get(i);
                if (!current.getId().equals(actual.getId())) break fileComparing;
                if (!current.getPath().equals(actual.getPath())) break fileComparing;
            }
            filesAndFoldersChanged = false;
        }

        return ResponseEntity.ok(
                new ActualizationResponseDto(true, true, false,
                        filesAndFoldersChanged,
                        project.getProjectUsers()
                                .stream()
                                .filter(projectUser -> projectUser.getLastChange() != null)
                                .sorted(Comparator.comparing(ProjectUser::getLastChange).reversed())
                                .limit(5)
                                .map(ChangeInfoDto::from)
                                .toList()));
    }
}
