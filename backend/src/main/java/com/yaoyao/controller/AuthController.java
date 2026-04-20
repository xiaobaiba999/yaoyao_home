package com.yaoyao.controller;

import com.yaoyao.common.Result;
import com.yaoyao.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginForm) {
        Map<String, Object> data = authService.login(loginForm.get("username"), loginForm.get("password"));
        return Result.success(data);
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = authHeader != null && authHeader.startsWith("Bearer ")
                ? authHeader.substring(7) : "";
        Map<String, Object> data = authService.getMe(token);
        return Result.success(data);
    }
}
