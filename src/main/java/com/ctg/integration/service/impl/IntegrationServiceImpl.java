package com.ctg.integration.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ctg.integration.dto.integration.*;
import com.ctg.integration.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 系统集成服务实现
 * 统一管理各外部系统的集成调用
 *
 * @author CTG
 * @since 2026-07-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationServiceImpl implements IntegrationService {

    private final RestTemplate restTemplate;

    @Value("${integration.ai-platform.enabled:false}")
    private boolean aiPlatformEnabled;

    @Value("${integration.ai-platform.base-url:}")
    private String aiPlatformUrl;

    @Value("${integration.big-data.enabled:false}")
    private boolean bigDataEnabled;

    @Value("${integration.big-data.base-url:}")
    private String bigDataUrl;

    @Value("${integration.xingyun.enabled:false}")
    private boolean xingyunEnabled;

    @Value("${integration.xingyun.base-url:}")
    private String xingyunUrl;

    @Value("${integration.wps.enabled:false}")
    private boolean wpsEnabled;

    @Value("${integration.wps.base-url:}")
    private String wpsUrl;

    @Override
    public AIPlatformResponse callAIPlatform(AIPlatformRequest request) {
        log.info("调用AI中台: modelId={}, taskType={}", request.getModelId(), request.getTaskType());

        if (!aiPlatformEnabled) {
            log.warn("AI中台未启用");
            return AIPlatformResponse.builder()
                    .success(false)
                    .errorMessage("AI中台未启用")
                    .build();
        }

        long startTime = System.currentTimeMillis();
        try {
            // 实际调用AI中台API
            String url = aiPlatformUrl + "/api/ai/invoke";
            ResponseEntity<AIPlatformResponse> response = restTemplate.postForEntity(url, request, AIPlatformResponse.class);

            long duration = System.currentTimeMillis() - startTime;
            AIPlatformResponse result = response.getBody();
            if (result != null) {
                result.setDuration(duration);
            }
            return result != null ? result : AIPlatformResponse.builder()
                    .success(false)
                    .errorMessage("AI中台返回空响应")
                    .duration(duration)
                    .build();
        } catch (Exception e) {
            log.error("调用AI中台失败", e);
            return AIPlatformResponse.builder()
                    .success(false)
                    .errorMessage("调用AI中台失败: " + e.getMessage())
                    .duration(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    @Override
    public LLMResponse callLLMPlatform(LLMRequest request) {
        log.info("调用大模型平台: model={}", request.getModel());

        long startTime = System.currentTimeMillis();
        try {
            // 实际调用大模型平台API
            String url = "http://llm-platform:8080/api/llm/chat";
            ResponseEntity<LLMResponse> response = restTemplate.postForEntity(url, request, LLMResponse.class);

            long duration = System.currentTimeMillis() - startTime;
            LLMResponse result = response.getBody();
            if (result != null) {
                result.setDuration(duration);
            }
            return result != null ? result : LLMResponse.builder()
                    .success(false)
                    .errorMessage("大模型平台返回空响应")
                    .duration(duration)
                    .build();
        } catch (Exception e) {
            log.error("调用大模型平台失败", e);
            return LLMResponse.builder()
                    .success(false)
                    .errorMessage("调用大模型平台失败: " + e.getMessage())
                    .duration(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    @Override
    public BigDataResponse accessBigDataPlatform(BigDataRequest request) {
        log.info("调用大数据平台: dataSourceId={}, operationType={}", request.getDataSourceId(), request.getOperationType());

        if (!bigDataEnabled) {
            log.warn("大数据平台未启用");
            return BigDataResponse.builder()
                    .success(false)
                    .errorMessage("大数据平台未启用")
                    .build();
        }

        long startTime = System.currentTimeMillis();
        try {
            // 实际调用大数据平台API
            String url = bigDataUrl + "/api/data/access";
            ResponseEntity<BigDataResponse> response = restTemplate.postForEntity(url, request, BigDataResponse.class);

            long duration = System.currentTimeMillis() - startTime;
            BigDataResponse result = response.getBody();
            if (result != null) {
                result.setDuration(duration);
            }
            return result != null ? result : BigDataResponse.builder()
                    .success(false)
                    .errorMessage("大数据平台返回空响应")
                    .duration(duration)
                    .build();
        } catch (Exception e) {
            log.error("调用大数据平台失败", e);
            return BigDataResponse.builder()
                    .success(false)
                    .errorMessage("调用大数据平台失败: " + e.getMessage())
                    .duration(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    @Override
    public XingyunResponse callXingyunApproval(XingyunRequest request) {
        log.info("调用三峡行云: processId={}, operationType={}", request.getProcessId(), request.getOperationType());

        if (!xingyunEnabled) {
            log.warn("三峡行云未启用");
            return XingyunResponse.builder()
                    .success(false)
                    .errorMessage("三峡行云未启用")
                    .build();
        }

        long startTime = System.currentTimeMillis();
        try {
            // 实际调用三峡行云API
            String url = xingyunUrl + "/api/approval/submit";
            ResponseEntity<XingyunResponse> response = restTemplate.postForEntity(url, request, XingyunResponse.class);

            long duration = System.currentTimeMillis() - startTime;
            XingyunResponse result = response.getBody();
            if (result != null) {
                result.setDuration(duration);
            }
            return result != null ? result : XingyunResponse.builder()
                    .success(false)
                    .errorMessage("三峡行云返回空响应")
                    .duration(duration)
                    .build();
        } catch (Exception e) {
            log.error("调用三峡行云失败", e);
            return XingyunResponse.builder()
                    .success(false)
                    .errorMessage("调用三峡行云失败: " + e.getMessage())
                    .duration(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    @Override
    public WPSResponse callWPSService(WPSRequest request) {
        log.info("调用WPS服务: operationType={}", request.getOperationType());

        if (!wpsEnabled) {
            log.warn("WPS服务未启用");
            return WPSResponse.builder()
                    .success(false)
                    .errorMessage("WPS服务未启用")
                    .build();
        }

        long startTime = System.currentTimeMillis();
        try {
            // 实际调用WPS服务API
            String url = wpsUrl + "/api/document/process";
            ResponseEntity<WPSResponse> response = restTemplate.postForEntity(url, request, WPSResponse.class);

            long duration = System.currentTimeMillis() - startTime;
            WPSResponse result = response.getBody();
            if (result != null) {
                result.setDuration(duration);
            }
            return result != null ? result : WPSResponse.builder()
                    .success(false)
                    .errorMessage("WPS服务返回空响应")
                    .duration(duration)
                    .build();
        } catch (Exception e) {
            log.error("调用WPS服务失败", e);
            return WPSResponse.builder()
                    .success(false)
                    .errorMessage("调用WPS服务失败: " + e.getMessage())
                    .duration(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    @Override
    public SignatureResponse callSignatureService(SignatureRequest request) {
        log.info("调用签章系统: documentId={}, signerId={}", request.getDocumentId(), request.getSignerId());

        long startTime = System.currentTimeMillis();
        try {
            // 实际调用签章系统API
            String url = "http://signature-system:8080/api/sign/signature";
            ResponseEntity<SignatureResponse> response = restTemplate.postForEntity(url, request, SignatureResponse.class);

            long duration = System.currentTimeMillis() - startTime;
            SignatureResponse result = response.getBody();
            if (result != null) {
                result.setDuration(duration);
            }
            return result != null ? result : SignatureResponse.builder()
                    .success(false)
                    .errorMessage("签章系统返回空响应")
                    .duration(duration)
                    .build();
        } catch (Exception e) {
            log.error("调用签章系统失败", e);
            return SignatureResponse.builder()
                    .success(false)
                    .errorMessage("调用签章系统失败: " + e.getMessage())
                    .duration(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    @Override
    public List<SystemStatusVO> getAllSystemStatus() {
        List<SystemStatusVO> statusList = new ArrayList<>();

        statusList.add(checkSystemStatus("AI中台", "AI_PLATFORM", aiPlatformEnabled, aiPlatformUrl));
        statusList.add(checkSystemStatus("大模型平台", "LLM_PLATFORM", true, "http://llm-platform:8080"));
        statusList.add(checkSystemStatus("大数据平台", "BIG_DATA", bigDataEnabled, bigDataUrl));
        statusList.add(checkSystemStatus("三峡行云", "XINGYUN", xingyunEnabled, xingyunUrl));
        statusList.add(checkSystemStatus("WPS服务", "WPS", wpsEnabled, wpsUrl));
        statusList.add(checkSystemStatus("签章系统", "SIGNATURE", true, "http://signature-system:8080"));

        return statusList;
    }

    private SystemStatusVO checkSystemStatus(String name, String code, boolean enabled, String url) {
        if (!enabled) {
            return SystemStatusVO.builder()
                    .systemName(name)
                    .systemCode(code)
                    .available(false)
                    .status("DISABLED")
                    .lastCheckTime(LocalDateTime.now())
                    .build();
        }

        try {
            // 实际调用健康检查接口
            long startTime = System.currentTimeMillis();
            String healthUrl = url + "/actuator/health";
            restTemplate.getForObject(healthUrl, String.class);
            long responseTime = System.currentTimeMillis() - startTime;

            return SystemStatusVO.builder()
                    .systemName(name)
                    .systemCode(code)
                    .available(true)
                    .status("HEALTHY")
                    .responseTime(responseTime)
                    .lastCheckTime(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.warn("系统健康检查失败: name={}, url={}", name, url);
            return SystemStatusVO.builder()
                    .systemName(name)
                    .systemCode(code)
                    .available(false)
                    .status("UNHEALTHY")
                    .responseTime(0L)
                    .lastCheckTime(LocalDateTime.now())
                    .build();
        }
    }
}
