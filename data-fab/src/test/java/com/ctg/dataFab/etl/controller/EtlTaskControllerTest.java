package com.ctg.dataFab.etl.controller;

import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.security.JwtTokenProvider;
import com.ctg.dataFab.etl.entity.EtlTask;
import com.ctg.dataFab.etl.service.EtlTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * EtlTaskController 单元测试
 *
 * @author Security Officer
 * @since 2026-07-05
 */
@WebMvcTest(EtlTaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class EtlTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EtlTaskService etlTaskService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void createTask_success() throws Exception {
        when(etlTaskService.createTask(eq(1L), eq("test-task"))).thenReturn(1L);

        mockMvc.perform(post("/api/v1/etl/tasks")
                        .param("datasetId", "1")
                        .param("taskName", "test-task"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void listTasks_success() throws Exception {
        EtlTask task = new EtlTask();
        task.setId(1L);
        task.setDatasetId(1L);
        task.setStatus(1);
        task.setCreateTime(LocalDateTime.now());

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<EtlTask> page =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1L, 20L);
        page.setRecords(List.of(task));
        page.setTotal(1L);

        when(etlTaskService.listTasks(any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/etl/tasks")
                        .param("datasetId", "1")
                        .param("pageNum", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records[0].id").value(1));
    }

    @Test
    void executeTask_success() throws Exception {
        doNothing().when(etlTaskService).executeTask(1L);

        mockMvc.perform(post("/api/v1/etl/tasks/1/execute"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getTask_success() throws Exception {
        EtlTask task = new EtlTask();
        task.setId(1L);
        task.setDatasetId(1L);
        task.setStatus(2);
        task.setCreateTime(LocalDateTime.now());

        when(etlTaskService.getTaskById(1L)).thenReturn(task);

        mockMvc.perform(get("/api/v1/etl/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void cancelTask_success() throws Exception {
        doNothing().when(etlTaskService).cancelTask(1L);

        mockMvc.perform(post("/api/v1/etl/tasks/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}