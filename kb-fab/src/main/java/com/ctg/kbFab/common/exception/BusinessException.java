package com.ctg.kbFab.common.exception;

/**
 * 业务异常
 *
 * @author Developer
 * @since 2026-07-01
 */
public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super(400, message);
    }

    public BusinessException(Integer code, String message) {
        super(code, message);
    }
}
