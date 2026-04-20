package com.yaoyao.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GlobalExceptionHandler 全局异常处理器测试")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Nested
    @DisplayName("IllegalArgumentException 处理测试")
    class IllegalArgumentTests {

        @Test
        @DisplayName("处理IllegalArgumentException返回400")
        void handleIllegalArgumentException() {
            IllegalArgumentException ex = new IllegalArgumentException("参数错误");
            Result<?> result = handler.handleIllegalArgumentException(ex);

            assertEquals(400, result.getCode());
            assertEquals("参数错误", result.getMessage());
            assertNull(result.getData());
        }

        @Test
        @DisplayName("处理空消息的IllegalArgumentException")
        void handleIllegalArgumentExceptionWithNullMessage() {
            IllegalArgumentException ex = new IllegalArgumentException();
            Result<?> result = handler.handleIllegalArgumentException(ex);

            assertEquals(400, result.getCode());
        }
    }

    @Nested
    @DisplayName("MissingServletRequestParameterException 处理测试")
    class MissingParamTests {

        @Test
        @DisplayName("缺少必要参数返回400")
        void handleMissingParam() {
            MissingServletRequestParameterException ex =
                    new MissingServletRequestParameterException("page", "int");

            Result<?> result = handler.handleMissingParamException(ex);

            assertEquals(400, result.getCode());
            assertTrue(result.getMessage().contains("page"));
        }
    }

    @Nested
    @DisplayName("MaxUploadSizeExceededException 处理测试")
    class MaxUploadSizeTests {

        @Test
        @DisplayName("文件大小超限返回400")
        void handleMaxUploadSize() {
            MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(50 * 1024 * 1024);

            Result<?> result = handler.handleMaxUploadSizeExceededException(ex);

            assertEquals(400, result.getCode());
            assertTrue(result.getMessage().contains("50MB"));
        }
    }

    @Nested
    @DisplayName("RuntimeException 处理测试")
    class RuntimeTests {

        @Test
        @DisplayName("运行时异常返回500")
        void handleRuntimeException() {
            RuntimeException ex = new RuntimeException("服务器内部错误");

            Result<?> result = handler.handleRuntimeException(ex);

            assertEquals(500, result.getCode());
            assertEquals("服务器内部错误", result.getMessage());
        }
    }

    @Nested
    @DisplayName("Exception 处理测试")
    class ExceptionTests {

        @Test
        @DisplayName("未知异常返回500")
        void handleGenericException() {
            Exception ex = new Exception("未知错误");

            Result<?> result = handler.handleException(ex);

            assertEquals(500, result.getCode());
            assertEquals("服务器内部错误", result.getMessage());
            assertNull(result.getData());
        }
    }

    @Nested
    @DisplayName("ResultCode 枚举完整性测试")
    class ResultCodeTests {

        @Test
        @DisplayName("所有错误码都有正确的code和message")
        void allResultCodesAreValid() {
            for (ResultCode code : ResultCode.values()) {
                assertNotNull(code.getMessage());
                assertTrue(code.getCode() > 0);
            }
        }

        @Test
        @DisplayName("SUCCESS码为200")
        void successCodeIs200() {
            assertEquals(200, ResultCode.SUCCESS.getCode());
        }

        @Test
        @DisplayName("BAD_REQUEST码为400")
        void badRequestCodeIs400() {
            assertEquals(400, ResultCode.BAD_REQUEST.getCode());
        }

        @Test
        @DisplayName("UNAUTHORIZED码为401")
        void unauthorizedCodeIs401() {
            assertEquals(401, ResultCode.UNAUTHORIZED.getCode());
        }

        @Test
        @DisplayName("INTERNAL_ERROR码为500")
        void internalErrorCodeIs500() {
            assertEquals(500, ResultCode.INTERNAL_ERROR.getCode());
        }
    }
}
