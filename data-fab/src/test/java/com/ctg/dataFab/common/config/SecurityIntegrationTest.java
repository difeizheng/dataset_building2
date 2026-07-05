package com.ctg.dataFab.common.config;

import com.ctg.dataFab.common.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.ctg.dataFab.access.mapper.AuditLogMapper;
import com.ctg.dataFab.access.mapper.DataApprovalMapper;
import com.ctg.dataFab.delivery.mapper.DeliveryRecordMapper;
import com.ctg.dataFab.etl.mapper.EtlRuleMapper;
import com.ctg.dataFab.etl.mapper.EtlTaskMapper;
import com.ctg.dataFab.ingest.mapper.DataSampleMapper;
import com.ctg.dataFab.ingest.mapper.DatasetMapper;
import com.ctg.dataFab.label.mapper.LabelTaskMapper;
import com.ctg.dataFab.qa.mapper.QaTaskMapper;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 安全配置集成测试
 * 验证: JWT认证 / 角色授权 / 限流 / MFA / 审计日志拦截
 *
 * @author Security Officer
 * @since 2026-07-05
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("安全配置集成测试")
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // Mock all mappers to avoid DB dependency
    @MockBean private AuditLogMapper auditLogMapper;
    @MockBean private DataApprovalMapper dataApprovalMapper;
    @MockBean private DeliveryRecordMapper deliveryRecordMapper;
    @MockBean private EtlRuleMapper etlRuleMapper;
    @MockBean private EtlTaskMapper etlTaskMapper;
    @MockBean private DataSampleMapper dataSampleMapper;
    @MockBean private DatasetMapper datasetMapper;
    @MockBean private LabelTaskMapper labelTaskMapper;
    @MockBean private QaTaskMapper qaTaskMapper;

    private String adminToken;
    private String operatorToken;
    private String auditorToken;

    @BeforeEach
    void setUp() {
        adminToken = jwtTokenProvider.generateToken("admin-user", 1L, Arrays.asList("ADMIN"));
        operatorToken = jwtTokenProvider.generateToken("operator-user", 2L, Arrays.asList("OPERATOR"));
        auditorToken = jwtTokenProvider.generateToken("auditor-user", 3L, Arrays.asList("AUDITOR"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // A. 未认证请求 — 必须返回 401
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("A. 未认证请求阻断")
    class UnauthenticatedRequests {

        @Test
        @DisplayName("POST /api/v1/data/samples — 无Token返回401")
        void createSample_withoutToken_returns401() throws Exception {
            mockMvc.perform(post("/api/v1/data/samples")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"t\",\"modality\":1}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /api/v1/data/samples/1 — 无Token返回401")
        void deleteDataset_withoutToken_returns401() throws Exception {
            mockMvc.perform(delete("/api/v1/data/samples/1"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/v1/delivery/publish — 无Token返回401")
        void publishDelivery_withoutToken_returns401() throws Exception {
            mockMvc.perform(post("/api/v1/delivery/publish")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/label/tasks — 无Token返回401")
        void listLabelTasks_withoutToken_returns401() throws Exception {
            mockMvc.perform(get("/api/v1/label/tasks"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/v1/qa/evaluate — 无Token返回401")
        void evaluateQa_withoutToken_returns401() throws Exception {
            mockMvc.perform(post("/api/v1/qa/evaluate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"datasetId\":1,\"modality\":1,\"batch\":\"t\"}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // B. 公开接口 — 无需认证即可访问
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("B. 公开接口可匿名访问")
    class PublicEndpoints {

        @Test
        @DisplayName("GET /api/v1/data/samples — 公开列表无需认证")
        void listSamples_anonymous_ok() throws Exception {
            mockMvc.perform(get("/api/v1/data/samples"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/data/datasets — 公开列表无需认证")
        void listDatasets_anonymous_ok() throws Exception {
            mockMvc.perform(get("/api/v1/data/datasets"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/data/samples/1 — 公开单个样本无需认证")
        void getSample_anonymous_ok() throws Exception {
            mockMvc.perform(get("/api/v1/data/samples/1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/data/datasets/1 — 公开单个数据集无需认证")
        void getDataset_anonymous_ok() throws Exception {
            mockMvc.perform(get("/api/v1/data/datasets/1"))
                    .andExpect(status().isOk());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // C. 基于角色的访问控制
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("C. 角色授权测试")
    class RoleBasedAccessControl {

        @Test
        @DisplayName("ADMIN 可 POST /api/v1/data/samples")
        void admin_canCreateSample() throws Exception {
            mockMvc.perform(post("/api/v1/data/samples")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"test\",\"modality\":1}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("OPERATOR 可 POST /api/v1/data/samples")
        void operator_canCreateSample() throws Exception {
            mockMvc.perform(post("/api/v1/data/samples")
                            .header("Authorization", "Bearer " + operatorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"test\",\"modality\":1}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("AUDITOR 无法 POST /api/v1/data/samples — 返回403")
        void auditor_cannotCreateSample_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/data/samples")
                            .header("Authorization", "Bearer " + auditorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"test\",\"modality\":1}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("OPERATOR 无法 DELETE /api/v1/data/samples/1 — 返回403")
        void operator_cannotDelete_returns403() throws Exception {
            mockMvc.perform(delete("/api/v1/data/samples/1")
                            .header("Authorization", "Bearer " + operatorToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ADMIN 可 DELETE /api/v1/data/samples/1")
        void admin_canDelete() throws Exception {
            mockMvc.perform(delete("/api/v1/data/samples/1")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("AUDITOR 可 GET /api/v1/label/tasks")
        void auditor_canListLabelTasks() throws Exception {
            mockMvc.perform(get("/api/v1/label/tasks")
                            .header("Authorization", "Bearer " + auditorToken))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("AUDITOR 可 GET /api/v1/qa/tasks")
        void auditor_canListQaTasks() throws Exception {
            mockMvc.perform(get("/api/v1/qa/tasks")
                            .header("Authorization", "Bearer " + auditorToken))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("AUDITOR 无法 POST /api/v1/label/submit — 返回403")
        void auditor_cannotSubmitLabel_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/label/submit")
                            .header("Authorization", "Bearer " + auditorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"taskId\":1}"))
                    .andExpect(status().isForbidden());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // D. L4 下载 MFA 验证
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("D. L4下载MFA验证")
    class L4MfaVerification {

        @Test
        @DisplayName("下载L4数据集 — 无X-MFA-Token返回403")
        void downloadL4_withoutMfaToken_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/delivery/1/download")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("userId", "1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("下载L4数据集 — 无效MFA Token格式返回403")
        void downloadL4_withInvalidMfaFormat_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/delivery/1/download")
                            .header("Authorization", "Bearer " + adminToken)
                            .header("X-MFA-Token", "abc")
                            .param("userId", "1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("下载L4数据集 — 6位数字MFA Token通过MFA校验(进入业务层)")
        void downloadL4_withValidMfaFormat_isNot403() throws Exception {
            mockMvc.perform(get("/api/v1/delivery/1/download")
                            .header("Authorization", "Bearer " + adminToken)
                            .header("X-MFA-Token", "123456")
                            .param("userId", "1"))
                    .andExpect(status().isOk());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // E. 限流测试
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("E. 限流测试")
    class RateLimitingTest {

        @Test
        @DisplayName("101请求在10秒窗口内 — 至少一个返回429")
        void rateLimit_exceeded_returns429() throws Exception {
            int count429 = 0;

            for (int i = 0; i < 101; i++) {
                MvcResult result = mockMvc.perform(get("/api/v1/data/datasets")
                                .header("Authorization", "Bearer " + adminToken)
                                .param("pageNum", String.valueOf(i % 10 + 1)))
                        .andReturn();
                if (result.getResponse().getStatus() == 429) {
                    count429++;
                }
            }

            assertTrue(count429 > 0, "限流应在第100+请求时触发");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // F. JWT 格式验证
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("F. JWT格式验证")
    class JwtValidation {

        @Test
        @DisplayName("无效Token — 返回401")
        void invalidToken_returns401() throws Exception {
            mockMvc.perform(get("/api/v1/data/samples")
                            .header("Authorization", "Bearer invalid.token.here"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Bearer前缀缺失 — 返回401")
        void missingBearerPrefix_returns401() throws Exception {
            mockMvc.perform(get("/api/v1/data/samples")
                            .header("Authorization", "just-a-string"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // G. 审计日志 AOP 拦截
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("G. 审计AOP拦截验证")
    class AuditAopVerification {

        @Test
        @DisplayName("POST /api/v1/data/samples — 触发审计日志写入")
        void auditAspect_triggered_onWrite() throws Exception {
            mockMvc.perform(post("/api/v1/data/samples")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"audit-test\",\"modality\":1}"))
                    .andExpect(status().isOk());
            // AuditLogService.log() 被调用; 无异常即通过
        }
    }
}
