package com.ctg.kbFab.common.dto;

import lombok.Data;

/**
 * 统一API响应信封
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
public class ApiResponse<T> {

    private Boolean success;
    private Integer code;
    private String message;
    private T data;
    private String traceId;
    private Long timestamp;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setCode(0);
        response.setMessage("ok");
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setCode(code);
        response.setMessage(message);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
}
