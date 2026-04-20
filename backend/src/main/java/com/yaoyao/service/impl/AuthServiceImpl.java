package com.yaoyao.service.impl;

import cn.hutool.core.util.StrUtil;
import com.yaoyao.config.AppProperties;
import com.yaoyao.service.AuthService;
import com.yaoyao.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AppProperties appProperties;
    private final JwtUtil jwtUtil;

    @Override
    public Map<String, Object> login(String username, String password) {
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            throw new IllegalArgumentException("请输入账号和密码");
        }

        if (!username.trim().equals(appProperties.getAdmin().getUsername())
                || !password.equals(appProperties.getAdmin().getPassword())) {
            throw new IllegalArgumentException("账号或密码错误");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", 1);
        claims.put("username", appProperties.getAdmin().getUsername());
        claims.put("name", "瑶瑶");

        String token = jwtUtil.generateToken(claims);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("user", Map.of(
                "id", 1,
                "username", appProperties.getAdmin().getUsername(),
                "name", "瑶瑶"
        ));
        return result;
    }

    @Override
    public Map<String, Object> getMe(String token) {
        if (StrUtil.isBlank(token)) {
            throw new IllegalArgumentException("未授权");
        }

        Claims claims = jwtUtil.parseToken(token);
        Map<String, Object> result = new HashMap<>();
        result.put("user", Map.of(
                "id", claims.get("id"),
                "username", claims.get("username"),
                "name", claims.get("name")
        ));
        return result;
    }
}
