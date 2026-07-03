package com.ctg.dataFab.integration;

import com.ctg.dataFab.DataFabApplication;
import com.ctg.dataFab.common.dto.ApiResponse;
import com.ctg.dataFab.delivery.dto.PublishRequest;
import com.ctg.dataFab.delivery.dto.PublishResponse;
import com.ctg.dataFab.etl.entity.EtlTask;
import com.ctg.dataFab.ingest.dto.DatasetCreateRequest;
import com.ctg.dataFab.label.dto.CreateLabelTaskRequest;
import com.ctg.dataFab.qa.dto.EvaluateRequest;
import com.ctg.dataFab.qa.dto.EvaluateResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据集建设子系统全链路集成测试
 * 测试完整的数据集建设流程：数据接入 → 清洗 → 标注 → 质量评估 → 发布
 *
 * @author Developer
 * @since 2026-07-03
 */
@SpringBootTest(
    classes = DataFabApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@DisplayName("数据集建设全链路集成测试")
public class DataFabFullChainIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    @Test
    @DisplayName("完整流程：数据接入 → 清洗 → 标注 → 质量评估 → 发布")
    public void testFullDataBuildingChain() {
        // Step 1: 创建数据集
        Long datasetId = createDataset();
        assertNotNull(datasetId, "数据集创建失败");

        // Step 2: 创建清洗任务
        Long etlTaskId = createEtlTask(datasetId);
        assertNotNull(etlTaskId, "清洗任务创建失败");

        // Step 3: 执行清洗任务
        executeEtlTask(etlTaskId);

        // Step 4: 创建标注任务
        Long labelTaskId = createLabelTask(datasetId);
        assertNotNull(labelTaskId, "标注任务创建失败");

        // Step 5: 执行质量评估
        EvaluateResponse qaResponse = evaluateDataset(datasetId);
        assertNotNull(qaResponse, "质量评估失败");

        // Step 6: 发布数据集
        PublishResponse publishResponse = publishDataset(datasetId);
        assertNotNull(publishResponse, "数据集发布失败");
    }

    private Long createDataset() {
        DatasetCreateRequest request = DatasetCreateRequest.builder()
            .datasetName("测试数据集-" + System.currentTimeMillis())
            .description("集成测试数据集")
            .modality(1) // 文本
            .dataLevel(2) // 内部数据
            .source("integration-test")
            .build();

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            getBaseUrl() + "/data/datasets",
            request,
            ApiResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        return (Long) response.getBody().getData();
    }

    private Long createEtlTask(Long datasetId) {
        Map<String, Object> params = new HashMap<>();
        params.put("datasetId", datasetId);
        params.put("taskName", "集成测试清洗任务");

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            getBaseUrl() + "/etl/tasks?datasetId=" + datasetId + "&taskName=integration-test",
            null,
            ApiResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        return (Long) response.getBody().getData();
    }

    private void executeEtlTask(Long taskId) {
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            getBaseUrl() + "/etl/tasks/" + taskId + "/execute",
            null,
            ApiResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
    }

    private Long createLabelTask(Long datasetId) {
        CreateLabelTaskRequest request = CreateLabelTaskRequest.builder()
            .datasetId(datasetId)
            .taskName("集成测试标注任务")
            .labelType(1) // 分类标注
            .assigneeIds(Arrays.asList(1L, 2L))
            .build();

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            getBaseUrl() + "/label/tasks",
            request,
            ApiResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        return (Long) response.getBody().getData();
    }

    private EvaluateResponse evaluateDataset(Long datasetId) {
        EvaluateRequest request = EvaluateRequest.builder()
            .datasetId(datasetId)
            .evaluationType("full")
            .build();

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            getBaseUrl() + "/qa/evaluate",
            request,
            ApiResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        return (EvaluateResponse) response.getBody().getData();
    }

    private PublishResponse publishDataset(Long datasetId) {
        PublishRequest request = PublishRequest.builder()
            .datasetId(datasetId)
            .version("1.0.0")
            .description("集成测试发布")
            .build();

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            getBaseUrl() + "/delivery/publish",
            request,
            ApiResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        return (PublishResponse) response.getBody().getData();
    }

    @Test
    @DisplayName("数据分级访问控制测试")
    public void testDataClassificationAccessControl() {
        // 测试L1公开数据可以被所有用户访问
        ResponseEntity<ApiResponse> l1Response = restTemplate.getForEntity(
            getBaseUrl() + "/data/datasets?dataLevel=1",
            ApiResponse.class
        );
        assertEquals(HttpStatus.OK, l1Response.getStatusCode());

        // 测试L4机密数据需要特殊权限（这里只是验证接口可访问）
        ResponseEntity<ApiResponse> l4Response = restTemplate.getForEntity(
            getBaseUrl() + "/data/datasets?dataLevel=4",
            ApiResponse.class
        );
        assertEquals(HttpStatus.OK, l4Response.getStatusCode());
    }

    @Test
    @DisplayName("标注一致性计算测试")
    public void testLabelConsistencyCalculation() {
        // 创建标注任务
        Long datasetId = createDataset();
        Long labelTaskId = createLabelTask(datasetId);

        // 计算IAA得分
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
            getBaseUrl() + "/label/tasks/" + labelTaskId + "/iaa",
            ApiResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        Double kappa = (Double) response.getBody().getData();
        assertNotNull(kappa, "IAA得分计算失败");
        assertTrue(kappa >= 0.0 && kappa <= 1.0, "IAA得分应在0-1之间");
    }

    @Test
    @DisplayName("质量门禁检查测试")
    public void testQualityGateCheck() {
        Long datasetId = createDataset();

        // 执行质量评估
        EvaluateResponse response = evaluateDataset(datasetId);

        assertNotNull(response, "质量评估响应为空");
        assertNotNull(response.getPassed(), "质量门禁结果为空");
        assertNotNull(response.getGateInfo(), "质量门禁详情为空");

        // 验证质量门禁包含必要的检查项
        assertFalse(response.getGateInfo().isEmpty(), "质量门禁检查项不应为空");
    }
}
