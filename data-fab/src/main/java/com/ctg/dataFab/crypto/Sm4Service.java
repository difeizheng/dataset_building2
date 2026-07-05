package com.ctg.dataFab.crypto;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SM4;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 国密 SM4 加解密服务接口
 *
 * @author Security Officer
 * @since 2026-07-05
 */
public interface Sm4Service {

    /**
     * 加密字符串
     *
     * @param plainText 明文
     * @return Base64 编码的密文
     * @throws Sm4EncryptionException 加密失败时抛出
     */
    String encrypt(String plainText);

    /**
     * 解密字符串
     *
     * @param cipherText Base64 编码的密文
     * @return 明文
     * @throws Sm4EncryptionException 解密失败时抛出
     */
    String decrypt(String cipherText);
}
