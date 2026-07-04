package com.ctg.integration.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 数据脱敏引擎测试
 *
 * @author CTG
 * @since 2026-07-01
 */
class DataMaskingEngineTest {

    private DataMaskingEngine maskingEngine;

    @BeforeEach
    void setUp() {
        maskingEngine = new DataMaskingEngine();
    }

    @Test
    void testMaskPhone() {
        String phone = "13812345678";
        String masked = maskingEngine.mask(phone, "phone");
        assertEquals("138****5678", masked);
    }

    @Test
    void testMaskIdCard() {
        String idCard = "110101199001011234";
        String masked = maskingEngine.mask(idCard, "idCard");
        assertEquals("110***********1234", masked);
    }

    @Test
    void testMaskEmail() {
        String email = "test@example.com";
        String masked = maskingEngine.mask(email, "email");
        assertEquals("t***t@example.com", masked);
    }

    @Test
    void testMaskName() {
        String name = "张三";
        String masked = maskingEngine.mask(name, "name");
        assertEquals("张*", masked);
    }

    @Test
    void testMaskBankCard() {
        String bankCard = "6222021234561234";
        String masked = maskingEngine.mask(bankCard, "bankCard");
        assertEquals("6222 **** **** 1234", masked);
    }

    @Test
    void testMaskByLevelL1() {
        String data = "13812345678";
        String masked = maskingEngine.maskByLevel(data, "phone", "L1");
        assertEquals(data, masked); // L1公开数据不脱敏
    }

    @Test
    void testMaskByLevelL2() {
        String data = "13812345678";
        String masked = maskingEngine.maskByLevel(data, "phone", "L2");
        assertEquals("138**5678", masked); // L2轻度脱敏
    }

    @Test
    void testMaskByLevelL3() {
        String data = "13812345678";
        String masked = maskingEngine.maskByLevel(data, "phone", "L3");
        assertEquals("138****5678", masked); // L3中度脱敏
    }

    @Test
    void testMaskByLevelL4() {
        String data = "13812345678";
        String masked = maskingEngine.maskByLevel(data, "phone", "L4");
        assertEquals("******", masked); // L4完全隐藏
    }

    @Test
    void testMaskNullData() {
        String masked = maskingEngine.mask(null, "phone");
        assertNull(masked);
    }

    @Test
    void testMaskEmptyData() {
        String masked = maskingEngine.mask("", "phone");
        assertEquals("", masked);
    }
}
