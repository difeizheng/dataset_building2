package com.ctg.dataFab.etl.controller;

import com.ctg.dataFab.common.dto.ApiResponse;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.dto.PageResponse;
import com.ctg.dataFab.etl.entity.EtlRule;
import com.ctg.dataFab.etl.service.EtlRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 清洗规则控制器
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/etl/rules")
@RequiredArgsConstructor
@Tag(name = "清洗规则管理", description = "清洗规则的增删改查接口")
public class EtlRuleController {

    private final EtlRuleService etlRuleService;

    @PostMapping
    @Operation(summary = "创建清洗规则")
    public ApiResponse<Long> createRule(@RequestBody EtlRule rule) {
        Long id = etlRuleService.createRule(rule);
        return ApiResponse.success(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取清洗规则详情")
    public ApiResponse<EtlRule> getRule(@PathVariable Long id) {
        EtlRule rule = etlRuleService.getRuleById(id);
        return ApiResponse.success(rule);
    }

    @GetMapping
    @Operation(summary = "分页查询清洗规则")
    public ApiResponse<PageResponse<EtlRule>> listRules(
            @RequestParam(required = false) Integer modality,
            @RequestParam(required = false) String ruleType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(pageNum);
        pageRequest.setPageSize(pageSize);

        var page = etlRuleService.listRules(modality, ruleType, pageRequest);
        PageResponse<EtlRule> response = PageResponse.of(
                page.getRecords(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize()
        );

        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新清洗规则")
    public ApiResponse<Void> updateRule(@PathVariable Long id, @RequestBody EtlRule rule) {
        rule.setId(id);
        etlRuleService.updateRule(rule);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除清洗规则")
    public ApiResponse<Void> deleteRule(@PathVariable Long id) {
        etlRuleService.deleteRule(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/toggle")
    @Operation(summary = "启用/禁用清洗规则")
    public ApiResponse<Void> toggleRuleEnabled(
            @PathVariable Long id,
            @RequestParam Integer enabled) {
        etlRuleService.toggleRuleEnabled(id, enabled);
        return ApiResponse.success();
    }
}
