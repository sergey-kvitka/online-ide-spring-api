package com.kvitka.spring_api.controllers;

import com.kvitka.spring_api.dtos.UserInfoDto;
import com.kvitka.spring_api.dtos.UserMainInfoDto;
import com.kvitka.spring_api.dtos.UserNamesDto;
import com.kvitka.spring_api.entities.User;
import com.kvitka.spring_api.security.jwt.JwtTokenProvider;
import com.kvitka.spring_api.services.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/usernameLike")
    public List<UserMainInfoDto> getUsersByUsernameLike(@RequestBody String username) {
        return userService.findByUsernameLike(username).stream()
                .map(user -> new UserMainInfoDto(
                        new UserNamesDto(
                                user.getUsername(),
                                user.getFirstName(),
                                user.getLastName()),
                        userService.findLastChangeByUserId(user.getUserId())
                ))
                .toList();
    }

    @GetMapping("/info")
    public UserInfoDto getUserInfo(@RequestHeader("Authorization") String bearerToken) {
        User user = jwtTokenProvider.getUserByBearerToken(bearerToken);
        return UserInfoDto.builder()
                .userNames(new UserNamesDto(
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName()))
                .userId(user.getUserId())
                .email(user.getEmail())
                .build();
    }
}
