package com.kvitka.spring_api.controllers;

import com.kvitka.spring_api.dtos.AuthenticationRequestDto;
import com.kvitka.spring_api.dtos.AuthenticationResponseDto;
import com.kvitka.spring_api.dtos.RegistrationRequestDto;
import com.kvitka.spring_api.entities.Role;
import com.kvitka.spring_api.entities.User;
import com.kvitka.spring_api.security.jwt.JwtTokenProvider;
import com.kvitka.spring_api.services.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserServiceImpl userService;

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
}
