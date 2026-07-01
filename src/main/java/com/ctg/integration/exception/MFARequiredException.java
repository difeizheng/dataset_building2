package com.ctg.integration.exception;

/**
 * MFA验证异常
 *
 * @author CTG
 * @since 2026-07-01
 */
public class MFARequiredException extends RuntimeException {

    public MFARequiredException(String message) {
        super(message);
    }

    public MFARequiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
