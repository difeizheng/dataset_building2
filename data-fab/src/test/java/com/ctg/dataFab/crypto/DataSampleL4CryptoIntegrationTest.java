package com.ctg.dataFab.crypto;

import com.ctg.dataFab.common.enums.DataStatus;
import com.ctg.dataFab.ingest.entity.DataSample;
import com.ctg.dataFab.ingest.mapper.DataSampleMapper;
import com.ctg.dataFab.ingest.service.DataSampleService;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * L4 数据加密集成测试
 * 验证 L4 数据落盘时加密、读取时解密的完整流程
 *
 * @author Security Officer
 * @since 2026-07-05
 */
@SpringBootTest(classes = com.ctg.dataFab.DataFabApplication.class)
@ActiveProfiles("test")
@DisplayName("L4 数据加密集成测试")
class DataSampleL4CryptoIntegrationTest {

    @Autowired
    private DataSampleService dataSampleService;

    @Autowired
    private DataSampleMapper dataSampleMapper;

    @Test
    @DisplayName("L4 数据落盘是密文、读出是明文")
    void l4Data_saveEncrypted_readDecrypted() {
        // 准备测试数据
        String originalFilePath = "/data/samples/secret/image.jpg";
        String originalMetadata = "{\"sensitive\":true,\"owner\":\"admin\",\"level\":\"L4\"}";

        // 创建 L4 数据样本
        com.ctg.dataFab.ingest.dto.DataSampleCreateRequest request =
            new com.ctg.dataFab.ingest.dto.DataSampleCreateRequest();
        request.setName("L4 Crypto Test Sample");
        request.setModality(2); // 图像
        request.setDataLevel(4); // L4 核心
        request.setFilePath(originalFilePath);
        request.setMetadata(originalMetadata);
        request.setSource("integration-test");
        request.setMimeType("image/jpeg");

        Long sampleId = dataSampleService.createDataSample(request);

        // 验证 1：数据库中存储的是密文（不是明文）
        DataSample rawSample = dataSampleMapper.selectById(sampleId);
        assertThat(rawSample.getFilePath())
            .isNotEqualTo(originalFilePath)
            .isNotBlank();
        assertThat(rawSample.getMetadata())
            .isNotEqualTo(originalMetadata)
            .isNotBlank();

        // 验证 2：读取时自动解密返回明文
        DataSample decryptedSample = dataSampleService.getDataSampleById(sampleId);
        assertThat(decryptedSample.getFilePath()).isEqualTo(originalFilePath);
        assertThat(decryptedSample.getMetadata()).isEqualTo(originalMetadata);

        // 清理测试数据
        dataSampleMapper.deleteById(sampleId);
    }

    @Test
    @DisplayName("L2 数据不加密（明文存储）")
    void l2Data_notEncrypted() {
        String originalFilePath = "/data/samples/public/doc.pdf";
        String originalMetadata = "{\"public\":true}";

        com.ctg.dataFab.ingest.dto.DataSampleCreateRequest request =
            new com.ctg.dataFab.ingest.dto.DataSampleCreateRequest();
        request.setName("L2 Public Test Sample");
        request.setModality(1); // 文本
        request.setDataLevel(2); // L2 内部
        request.setFilePath(originalFilePath);
        request.setMetadata(originalMetadata);
        request.setSource("integration-test");
        request.setMimeType("application/pdf");

        Long sampleId = dataSampleService.createDataSample(request);

        // L2 数据应该是明文存储
        DataSample rawSample = dataSampleMapper.selectById(sampleId);
        assertThat(rawSample.getFilePath()).isEqualTo(originalFilePath);
        assertThat(rawSample.getMetadata()).isEqualTo(originalMetadata);

        // 清理测试数据
        dataSampleMapper.deleteById(sampleId);
    }

    @Test
    @DisplayName("L4 数据 updateDataSampleStatus 不破坏加密数据（无双重解密）")
    void l4Data_updateStatus_noDoubleDecryption() {
        // 准备测试数据
        String originalFilePath = "/data/samples/secret/report.xlsx";
        String originalMetadata = "{\"classification\":\"L4\",\"owner\":\"admin\"}";

        // 创建 L4 数据样本
        com.ctg.dataFab.ingest.dto.DataSampleCreateRequest request =
            new com.ctg.dataFab.ingest.dto.DataSampleCreateRequest();
        request.setName("L4 Update Status Test");
        request.setModality(1);
        request.setDataLevel(4);
        request.setFilePath(originalFilePath);
        request.setMetadata(originalMetadata);
        request.setSource("integration-test");
        request.setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        Long sampleId = dataSampleService.createDataSample(request);

        // 验证落盘是密文
        DataSample rawSampleBefore = dataSampleMapper.selectById(sampleId);
        assertThat(rawSampleBefore.getFilePath()).isNotEqualTo(originalFilePath);
        assertThat(rawSampleBefore.getMetadata()).isNotEqualTo(originalMetadata);
        assertThat(rawSampleBefore.getStatus()).isEqualTo(DataStatus.NEW.getCode());

        // update 状态（这里如果用 getDataSampleById 会导致双重解密 bug）
        dataSampleService.updateDataSampleStatus(sampleId, DataStatus.LABELED.getCode());

        // 验证数据库中仍然是密文（没有被双重解密成明文）
        DataSample rawSampleAfter = dataSampleMapper.selectById(sampleId);
        assertThat(rawSampleAfter.getFilePath())
            .isNotEqualTo(originalFilePath)
            .isEqualTo(rawSampleBefore.getFilePath());  // 密文应保持不变
        assertThat(rawSampleAfter.getMetadata())
            .isNotEqualTo(originalMetadata)
            .isEqualTo(rawSampleBefore.getMetadata());  // 密文应保持不变
        assertThat(rawSampleAfter.getStatus()).isEqualTo(DataStatus.LABELED.getCode());

        // 验证读取时仍能正确解密为明文
        DataSample decryptedSample = dataSampleService.getDataSampleById(sampleId);
        assertThat(decryptedSample.getFilePath()).isEqualTo(originalFilePath);
        assertThat(decryptedSample.getMetadata()).isEqualTo(originalMetadata);

        // 清理测试数据
        dataSampleMapper.deleteById(sampleId);
    }
}
