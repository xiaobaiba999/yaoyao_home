package com.yaoyao.controller;

import com.yaoyao.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/")
    public Result<Map<String, Object>> index() {
        return Result.success(Map.of("message", "瑶瑶纪念空间 API", "version", "3.0.0"));
    }

    @GetMapping("/api/health")
    public Result<Map<String, Object>> health() {
        return Result.success(Map.of("status", "ok", "timestamp", LocalDateTime.now().toString()));
    }
}
