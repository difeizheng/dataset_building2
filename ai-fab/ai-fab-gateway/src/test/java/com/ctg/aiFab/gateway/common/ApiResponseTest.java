package com.ctg.aiFab.gateway.common;

import com.ctg.aiFab.gateway.common.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ApiResponse单元测试
 *
 * @author Developer
 * @since 2026-07-01
 */
class ApiResponseTest {

    @Test
    void testSuccess_withData() {
        ApiResponse<String> response = ApiResponse.success("test data");
        assertTrue(response.getSuccess());
        assertEquals(0, response.getCode());
        assertEquals("ok", response.getMessage());
        assertEquals("test data", response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testSuccess_noData() {
        ApiResponse<Void> response = ApiResponse.success();
        assertTrue(response.getSuccess());
        assertEquals(0, response.getCode());
        assertNull(response.getData());
    }

    @Test
    void testError() {
        ApiResponse<Void> response = ApiResponse.error(400, "参数错误");
        assertFalse(response.getSuccess());
        assertEquals(400, response.getCode());
        assertEquals("参数错误", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testError_withData() {
        ApiResponse<String> response = ApiResponse.error(500, "系统错误", "details");
        assertFalse(response.getSuccess());
        assertEquals(500, response.getCode());
        assertEquals("系统错误", response.getMessage());
        assertEquals("details", response.getData());
    }
}
