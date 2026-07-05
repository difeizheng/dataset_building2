package com.ctg.dataFab.crypto;

/**
 * 国密 SM4 加密/解密操作异常
 *
 * @author Security Officer
 * @since 2026-07-05
 */
public class Sm4EncryptionException extends RuntimeException {

    public Sm4EncryptionException(String message) {
        super(message);
    }

    public Sm4EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
