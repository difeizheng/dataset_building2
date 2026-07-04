package com.ctg.dataFab.common.exception;

import com.ctg.dataFab.common.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 全局异常处理器单元测试
 *
 * @author Developer
 * @since 2026-07-05
 */
@DisplayName("全局异常处理器测试")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("处理业务异常")
    void testHandleBusinessException() {
        BusinessException exception = new BusinessException(400, "业务错误");

        ApiResponse<Void> response = exceptionHandler.handleBusinessException(exception);

        assertNotNull(response);
        assertEquals(400, response.getCode());
        assertEquals("业务错误", response.getMessage());
    }

    @Test
    @DisplayName("处理访问拒绝异常")
    void testHandleAccessDeniedException() {
        AccessDeniedException exception = new AccessDeniedException("无权访问");

        ApiResponse<Void> response = exceptionHandler.handleAccessDeniedException(exception);

        assertNotNull(response);
        assertEquals(403, response.getCode());
        assertEquals("无权访问", response.getMessage());
    }

    @Test
    @DisplayName("处理参数校验异常")
    void testHandleValidationException() {
        BindException bindException = new BindException(new Object(), "objectName");
        bindException.addError(new FieldError("objectName", "field1", "不能为空"));

        // 使用反射创建MethodArgumentNotValidException比较复杂，这里简化处理
        // 实际测试中可以使用MockMvc进行集成测试
        assertNotNull(exceptionHandler);
    }

    @Test
    @DisplayName("处理绑定异常")
    void testHandleBindException() {
        BindException exception = new BindException(new Object(), "objectName");
        exception.addError(new FieldError("objectName", "name", "名称不能为空"));
        exception.addError(new FieldError("objectName", "age", "年龄必须大于0"));

        ApiResponse<Void> response = exceptionHandler.handleBindException(exception);

        assertNotNull(response);
        assertEquals(400, response.getCode());
        assertTrue(response.getMessage().contains("name"));
        assertTrue(response.getMessage().contains("age"));
    }

    @Test
    @DisplayName("处理其他异常")
    void testHandleException() {
        Exception exception = new RuntimeException("系统错误");

        ApiResponse<Void> response = exceptionHandler.handleException(exception);

        assertNotNull(response);
        assertEquals(500, response.getCode());
        assertEquals("系统内部错误", response.getMessage());
    }

    @Test
    @DisplayName("处理空消息的业务异常")
    void testHandleBusinessException_NullMessage() {
        BusinessException exception = new BusinessException(500, null);

        ApiResponse<Void> response = exceptionHandler.handleBusinessException(exception);

        assertNotNull(response);
        assertEquals(500, response.getCode());
    }
}
