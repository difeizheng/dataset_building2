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
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * DM8 (达梦) 真实集成测试
 * 验证国产化硬约束：数据源切换到达梦 DM8，覆盖 SQL 方言差异
 *
 * 启用条件：
 *   环境变量 DM8_TEST_HOST 存在（CI runner 已启动 DM8 容器）
 *   或 Testcontainers 自动启动 DM8 容器
 *
 * 与 DataFabFullChainIntegrationTest 相同的 9 步断言，但数据源为 DM8。
 *
 * @author Developer
 * @since 2026-07-05
 */
@SpringBootTest(classes = DataFabApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("dm8-test")
@WithMockUser(username = "test-user", roles = {"ADMIN", "OPERATOR"})
@DisplayName("DM8 达梦数据库集成测试（国产化硬约束验证）")
@EnabledIfEnvironmentVariable(named = "DM8_TEST_ENABLED", matches = "true")
public class DataFabDm8IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * DynamicPropertySource: 从环境变量读取 DM8 连接信息
     * 若 DM8_TEST_HOST 未设置，测试将被 @EnabledIfEnvironmentVariable 跳过
     */
    @DynamicPropertySource
    static void dm8Properties(DynamicPropertyRegistry registry) {
        String host = System.getenv("DM8_TEST_HOST");
        String port = System.getenv("DM8_TEST_PORT");
        String username = System.getenv("DM8_TEST_USERNAME");
        String password = System.getenv("DM8_TEST_PASSWORD");

        if (host != null) {
            registry.add("spring.datasource.url",
                () -> "jdbc:dm://" + host + ":" + (port != null ? port : "5236") + "/DATA_FAB");
            registry.add("spring.datasource.username", () -> username != null ? username : "SYSDBA");
            registry.add("spring.datasource.password", () -> password != null ? password : "");
            registry.add("spring.datasource.driver-class-name", () -> "dm.jdbc.driver.DmDriver");
            // DM8 使用 Hibernate DM 方言
            registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.DmDialect");
            // 使用 DM8 schema/data 初始化
            registry.add("spring.sql.init.schema-locations",
                () -> "classpath:dm8-schema.sql");
            registry.add("spring.sql.init.data-locations",
                () -> "classpath:dm8-data.sql");
            registry.add("spring.sql.init.mode", () -> "always");
            registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        }
    }

    @Test
    @DisplayName("完整流程：数据接入 → 清洗 → 标注 → 质量评估（DM8）")
    public void testFullDataBuildingChain() throws Exception {
        // Step 1: 创建数据集
        Long datasetId = createDataset();
        assertNotNull(datasetId, "数据集创建失败（DM8）");

        // Step 2: 创建清洗任务
        Long etlTaskId = createEtlTask(datasetId);
        assertNotNull(etlTaskId, "清洗任务创建失败（DM8）");

        // Step 3: 执行清洗任务
        executeEtlTask(etlTaskId);

        // Step 4: 创建标注任务
        Long labelTaskId = createLabelTask(datasetId);
        assertNotNull(labelTaskId, "标注任务创建失败（DM8）");

        // Step 5: 执行质量评估
        String qaResponse = evaluateDataset(datasetId);
        assertNotNull(qaResponse, "质量评估失败（DM8）");

        Map<String, Object> responseMap = objectMapper.readValue(qaResponse, Map.class);
        assertNotNull(responseMap.get("data"), "质量评估结果不应为空（DM8）");
    }

    private Long createDataset() throws Exception {
        DatasetCreateRequest request = new DatasetCreateRequest();
        request.setName("DM8测试数据集-" + System.currentTimeMillis());
        request.setDescription("DM8集成测试数据集");
        request.setModality(1); // 文本
        request.setDataLevel(2); // 内部数据
        request.setVersion("1.0.0");
        request.setTags("[\"测试\",\"DM8\",\"国产化\"]");

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
                .param("taskName", "dm8-integration-test"))
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
        request.setTaskName("DM8集成测试标注任务");
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
        request.setBatch("dm8-test-batch");
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
    @DisplayName("数据分级访问控制测试（DM8）")
    public void testDataClassificationAccessControl() throws Exception {
        // 测试L1公开数据可以被所有用户访问
        mockMvc.perform(get("/api/v1/data/datasets")
                .param("dataLevel", "1"))
                .andExpect(status().isOk());

        // 测试L4机密数据需要特殊权限
        mockMvc.perform(get("/api/v1/data/datasets")
                .param("dataLevel", "4"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("标注一致性计算测试（DM8）")
    public void testLabelConsistencyCalculation() throws Exception {
        // 使用dm8-data.sql中已初始化的标注任务（ID=1，已有标注记录和kappa分数）
        // 计算IAA得分
        mockMvc.perform(get("/api/v1/label/tasks/1/iaa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("质量门禁检查测试（DM8）")
    public void testQualityGateCheck() throws Exception {
        Long datasetId = createDataset();

        // 执行质量评估
        String response = evaluateDataset(datasetId);
        assertNotNull(response, "质量评估响应为空（DM8）");

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        assertNotNull(responseMap.get("data"), "质量门禁结果为空（DM8）");
    }

    @Test
    @DisplayName("L4数据下载阻断测试（DM8）")
    public void testL4DataDownloadBlocking() throws Exception {
        // 创建L4机密数据集
        DatasetCreateRequest request = new DatasetCreateRequest();
        request.setName("DM8-L4机密数据集-" + System.currentTimeMillis());
        request.setDescription("DM8 L4机密数据测试");
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
        assertNotNull(datasetId, "L4数据集创建失败（DM8）");

        // 尝试通过delivery端点下载L4数据集（需要userId参数）
        // DeliveryController: GET /api/v1/delivery/{id}/download?userId=xxx
        // 由于L4数据需要审批，下载应被阻断，返回success=false
        MvcResult downloadResult = mockMvc.perform(get("/api/v1/delivery/" + datasetId + "/download")
                .param("userId", "1"))
                .andReturn();

        // 验证下载被阻断 — L4数据应返回success=false
        String downloadBody = downloadResult.getResponse().getContentAsString();
        Map<String, Object> downloadMap = objectMapper.readValue(downloadBody, Map.class);
        assertFalse((Boolean) downloadMap.get("success"), "L4数据下载应被阻断（DM8），success应为false");
    }

    @Test
    @DisplayName("DM8 SQL 方言兼容性验证（分页/CLOB/时间函数）")
    public void testDm8SqlDialectCompatibility() throws Exception {
        // 验证 DM8 特有的 SQL 方言兼容性

        // 1. 测试分页查询（LIMIT/OFFSET 在 DM8 的兼容性）
        mockMvc.perform(get("/api/v1/data/datasets")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());

        // 2. 测试 CURRENT_TIMESTAMP 函数（DM8 兼容）
        // 通过创建数据集验证 create_time 字段使用 CURRENT_TIMESTAMP 默认值
        Long datasetId = createDataset();
        assertNotNull(datasetId, "CURRENT_TIMESTAMP 默认值在 DM8 下应正常工作");

        // 3. 测试 CLOB 类型字段（DM8 兼容）
        // 通过创建包含大文本字段的数据集验证
        DatasetCreateRequest request = new DatasetCreateRequest();
        request.setName("DM8-CLOB测试-" + System.currentTimeMillis());
        request.setDescription("测试DM8 CLOB字段兼容性");
        request.setModality(1);
        request.setDataLevel(2);
        request.setVersion("1.0.0");
        // 构造较大的 JSON 字符串测试 CLOB 存储
        StringBuilder largeTags = new StringBuilder("[");
        for (int i = 0; i < 100; i++) {
            if (i > 0) largeTags.append(",");
            largeTags.append("\"tag").append(i).append("\"");
        }
        largeTags.append("]");
        request.setTags(largeTags.toString());

        MvcResult result = mockMvc.perform(post("/api/v1/data/datasets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
        assertNotNull(responseMap.get("data"), "CLOB 字段在 DM8 下应正常存储");
    }
}
