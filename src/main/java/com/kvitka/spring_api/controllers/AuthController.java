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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserServiceImpl userService;

    @PostMapping("login")
    public AuthenticationResponseDto login(@RequestBody AuthenticationRequestDto authenticationRequestDto) {
        log.info("login {}", authenticationRequestDto);
        try {
            String username = authenticationRequestDto.getUsername();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    username, authenticationRequestDto.getPassword()));
            User user = userService.findByUsername(username);
            if (user == null) throw new UsernameNotFoundException("");
            String token = jwtTokenProvider.createToken(username, user.getRoles());
            log.info("returning login response");
            return new AuthenticationResponseDto(token);
        } catch (AuthenticationException e) {
            log.error(e.getMessage());
            throw new BadCredentialsException("Неверные имя пользователя или пароль");
        }
    }

    @PostMapping("register")
    public AuthenticationResponseDto register(@RequestBody RegistrationRequestDto registrationRequestDto) {
        log.info("register {}", registrationRequestDto);
        String username = registrationRequestDto.getUsername();
        if (userService.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "Ошибка регистрации: пользователь с таким именем пользователя уже существует.");
        }
        User user = User.builder()
                .username(username)
                .password(registrationRequestDto.getPassword())
                .firstName(registrationRequestDto.getFirstName())
                .lastName(registrationRequestDto.getLastName())
                .email(registrationRequestDto.getEmail())
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
                .collect(Collectors.toList());
    }
}
