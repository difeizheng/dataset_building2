package com.ctg.integration.exception;

/**
 * 账户锁定异常
 *
 * @author CTG
 * @since 2026-07-01
 */
public class AccountLockedException extends RuntimeException {

    public AccountLockedException(String message) {
        super(message);
    }

    public AccountLockedException(String message, Throwable cause) {
        super(message, cause);
    }
}
