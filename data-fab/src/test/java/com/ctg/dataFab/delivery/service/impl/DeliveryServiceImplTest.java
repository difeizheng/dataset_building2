package com.ctg.dataFab.delivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.access.service.DataAccessControlService;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.enums.DataStatus;
import com.ctg.dataFab.common.enums.DataLevel;
import com.ctg.dataFab.common.exception.BusinessException;
import com.ctg.dataFab.delivery.dto.PublishRequest;
import com.ctg.dataFab.delivery.dto.PublishResponse;
import com.ctg.dataFab.delivery.entity.DeliveryRecord;
import com.ctg.dataFab.delivery.mapper.DeliveryRecordMapper;
import com.ctg.dataFab.ingest.entity.DataSample;
import com.ctg.dataFab.ingest.entity.Dataset;
import com.ctg.dataFab.ingest.mapper.DataSampleMapper;
import com.ctg.dataFab.ingest.mapper.DatasetMapper;
import com.ctg.dataFab.ingest.service.DatasetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 交付服务单元测试
 *
 * @author Developer
 * @since 2026-07-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("交付服务测试")
class DeliveryServiceImplTest {

    @Mock
    private DeliveryRecordMapper deliveryRecordMapper;

    @Mock
    private DatasetMapper datasetMapper;

    @Mock
    private DataSampleMapper dataSampleMapper;

    @Mock
    private DatasetService datasetService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private DataAccessControlService dataAccessControlService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DeliveryServiceImpl deliveryService;

    private PublishRequest publishRequest;
    private Dataset dataset;
    private DeliveryRecord deliveryRecord;

    @BeforeEach
    void setUp() {
        publishRequest = new PublishRequest();
        publishRequest.setDatasetId(1L);
        publishRequest.setVersion("1.0.0");
        publishRequest.setLicense("MIT");
        publishRequest.setAccessPolicy("public");

        dataset = new Dataset();
        dataset.setId(1L);
        dataset.setName("测试数据集");
        dataset.setStatus(DataStatus.QA_PASS.getCode());

        deliveryRecord = new DeliveryRecord();
        deliveryRecord.setId(1L);
        deliveryRecord.setDatasetId(1L);
        deliveryRecord.setStatus(1);
    }

    @Test
    @DisplayName("发布数据集 - 成功")
    void testPublish_Success() throws Exception {
        when(datasetService.getDatasetById(1L)).thenReturn(dataset);
        when(deliveryRecordMapper.insert(any(DeliveryRecord.class))).thenAnswer(invocation -> {
            DeliveryRecord record = invocation.getArgument(0);
            record.setId(1L);
            return 1;
        });
        when(datasetMapper.updateById(any(Dataset.class))).thenReturn(1);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        PublishResponse response = deliveryService.publish(publishRequest);

        assertNotNull(response);
        assertEquals(1L, response.getRecordId());
        verify(deliveryRecordMapper).insert(any(DeliveryRecord.class));
        verify(datasetMapper).updateById(any(Dataset.class));
    }

    @Test
    @DisplayName("发布数据集 - 未通过质量评估")
    void testPublish_NotQaPass() {
        dataset.setStatus(DataStatus.NEW.getCode());
        when(datasetService.getDatasetById(1L)).thenReturn(dataset);

        assertThrows(BusinessException.class, () -> {
            deliveryService.publish(publishRequest);
        });
    }

    @Test
    @DisplayName("获取交付记录 - 存在")
    void testGetRecordById_Exists() {
        when(deliveryRecordMapper.selectById(1L)).thenReturn(deliveryRecord);

        DeliveryRecord result = deliveryService.getRecordById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("获取交付记录 - 不存在")
    void testGetRecordById_NotExists() {
        when(deliveryRecordMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            deliveryService.getRecordById(999L);
        });
    }

    @Test
    @DisplayName("分页查询交付记录")
    void testListRecords() {
        Page<DeliveryRecord> page = new Page<>(1, 20);
        page.setRecords(java.util.Collections.singletonList(deliveryRecord));

        when(deliveryRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(20);

        Page<DeliveryRecord> result = deliveryService.listRecords(null, null, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("C2: L4核心数据下载被阻断")
    void testGenerateDownloadToken_L4Blocked() {
        when(deliveryRecordMapper.selectById(1L)).thenReturn(deliveryRecord);

        DataSample l4Sample = new DataSample();
        l4Sample.setId(100L);
        l4Sample.setDatasetId(1L);
        l4Sample.setDataLevel(DataLevel.L4_CORE.getCode());

        when(dataSampleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(l4Sample));
        when(dataAccessControlService.canDownload(100L, 10L)).thenReturn(false);

        assertThrows(BusinessException.class, () -> {
            deliveryService.generateDownloadToken(1L, 10L, "approval-123");
        });
    }

    @Test
    @DisplayName("C2: L1/L2数据下载成功生成令牌")
    void testGenerateDownloadToken_L2Allowed() {
        when(deliveryRecordMapper.selectById(1L)).thenReturn(deliveryRecord);

        DataSample l2Sample = new DataSample();
        l2Sample.setId(100L);
        l2Sample.setDatasetId(1L);
        l2Sample.setDataLevel(DataLevel.L2_INTERNAL.getCode());

        when(dataSampleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(l2Sample));
        when(dataAccessControlService.canDownload(100L, 10L)).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String token = deliveryService.generateDownloadToken(1L, 10L, "approval-123");

        assertNotNull(token);
        verify(redisTemplate.opsForValue()).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("生成下载令牌 - 数据集未发布")
    void testGenerateDownloadToken_NotPublished() {
        deliveryRecord.setStatus(2); // 已下架
        when(deliveryRecordMapper.selectById(1L)).thenReturn(deliveryRecord);

        assertThrows(BusinessException.class, () -> {
            deliveryService.generateDownloadToken(1L, 10L, "approval-123");
        });
    }

    @Test
    @DisplayName("下架数据集")
    void testUnpublish() {
        when(deliveryRecordMapper.selectById(1L)).thenReturn(deliveryRecord);
        when(deliveryRecordMapper.updateById(any(DeliveryRecord.class))).thenReturn(1);
        when(datasetService.getDatasetById(1L)).thenReturn(dataset);
        when(datasetMapper.updateById(any(Dataset.class))).thenReturn(1);

        deliveryService.unpublish(1L);

        verify(deliveryRecordMapper).updateById(any(DeliveryRecord.class));
        verify(datasetMapper).updateById(any(Dataset.class));
    }
}
