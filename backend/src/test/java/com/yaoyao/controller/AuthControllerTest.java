package com.yaoyao.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaoyao.common.PageResult;
import com.yaoyao.config.AppProperties;
import com.yaoyao.entity.Message;
import com.yaoyao.service.AuthService;
import com.yaoyao.service.MessageService;
import com.yaoyao.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuthController 认证接口测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Nested
    @DisplayName("POST /api/auth/login 登录接口测试")
    class LoginTests {

        @Test
        @DisplayName("登录成功返回200和token")
        void loginSuccess() throws Exception {
            Map<String, Object> loginResult = new HashMap<>();
            loginResult.put("message", "登录成功");
            loginResult.put("token", "test-jwt-token");
            loginResult.put("user", Map.of("id", 1, "username", "yaoyao", "name", "瑶瑶"));

            when(authService.login("yaoyao", "yaoyao20230228")).thenReturn(loginResult);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("username", "yaoyao", "password", "yaoyao20230228"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("操作成功"))
                    .andExpect(jsonPath("$.data.token").value("test-jwt-token"))
                    .andExpect(jsonPath("$.data.user.username").value("yaoyao"));
        }

        @Test
        @DisplayName("空用户名登录返回400")
        void loginWithBlankUsername() throws Exception {
            when(authService.login(any(), any()))
                    .thenThrow(new IllegalArgumentException("请输入账号和密码"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("username", "", "password", "yaoyao20230228"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("错误密码登录返回400")
        void loginWithWrongPassword() throws Exception {
            when(authService.login(any(), any()))
                    .thenThrow(new IllegalArgumentException("账号或密码错误"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("username", "yaoyao", "password", "wrong"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("账号或密码错误"));
        }
    }

    @Nested
    @DisplayName("GET /api/auth/me 获取用户信息测试")
    class GetMeTests {

        @Test
        @DisplayName("有效Token获取用户信息成功")
        void getMeSuccess() throws Exception {
            Map<String, Object> meResult = new HashMap<>();
            meResult.put("user", Map.of("id", 1, "username", "yaoyao", "name", "瑶瑶"));

            when(authService.getMe("valid-token")).thenReturn(meResult);

            mockMvc.perform(get("/api/auth/me")
                            .header("Authorization", "Bearer valid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.user.username").value("yaoyao"));
        }

        @Test
        @DisplayName("无Token获取用户信息返回400")
        void getMeWithoutToken() throws Exception {
            when(authService.getMe(any())).thenThrow(new IllegalArgumentException("未授权"));

            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("未授权"));
        }
    }
}
