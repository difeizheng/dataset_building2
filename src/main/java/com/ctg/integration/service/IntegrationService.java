package com.ctg.integration.service;

import java.util.List;

import com.ctg.integration.dto.integration.*;

/**
 * 系统集成服务接口
 * 统一管理各外部系统的集成调用
 *
 * @author CTG
 * @since 2026-07-01
 */
public interface IntegrationService {

    /**
     * AI中台调用
     */
    AIPlatformResponse callAIPlatform(AIPlatformRequest request);

    /**
     * 大模型平台调用
     */
    LLMResponse callLLMPlatform(LLMRequest request);

    /**
     * 大数据平台数据接入
     */
    BigDataResponse accessBigDataPlatform(BigDataRequest request);

    /**
     * 三峡行云审批流程调用
     */
    XingyunResponse callXingyunApproval(XingyunRequest request);

    /**
     * WPS服务中台文档处理
     */
    WPSResponse callWPSService(WPSRequest request);

    /**
     * 数字化签章系统调用
     */
    SignatureResponse callSignatureService(SignatureRequest request);

    /**
     * 获取所有集成系统状态
     */
    List<SystemStatusVO> getAllSystemStatus();
}
