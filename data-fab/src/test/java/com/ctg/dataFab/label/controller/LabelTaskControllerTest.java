package com.ctg.dataFab.label.controller;

import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.security.JwtTokenProvider;
import com.ctg.dataFab.label.dto.CreateLabelTaskRequest;
import com.ctg.dataFab.label.dto.SubmitLabelRequest;
import com.ctg.dataFab.label.entity.LabelTask;
import com.ctg.dataFab.label.service.LabelTaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LabelTaskController 单元测试
 *
 * @author Security Officer
 * @since 2026-07-05
 */
@WebMvcTest(LabelTaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class LabelTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LabelTaskService labelTaskService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void createTask_success() throws Exception {
        CreateLabelTaskRequest request = new CreateLabelTaskRequest();
        request.setDatasetId(1L);
        request.setTaskName("测试标注任务");
        request.setModality(1);
        request.setLabelType("分类");
        request.setLabelSchema("{\"labels\":[\"A\",\"B\"]}");

        when(labelTaskService.createTask(any(CreateLabelTaskRequest.class))).thenReturn(1L);

        mockMvc.perform(post("/api/v1/label/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void getTask_success() throws Exception {
        LabelTask task = new LabelTask();
        task.setId(1L);
        task.setDatasetId(1L);
        task.setTaskName("测试任务");
        task.setStatus(2);
        task.setCreateTime(LocalDateTime.now());

        when(labelTaskService.getTaskById(1L)).thenReturn(task);

        mockMvc.perform(get("/api/v1/label/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.taskName").value("测试任务"));
    }

    @Test
    void submitLabel_success() throws Exception {
        SubmitLabelRequest request = new SubmitLabelRequest();
        request.setTaskId(1L);
        request.setSampleId(100L);
        request.setAnnotatorId(200L);
        request.setAnnotations("{\"label\":\"A\"}");

        doNothing().when(labelTaskService).submitLabel(any(SubmitLabelRequest.class));

        mockMvc.perform(post("/api/v1/label/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void triggerArbitration_success() throws Exception {
        doNothing().when(labelTaskService).triggerArbitration(1L);

        mockMvc.perform(post("/api/v1/label/arbitrate")
                        .param("taskId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void calculateIaa_success() throws Exception {
        when(labelTaskService.calculateIaaScore(1L)).thenReturn(0.85);

        mockMvc.perform(get("/api/v1/label/tasks/1/iaa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(0.85));
    }

    @Test
    void triggerRelabel_success() throws Exception {
        doNothing().when(labelTaskService).triggerRelabel(1L);

        mockMvc.perform(post("/api/v1/label/tasks/1/relabel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}