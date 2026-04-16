package com.example.authapp.controller;

import com.example.authapp.dto.response.UserSummaryResponse;
import com.example.authapp.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<UserSummaryResponse> users() {
        return userService.getAllUsers();
    }
}
