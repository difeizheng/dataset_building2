package com.ctg.dataFab.ingest.controller;

import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.security.JwtTokenProvider;
import com.ctg.dataFab.ingest.dto.DatasetCreateRequest;
import com.ctg.dataFab.ingest.entity.Dataset;
import com.ctg.dataFab.ingest.service.DataSampleService;
import com.ctg.dataFab.ingest.service.DatasetService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DatasetController 单元测试
 *
 * @author Security Officer
 * @since 2026-07-05
 */
@WebMvcTest(DatasetController.class)
@AutoConfigureMockMvc(addFilters = false)
class DatasetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DatasetService datasetService;

    @MockBean
    private DataSampleService dataSampleService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void createDataset_success() throws Exception {
        DatasetCreateRequest request = new DatasetCreateRequest();
        request.setName("测试数据集");
        request.setDescription("测试描述");
        request.setModality(1);
        request.setDataLevel(2);
        request.setVersion("1.0.0");

        when(datasetService.createDataset(any(DatasetCreateRequest.class))).thenReturn(1L);

        mockMvc.perform(post("/api/v1/data/datasets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void getDataset_success() throws Exception {
        Dataset dataset = new Dataset();
        dataset.setId(1L);
        dataset.setName("测试数据集");
        dataset.setModality(1);
        dataset.setDataLevel(2);
        dataset.setStatus(1);
        dataset.setCreateTime(LocalDateTime.now());

        when(datasetService.getDatasetById(1L)).thenReturn(dataset);

        mockMvc.perform(get("/api/v1/data/datasets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试数据集"));
    }

    @Test
    void listDatasets_success() throws Exception {
        Dataset dataset = new Dataset();
        dataset.setId(1L);
        dataset.setName("测试数据集");
        dataset.setModality(1);
        dataset.setCreateTime(LocalDateTime.now());

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Dataset> page =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1L, 20L);
        page.setRecords(List.of(dataset));
        page.setTotal(1L);

        when(datasetService.listDatasets(any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/data/datasets")
                        .param("modality", "1")
                        .param("dataLevel", "2")
                        .param("status", "1")
                        .param("pageNum", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records[0].id").value(1));
    }

    @Test
    void deleteDataset_success() throws Exception {

        mockMvc.perform(delete("/api/v1/data/datasets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}