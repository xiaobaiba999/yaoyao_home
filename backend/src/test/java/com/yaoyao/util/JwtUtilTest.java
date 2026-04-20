package com.yaoyao.util;

import com.yaoyao.config.AppProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtil 工具类测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private AppProperties appProperties;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties();
        appProperties.getJwt().setSecret("yaoyao-memorial-secret-key-2024-test-key-long-enough");
        appProperties.getJwt().setExpiresIn(604800000L);
        jwtUtil = new JwtUtil(appProperties);
    }

    @Nested
    @DisplayName("generateToken 方法测试")
    class GenerateTokenTests {

        @Test
        @DisplayName("正常生成Token")
        void generateTokenSuccessfully() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", 1);
            claims.put("username", "yaoyao");

            String token = jwtUtil.generateToken(claims);
            assertNotNull(token);
            assertFalse(token.isEmpty());
            assertTrue(token.split("\\.").length == 3);
        }

        @Test
        @DisplayName("生成Token包含所有claims")
        void tokenContainsAllClaims() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", 1);
            claims.put("username", "yaoyao");
            claims.put("name", "瑶瑶");

            String token = jwtUtil.generateToken(claims);
            Claims parsed = jwtUtil.parseToken(token);

            assertEquals(1, parsed.get("id"));
            assertEquals("yaoyao", parsed.get("username"));
            assertEquals("瑶瑶", parsed.get("name"));
        }

        @Test
        @DisplayName("空claims也能生成Token")
        void generateTokenWithEmptyClaims() {
            Map<String, Object> claims = new HashMap<>();
            String token = jwtUtil.generateToken(claims);
            assertNotNull(token);
        }
    }

    @Nested
    @DisplayName("parseToken 方法测试")
    class ParseTokenTests {

        @Test
        @DisplayName("正确解析有效Token")
        void parseValidToken() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", 1);
            claims.put("username", "yaoyao");

            String token = jwtUtil.generateToken(claims);
            Claims parsed = jwtUtil.parseToken(token);

            assertEquals(1, parsed.get("id"));
            assertEquals("yaoyao", parsed.get("username"));
        }

        @Test
        @DisplayName("无效Token抛出异常")
        void parseInvalidTokenThrows() {
            assertThrows(Exception.class, () -> jwtUtil.parseToken("invalid.token.string"));
        }

        @Test
        @DisplayName("篡改的Token抛出异常")
        void parseTamperedTokenThrows() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", 1);
            String token = jwtUtil.generateToken(claims);
            String tamperedToken = token + "tampered";
            assertThrows(Exception.class, () -> jwtUtil.parseToken(tamperedToken));
        }
    }

    @Nested
    @DisplayName("validateToken 方法测试")
    class ValidateTokenTests {

        @Test
        @DisplayName("有效Token验证通过")
        void validateValidToken() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", 1);
            String token = jwtUtil.generateToken(claims);
            assertTrue(jwtUtil.validateToken(token));
        }

        @Test
        @DisplayName("null Token验证失败")
        void validateNullToken() {
            assertFalse(jwtUtil.validateToken(null));
        }

        @Test
        @DisplayName("空字符串Token验证失败")
        void validateEmptyToken() {
            assertFalse(jwtUtil.validateToken(""));
        }

        @Test
        @DisplayName("空白字符串Token验证失败")
        void validateBlankToken() {
            assertFalse(jwtUtil.validateToken("   "));
        }

        @Test
        @DisplayName("无效Token验证失败")
        void validateInvalidToken() {
            assertFalse(jwtUtil.validateToken("invalid.token.string"));
        }
    }

    @Nested
    @DisplayName("getClaimFromToken 方法测试")
    class GetClaimTests {

        @Test
        @DisplayName("获取Token中的指定claim")
        void getClaimFromToken() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("username", "yaoyao");
            String token = jwtUtil.generateToken(claims);

            String username = jwtUtil.getClaimFromToken(token, "username");
            assertEquals("yaoyao", username);
        }

        @Test
        @DisplayName("获取不存在的claim返回null")
        void getNonExistentClaim() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("username", "yaoyao");
            String token = jwtUtil.generateToken(claims);

            String result = jwtUtil.getClaimFromToken(token, "nonexistent");
            assertNull(result);
        }
    }
}
