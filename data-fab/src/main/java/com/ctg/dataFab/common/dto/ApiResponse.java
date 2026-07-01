package com.ctg.dataFab.common.dto;

import lombok.Data;

/**
 * 统一API响应信封
 * 符合技术方案 §6.2 统一响应信封规范
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

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setCode(0);
        response.setMessage("ok");
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setCode(code);
        response.setMessage(message);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    /**
     * 失败响应（带数据）
     */
    public static <T> ApiResponse<T> error(Integer code, String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setCode(code);
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
}
