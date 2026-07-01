package com.ctg.dataFab.common;

import com.ctg.dataFab.common.enums.DataLevel;
import com.ctg.dataFab.common.enums.DataModality;
import com.ctg.dataFab.common.enums.DataStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 枚举类单元测试
 *
 * @author Developer
 * @since 2026-07-01
 */
class EnumsTest {

    @Test
    void testDataLevel_fromCode() {
        assertEquals(DataLevel.L1_PUBLIC, DataLevel.fromCode(1));
        assertEquals(DataLevel.L2_INTERNAL, DataLevel.fromCode(2));
        assertEquals(DataLevel.L3_SENSITIVE, DataLevel.fromCode(3));
        assertEquals(DataLevel.L4_CORE, DataLevel.fromCode(4));
    }

    @Test
    void testDataLevel_invalidCode() {
        assertThrows(IllegalArgumentException.class, () -> DataLevel.fromCode(99));
    }

    @Test
    void testDataModality_fromCode() {
        assertEquals(DataModality.TEXT, DataModality.fromCode(1));
        assertEquals(DataModality.IMAGE, DataModality.fromCode(2));
        assertEquals(DataModality.AUDIO, DataModality.fromCode(3));
        assertEquals(DataModality.VIDEO, DataModality.fromCode(4));
    }

    @Test
    void testDataModality_fromName() {
        assertEquals(DataModality.TEXT, DataModality.fromName("文本"));
        assertEquals(DataModality.IMAGE, DataModality.fromName("图像"));
        assertEquals(DataModality.AUDIO, DataModality.fromName("音频"));
        assertEquals(DataModality.VIDEO, DataModality.fromName("视频"));
    }

    @Test
    void testDataStatus_fromCode() {
        assertEquals(DataStatus.NEW, DataStatus.fromCode(0));
        assertEquals(DataStatus.RAW, DataStatus.fromCode(1));
        assertEquals(DataStatus.CLEAN, DataStatus.fromCode(2));
        assertEquals(DataStatus.LABELED, DataStatus.fromCode(3));
        assertEquals(DataStatus.QA_PASS, DataStatus.fromCode(4));
        assertEquals(DataStatus.PUBLISHED, DataStatus.fromCode(6));
    }

    @Test
    void testDataLevel_properties() {
        assertEquals(1, DataLevel.L1_PUBLIC.getCode());
        assertEquals("公开", DataLevel.L1_PUBLIC.getName());
        assertNotNull(DataLevel.L1_PUBLIC.getDescription());
    }
}
