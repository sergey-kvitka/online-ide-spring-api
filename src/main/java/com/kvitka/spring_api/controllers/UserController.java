package com.kvitka.spring_api.controllers;

import com.kvitka.spring_api.entities.User;
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

    @PostMapping("usernameLike")
    public List<User> getUsersByUsernameLike(@RequestBody String username) {
        List<User> byUsernameLike = userService.findByUsernameLike(username);
        System.out.println(byUsernameLike);
        return byUsernameLike;
    }


}
