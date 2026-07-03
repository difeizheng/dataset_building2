package com.ctg.integration.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ctg.integration.dto.classification.*;
import com.ctg.integration.service.DataClassificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据分级控制器
 * 提供数据分级管理和查询接口
 * 三权分立：写操作仅ADMIN，读操作ADMIN/AUDITOR/OPERATOR
 *
 * @author CTG
 * @since 2026-07-01
 */
@Slf4j
@RestController
@RequestMapping("/data-classification")
@RequiredArgsConstructor
@Tag(name = "数据分级", description = "数据分级管理")
public class DataClassificationController {

    private final DataClassificationService classificationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建分级", description = "创建数据分级（仅管理员）")
    public ResponseEntity<DataClassificationVO> createClassification(
            @Valid @RequestBody CreateClassificationRequest request) {
        log.info("创建数据分级: level={}", request.getSecurityLevel());
        DataClassificationVO classification = classificationService.createClassification(request);
        return ResponseEntity.ok(classification);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新分级", description = "更新数据分级（仅管理员）")
    public ResponseEntity<DataClassificationVO> updateClassification(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClassificationRequest request) {
        log.info("更新数据分级: id={}", id);
        DataClassificationVO classification = classificationService.updateClassification(id, request);
        return ResponseEntity.ok(classification);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除分级", description = "删除数据分级（仅管理员）")
    public ResponseEntity<Void> deleteClassification(@PathVariable Long id) {
        log.info("删除数据分级: id={}", id);
        classificationService.deleteClassification(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'OPERATOR')")
    @Operation(summary = "获取分级", description = "获取数据分级详情")
    public ResponseEntity<DataClassificationVO> getClassification(@PathVariable Long id) {
        log.debug("获取数据分级: id={}", id);
        DataClassificationVO classification = classificationService.getClassificationById(id);
        return ResponseEntity.ok(classification);
    }

    @GetMapping("/level/{level}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'OPERATOR')")
    @Operation(summary = "按级别获取", description = "根据安全级别获取数据分级")
    public ResponseEntity<DataClassificationVO> getClassificationByLevel(@PathVariable String level) {
        log.debug("根据级别获取数据分级: level={}", level);
        DataClassificationVO classification = classificationService.getClassificationByLevel(level);
        return ResponseEntity.ok(classification);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'OPERATOR')")
    @Operation(summary = "分级列表", description = "获取所有数据分级")
    public ResponseEntity<List<DataClassificationVO>> listClassifications() {
        log.debug("获取所有数据分级");
        List<DataClassificationVO> classifications = classificationService.listClassifications();
        return ResponseEntity.ok(classifications);
    }

    @GetMapping("/{level}/requires-encryption")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'OPERATOR')")
    @Operation(summary = "检查加密", description = "检查指定级别是否需要加密")
    public ResponseEntity<Boolean> requiresEncryption(@PathVariable String level) {
        boolean requires = classificationService.requiresEncryption(level);
        return ResponseEntity.ok(requires);
    }

    @GetMapping("/{level}/requires-masking")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'OPERATOR')")
    @Operation(summary = "检查脱敏", description = "检查指定级别是否需要脱敏")
    public ResponseEntity<Boolean> requiresMasking(@PathVariable String level) {
        boolean requires = classificationService.requiresMasking(level);
        return ResponseEntity.ok(requires);
    }
}
