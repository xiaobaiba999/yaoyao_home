package com.yaoyao.service.impl;

import com.yaoyao.config.AppProperties;
import com.yaoyao.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 认证服务测试")
class AuthServiceImplTest {

    @Mock
    private JwtUtil jwtUtil;

    @Spy
    private AppProperties appProperties = new AppProperties();

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        appProperties.getAdmin().setUsername("yaoyao");
        appProperties.getAdmin().setPassword("yaoyao20230228");
    }

    @Nested
    @DisplayName("login 登录测试")
    class LoginTests {

        @Test
        @DisplayName("正确账号密码登录成功")
        void loginSuccess() {
            when(jwtUtil.generateToken(any())).thenReturn("mock-token");

            Map<String, Object> result = authService.login("yaoyao", "yaoyao20230228");

            assertNotNull(result);
            assertEquals("登录成功", result.get("message"));
            assertEquals("mock-token", result.get("token"));

            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) result.get("user");
            assertNotNull(user);
            assertEquals(1, user.get("id"));
            assertEquals("yaoyao", user.get("username"));
            assertEquals("瑶瑶", user.get("name"));
        }

        @Test
        @DisplayName("用户名为空抛出异常")
        void loginWithBlankUsername() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> authService.login("", "yaoyao20230228"));
            assertEquals("请输入账号和密码", ex.getMessage());
        }

        @Test
        @DisplayName("密码为空抛出异常")
        void loginWithBlankPassword() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> authService.login("yaoyao", ""));
            assertEquals("请输入账号和密码", ex.getMessage());
        }

        @Test
        @DisplayName("用户名为null抛出异常")
        void loginWithNullUsername() {
            assertThrows(IllegalArgumentException.class,
                    () -> authService.login(null, "yaoyao20230228"));
        }

        @Test
        @DisplayName("密码为null抛出异常")
        void loginWithNullPassword() {
            assertThrows(IllegalArgumentException.class,
                    () -> authService.login("yaoyao", null));
        }

        @Test
        @DisplayName("错误用户名抛出异常")
        void loginWithWrongUsername() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> authService.login("wrong", "yaoyao20230228"));
            assertEquals("账号或密码错误", ex.getMessage());
        }

        @Test
        @DisplayName("错误密码抛出异常")
        void loginWithWrongPassword() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> authService.login("yaoyao", "wrong"));
            assertEquals("账号或密码错误", ex.getMessage());
        }

        @Test
        @DisplayName("用户名前后空格自动trim后匹配")
        void loginWithTrimmedUsername() {
            when(jwtUtil.generateToken(any())).thenReturn("mock-token");

            Map<String, Object> result = authService.login("  yaoyao  ", "yaoyao20230228");
            assertNotNull(result);
            assertEquals("登录成功", result.get("message"));
        }
    }

    @Nested
    @DisplayName("getMe 获取用户信息测试")
    class GetMeTests {

        @Test
        @DisplayName("有效Token获取用户信息成功")
        void getMeSuccess() {
            Claims mockClaims = mock(Claims.class);
            when(mockClaims.get("id")).thenReturn(1);
            when(mockClaims.get("username")).thenReturn("yaoyao");
            when(mockClaims.get("name")).thenReturn("瑶瑶");
            when(jwtUtil.parseToken("valid-token")).thenReturn(mockClaims);

            Map<String, Object> result = authService.getMe("valid-token");

            assertNotNull(result);
            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) result.get("user");
            assertNotNull(user);
            assertEquals(1, user.get("id"));
            assertEquals("yaoyao", user.get("username"));
            assertEquals("瑶瑶", user.get("name"));
        }

        @Test
        @DisplayName("空Token抛出异常")
        void getMeWithEmptyToken() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> authService.getMe(""));
            assertEquals("未授权", ex.getMessage());
        }

        @Test
        @DisplayName("null Token抛出异常")
        void getMeWithNullToken() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> authService.getMe(null));
            assertEquals("未授权", ex.getMessage());
        }
    }
}
