package com.ctg.dataFab.delivery.controller;

import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.security.JwtTokenProvider;
import com.ctg.dataFab.delivery.dto.PublishRequest;
import com.ctg.dataFab.delivery.dto.PublishResponse;
import com.ctg.dataFab.delivery.entity.DeliveryRecord;
import com.ctg.dataFab.delivery.service.DeliveryService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DeliveryController 单元测试
 *
 * @author Security Officer
 * @since 2026-07-05
 */
@WebMvcTest(DeliveryController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeliveryService deliveryService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void publish_success() throws Exception {
        PublishRequest request = new PublishRequest();
        request.setDatasetId(1L);
        request.setVersion("1.0.0");

        PublishResponse response = new PublishResponse();
        response.setRecordId(100L);
        response.setDatasetUri("https://example.com/dataset/1");
        response.setLicense("MIT");
        response.setLineage("{\"parent\":\"dataset-0\"}");

        when(deliveryService.publish(any(PublishRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/delivery/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recordId").value(100));
    }

    @Test
    void getRecord_success() throws Exception {
        DeliveryRecord record = new DeliveryRecord();
        record.setId(1L);
        record.setDatasetId(1L);
        record.setVersion("1.0.0");
        record.setStatus(1);
        record.setCreateTime(LocalDateTime.now());

        when(deliveryService.getRecordById(1L)).thenReturn(record);

        mockMvc.perform(get("/api/v1/delivery/records/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.version").value("1.0.0"));
    }

    @Test
    void listRecords_success() throws Exception {
        DeliveryRecord record = new DeliveryRecord();
        record.setId(1L);
        record.setDatasetId(1L);
        record.setStatus(1);
        record.setCreateTime(LocalDateTime.now());

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<DeliveryRecord> page =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1L, 20L);
        page.setRecords(List.of(record));
        page.setTotal(1L);

        when(deliveryService.listRecords(any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/delivery/records")
                        .param("datasetId", "1")
                        .param("pageNum", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records[0].id").value(1));
    }

    @Test
    void generateDownloadToken_success() throws Exception {
        when(deliveryService.generateDownloadToken(1L, 100L, null)).thenReturn("download-token-abc");

        mockMvc.perform(get("/api/v1/delivery/1/download")
                        .param("userId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("download-token-abc"));
    }

    @Test
    void unpublish_success() throws Exception {
        doNothing().when(deliveryService).unpublish(1L);

        mockMvc.perform(post("/api/v1/delivery/records/1/unpublish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}