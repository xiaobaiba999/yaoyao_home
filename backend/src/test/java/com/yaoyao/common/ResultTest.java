package com.yaoyao.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Result 统一响应测试")
class ResultTest {

    @Nested
    @DisplayName("success 方法测试")
    class SuccessTests {

        @Test
        @DisplayName("success() - 无数据成功响应")
        void successWithoutData() {
            Result<Void> result = Result.success();
            assertEquals(200, result.getCode());
            assertEquals("操作成功", result.getMessage());
            assertNull(result.getData());
        }

        @Test
        @DisplayName("success(data) - 带数据成功响应")
        void successWithData() {
            Result<String> result = Result.success("hello");
            assertEquals(200, result.getCode());
            assertEquals("操作成功", result.getMessage());
            assertEquals("hello", result.getData());
        }

        @Test
        @DisplayName("success(message, data) - 自定义消息成功响应")
        void successWithMessageAndData() {
            Result<String> result = Result.success("自定义消息", "data");
            assertEquals(200, result.getCode());
            assertEquals("自定义消息", result.getMessage());
            assertEquals("data", result.getData());
        }

        @Test
        @DisplayName("success(data) - data为null时正常返回")
        void successWithNullData() {
            Result<String> result = Result.success(null);
            assertEquals(200, result.getCode());
            assertNull(result.getData());
        }

        @Test
        @DisplayName("success(data) - data为复杂对象")
        void successWithComplexData() {
            java.util.Map<String, Object> data = java.util.Map.of("key", "value", "num", 123);
            Result<java.util.Map<String, Object>> result = Result.success(data);
            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
            assertEquals("value", result.getData().get("key"));
            assertEquals(123, result.getData().get("num"));
        }
    }

    @Nested
    @DisplayName("error 方法测试")
    class ErrorTests {

        @Test
        @DisplayName("error(ResultCode) - 使用错误码枚举")
        void errorWithResultCode() {
            Result<?> result = Result.error(ResultCode.BAD_REQUEST);
            assertEquals(400, result.getCode());
            assertEquals("请求参数错误", result.getMessage());
            assertNull(result.getData());
        }

        @Test
        @DisplayName("error(code, message) - 自定义错误码和消息")
        void errorWithCodeAndMessage() {
            Result<?> result = Result.error(1001, "自定义错误");
            assertEquals(1001, result.getCode());
            assertEquals("自定义错误", result.getMessage());
            assertNull(result.getData());
        }

        @Test
        @DisplayName("error(message) - 仅消息，使用500错误码")
        void errorWithMessage() {
            Result<?> result = Result.error("服务器出错了");
            assertEquals(500, result.getCode());
            assertEquals("服务器出错了", result.getMessage());
            assertNull(result.getData());
        }

        @Test
        @DisplayName("error(ResultCode.UNAUTHORIZED) - 401未授权")
        void errorUnauthorized() {
            Result<?> result = Result.error(ResultCode.UNAUTHORIZED);
            assertEquals(401, result.getCode());
            assertEquals("未授权", result.getMessage());
        }

        @Test
        @DisplayName("error(ResultCode.NOT_FOUND) - 404资源不存在")
        void errorNotFound() {
            Result<?> result = Result.error(ResultCode.NOT_FOUND);
            assertEquals(404, result.getCode());
            assertEquals("资源不存在", result.getMessage());
        }
    }
}
