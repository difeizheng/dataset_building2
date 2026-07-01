package com.ctg.dataFab.classification.engine;

import com.ctg.dataFab.common.enums.DataLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据分级引擎单元测试
 *
 * @author Developer
 * @since 2026-07-01
 */
@DisplayName("数据分级引擎测试")
class DataClassificationEngineTest {

    @Test
    @DisplayName("测试L4核心数据分级 - 关键词匹配")
    void testClassify_L4Core_KeywordMatch() {
        DataClassificationEngine.ClassificationContext context =
                new DataClassificationEngine.ClassificationContext(
                        "核心机密数据",
                        "包含国家安全相关的核心算法",
                        "text/plain"
                );

        DataClassificationEngine.ClassificationResult result =
                DataClassificationEngine.classify(context);

        assertEquals(DataLevel.L4_CORE, result.getLevel());
        assertTrue(result.isRequiresEncryption());
        assertTrue(result.isBlockDownload());
        assertTrue(result.isBlockPrint());
        assertTrue(result.isBlockExport());
        assertTrue(result.getReason().contains("L4"));
    }

    @Test
    @DisplayName("测试L3敏感数据分级 - 关键词匹配")
    void testClassify_L3Sensitive_KeywordMatch() {
        DataClassificationEngine.ClassificationContext context =
                new DataClassificationEngine.ClassificationContext(
                        "客户敏感信息",
                        "包含身份证号和手机号",
                        "text/plain"
                );

        DataClassificationEngine.ClassificationResult result =
                DataClassificationEngine.classify(context);

        assertEquals(DataLevel.L3_SENSITIVE, result.getLevel());
        assertTrue(result.isRequiresEncryption());
        assertFalse(result.isBlockDownload());
        assertTrue(result.getReason().contains("L3"));
    }

    @Test
    @DisplayName("测试L2内部数据分级 - 关键词匹配")
    void testClassify_L2Internal_KeywordMatch() {
        DataClassificationEngine.ClassificationContext context =
                new DataClassificationEngine.ClassificationContext(
                        "内部工作文档",
                        "一般业务会议纪要",
                        "text/plain"
                );

        DataClassificationEngine.ClassificationResult result =
                DataClassificationEngine.classify(context);

        assertEquals(DataLevel.L2_INTERNAL, result.getLevel());
        assertFalse(result.isRequiresEncryption());
        assertFalse(result.isBlockDownload());
    }

    @Test
    @DisplayName("测试L1公开数据分级 - 默认分级")
    void testClassify_L1Public_Default() {
        DataClassificationEngine.ClassificationContext context =
                new DataClassificationEngine.ClassificationContext(
                        "普通数据",
                        "无特殊标记的数据",
                        "text/plain"
                );

        DataClassificationEngine.ClassificationResult result =
                DataClassificationEngine.classify(context);

        assertEquals(DataLevel.L1_PUBLIC, result.getLevel());
        assertFalse(result.isRequiresEncryption());
        assertFalse(result.isBlockDownload());
    }

    @Test
    @DisplayName("测试分级结果包含安全要求")
    void testClassify_SecurityRequirements() {
        DataClassificationEngine.ClassificationContext context =
                new DataClassificationEngine.ClassificationContext(
                        "绝密文件",
                        "核心专利数据",
                        "text/plain"
                );

        DataClassificationEngine.ClassificationResult result =
                DataClassificationEngine.classify(context);

        assertEquals(DataLevel.L4_CORE, result.getLevel());
        assertTrue(result.isRequiresEncryption());
        assertTrue(result.isRequiresApproval());
        assertTrue(result.isBlockDownload());
        assertTrue(result.isBlockPrint());
        assertTrue(result.isBlockExport());
        assertTrue(result.isRequireWatermark());
    }

    @Test
    @DisplayName("测试分级原因生成")
    void testClassify_ReasonGeneration() {
        DataClassificationEngine.ClassificationContext context =
                new DataClassificationEngine.ClassificationContext(
                        "敏感财务数据",
                        "包含财务报表",
                        "text/plain"
                );

        DataClassificationEngine.ClassificationResult result =
                DataClassificationEngine.classify(context);

        assertNotNull(result.getReason());
        assertTrue(result.getReason().contains("数据分级为"));
        assertTrue(result.getReason().contains("需要加密保护"));
    }
}
