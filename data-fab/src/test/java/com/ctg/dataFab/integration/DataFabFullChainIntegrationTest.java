package com.ctg.dataFab.integration;

import com.ctg.dataFab.DataFabApplication;
import com.ctg.dataFab.delivery.dto.PublishRequest;
import com.ctg.dataFab.ingest.dto.DatasetCreateRequest;
import com.ctg.dataFab.label.dto.CreateLabelTaskRequest;
import com.ctg.dataFab.qa.dto.EvaluateRequest;
import com.ctg.dataFab.qa.dto.EvaluateResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据集建设子系统全链路集成测试
 * 测试完整的数据集建设流程：数据接入 → 清洗 → 标注 → 质量评估 → 发布
 *
 * @author Developer
 * @since 2026-07-03
 */
@SpringBootTest(classes = DataFabApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "test-user", roles = {"ADMIN", "OPERATOR"})
@DisplayName("数据集建设全链路集成测试")
public class DataFabFullChainIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("完整流程：数据接入 → 清洗 → 标注 → 质量评估")
    public void testFullDataBuildingChain() throws Exception {
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
        String qaResponse = evaluateDataset(datasetId);
        assertNotNull(qaResponse, "质量评估失败");

        Map<String, Object> responseMap = objectMapper.readValue(qaResponse, Map.class);
        assertNotNull(responseMap.get("data"), "质量评估结果不应为空");
    }

    private Long createDataset() throws Exception {
        DatasetCreateRequest request = new DatasetCreateRequest();
        request.setName("测试数据集-" + System.currentTimeMillis());
        request.setDescription("集成测试数据集");
        request.setModality(1); // 文本
        request.setDataLevel(2); // 内部数据
        request.setVersion("1.0.0");
        request.setTags("[\"测试\",\"集成\"]");

        MvcResult result = mockMvc.perform(post("/api/v1/data/datasets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
        return ((Number) responseMap.get("data")).longValue();
    }

    private Long createEtlTask(Long datasetId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/etl/tasks")
                .param("datasetId", datasetId.toString())
                .param("taskName", "integration-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
        return ((Number) responseMap.get("data")).longValue();
    }

    private void executeEtlTask(Long taskId) throws Exception {
        mockMvc.perform(post("/api/v1/etl/tasks/" + taskId + "/execute"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private Long createLabelTask(Long datasetId) throws Exception {
        CreateLabelTaskRequest request = new CreateLabelTaskRequest();
        request.setDatasetId(datasetId);
        request.setTaskName("集成测试标注任务");
        request.setModality(1); // 文本
        request.setLabelType("classification");
        request.setLabelSchema("{\"type\":\"object\",\"properties\":{\"category\":{\"type\":\"string\"}}}");
        request.setAnnotatorIds(Arrays.asList(1L, 2L));
        request.setDoubleBlind(1);

        MvcResult result = mockMvc.perform(post("/api/v1/label/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
        return ((Number) responseMap.get("data")).longValue();
    }

    private String evaluateDataset(Long datasetId) throws Exception {
        EvaluateRequest request = new EvaluateRequest();
        request.setDatasetId(datasetId);
        request.setModality(1); // 文本
        request.setBatch("test-batch");
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("completeness", 0.95);
        metrics.put("accuracy", 0.90);
        request.setMetrics(metrics);

        MvcResult result = mockMvc.perform(post("/api/v1/qa/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        return result.getResponse().getContentAsString();
    }

    @Test
    @DisplayName("数据分级访问控制测试")
    public void testDataClassificationAccessControl() throws Exception {
        // 测试L1公开数据可以被所有用户访问
        mockMvc.perform(get("/api/v1/data/datasets")
                .param("dataLevel", "1"))
                .andExpect(status().isOk());

        // 测试L4机密数据需要特殊权限（这里只是验证接口可访问）
        mockMvc.perform(get("/api/v1/data/datasets")
                .param("dataLevel", "4"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("标注一致性计算测试")
    public void testLabelConsistencyCalculation() throws Exception {
        // 使用data.sql中已初始化的标注任务（ID=1，已有标注记录和kappa分数）
        // 计算IAA得分
        mockMvc.perform(get("/api/v1/label/tasks/1/iaa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("质量门禁检查测试")
    public void testQualityGateCheck() throws Exception {
        Long datasetId = createDataset();

        // 执行质量评估
        String response = evaluateDataset(datasetId);
        assertNotNull(response, "质量评估响应为空");

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        assertNotNull(responseMap.get("data"), "质量门禁结果为空");
    }

    @Test
    @DisplayName("L4数据下载阻断测试")
    public void testL4DataDownloadBlocking() throws Exception {
        // 创建L4机密数据集
        DatasetCreateRequest request = new DatasetCreateRequest();
        request.setName("L4机密数据集-" + System.currentTimeMillis());
        request.setDescription("L4机密数据测试");
        request.setModality(1); // 文本
        request.setDataLevel(4); // L4机密数据
        request.setVersion("1.0.0");

        MvcResult createResult = mockMvc.perform(post("/api/v1/data/datasets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
        Long datasetId = ((Number) responseMap.get("data")).longValue();
        assertNotNull(datasetId, "L4数据集创建失败");

        // 尝试通过delivery端点下载L4数据集（需要userId参数）
        // DeliveryController: GET /api/v1/delivery/{id}/download?userId=xxx
        // 由于L4数据需要审批，下载应被阻断，返回success=false
        MvcResult downloadResult = mockMvc.perform(get("/api/v1/delivery/" + datasetId + "/download")
                .param("userId", "1"))
                .andReturn();

        // 验证下载被阻断 — L4数据应返回success=false
        String downloadBody = downloadResult.getResponse().getContentAsString();
        Map<String, Object> downloadMap = objectMapper.readValue(downloadBody, Map.class);
        assertFalse((Boolean) downloadMap.get("success"), "L4数据下载应被阻断，success应为false");
    }
}
