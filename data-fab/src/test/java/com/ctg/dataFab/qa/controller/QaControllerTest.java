package com.ctg.dataFab.qa.controller;

import com.ctg.dataFab.common.security.JwtTokenProvider;
import com.ctg.dataFab.qa.dto.EvaluateRequest;
import com.ctg.dataFab.qa.dto.EvaluateResponse;
import com.ctg.dataFab.qa.entity.QaTask;
import com.ctg.dataFab.qa.service.QaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * QaController 单元测试
 *
 * @author Security Officer
 * @since 2026-07-05
 */
@WebMvcTest(QaController.class)
@AutoConfigureMockMvc(addFilters = false)
class QaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QaService qaService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void evaluate_success() throws Exception {
        EvaluateRequest request = new EvaluateRequest();
        request.setDatasetId(1L);
        request.setModality(1);
        request.setMetrics(Map.of("quality", 85.5));

        EvaluateResponse response = new EvaluateResponse();
        response.setTaskId(100L);
        response.setPassed(true);
        response.setMetrics(Map.of("quality", 85.5));

        when(qaService.evaluate(any(EvaluateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/qa/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.passed").value(true));
    }

    @Test
    void getTask_success() throws Exception {
        QaTask task = new QaTask();
        task.setId(1L);
        task.setDatasetId(1L);
        task.setStatus(2);
        task.setPassed(1);
        task.setCreateTime(LocalDateTime.now());

        when(qaService.getTaskById(1L)).thenReturn(task);

        mockMvc.perform(get("/api/v1/qa/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void listTasks_success() throws Exception {
        QaTask task = new QaTask();
        task.setId(1L);
        task.setDatasetId(1L);
        task.setStatus(2);
        task.setCreateTime(LocalDateTime.now());

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<QaTask> page =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1L, 20L);
        page.setRecords(List.of(task));
        page.setTotal(1L);

        when(qaService.listTasks(any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/qa/tasks")
                        .param("datasetId", "1")
                        .param("pageNum", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records[0].id").value(1));
    }

    @Test
    void manualReview_success() throws Exception {
        doNothing().when(qaService).manualReview(eq(1L), eq(true), eq("OK"), eq("reviewer1"));

        mockMvc.perform(post("/api/v1/qa/tasks/1/manual-review")
                        .param("passed", "true")
                        .param("comment", "OK")
                        .param("reviewer", "reviewer1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void expertReview_success() throws Exception {
        doNothing().when(qaService).expertReview(eq(1L), eq(true), eq("Approved"), eq("expert1"));

        mockMvc.perform(post("/api/v1/qa/tasks/1/expert-review")
                        .param("passed", "true")
                        .param("comment", "Approved")
                        .param("reviewer", "expert1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void publishDataset_success() throws Exception {
        doNothing().when(qaService).publishDataset(1L);

        mockMvc.perform(post("/api/v1/qa/tasks/1/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}