package com.ctg.kbFab.ontology.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.kbFab.common.dto.ApiResponse;
import com.ctg.kbFab.ontology.dto.CreateOntologyClassRequest;
import com.ctg.kbFab.ontology.dto.CreateOntologyRelationRequest;
import com.ctg.kbFab.ontology.entity.OntologyClass;
import com.ctg.kbFab.ontology.entity.OntologyRelation;
import com.ctg.kbFab.ontology.service.OntologyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 本体建模控制器
 *
 * @author Developer
 * @since 2026-07-01
 */
@RestController
@RequestMapping("/api/v1/kb/ontology")
@RequiredArgsConstructor
@Tag(name = "本体建模", description = "本体建模工具API")
public class OntologyController {

    private final OntologyService ontologyService;

    @PostMapping("/classes")
    @Operation(summary = "创建本体类")
    public ApiResponse<OntologyClass> createClass(@Valid @RequestBody CreateOntologyClassRequest request) {
        OntologyClass ontologyClass = ontologyService.createClass(request);
        return ApiResponse.success(ontologyClass);
    }

    @GetMapping("/classes/{classId}")
    @Operation(summary = "获取本体类")
    public ApiResponse<OntologyClass> getClass(@PathVariable String classId) {
        OntologyClass ontologyClass = ontologyService.getClass(classId);
        return ApiResponse.success(ontologyClass);
    }

    @GetMapping("/classes")
    @Operation(summary = "分页查询本体类")
    public ApiResponse<Page<OntologyClass>> listClasses(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<OntologyClass> classes = ontologyService.listClasses(page, size);
        return ApiResponse.success(classes);
    }

    @DeleteMapping("/classes/{classId}")
    @Operation(summary = "删除本体类")
    public ApiResponse<Void> deleteClass(@PathVariable String classId) {
        ontologyService.deleteClass(classId);
        return ApiResponse.success();
    }

    @PostMapping("/relations")
    @Operation(summary = "创建本体关系")
    public ApiResponse<OntologyRelation> createRelation(@Valid @RequestBody CreateOntologyRelationRequest request) {
        OntologyRelation relation = ontologyService.createRelation(request);
        return ApiResponse.success(relation);
    }

    @GetMapping("/classes/{classId}/relations")
    @Operation(summary = "获取类的关系")
    public ApiResponse<List<OntologyRelation>> getRelationsByClass(@PathVariable String classId) {
        List<OntologyRelation> relations = ontologyService.getRelationsByClass(classId);
        return ApiResponse.success(relations);
    }

    @DeleteMapping("/relations/{relationId}")
    @Operation(summary = "删除本体关系")
    public ApiResponse<Void> deleteRelation(@PathVariable String relationId) {
        ontologyService.deleteRelation(relationId);
        return ApiResponse.success();
    }

    @PostMapping("/validate")
    @Operation(summary = "验证本体一致性")
    public ApiResponse<Boolean> validateOntology() {
        boolean valid = ontologyService.validateOntology();
        return ApiResponse.success(valid);
    }
}
