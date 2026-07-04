package com.ctg.kbFab.ontology.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.kbFab.common.exception.BusinessException;
import com.ctg.kbFab.ontology.dto.CreateOntologyClassRequest;
import com.ctg.kbFab.ontology.dto.CreateOntologyRelationRequest;
import com.ctg.kbFab.ontology.entity.OntologyClass;
import com.ctg.kbFab.ontology.entity.OntologyRelation;
import com.ctg.kbFab.ontology.mapper.OntologyClassMapper;
import com.ctg.kbFab.ontology.mapper.OntologyRelationMapper;
import com.ctg.kbFab.ontology.service.OntologyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本体建模服务实现
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OntologyServiceImpl implements OntologyService {

    private final OntologyClassMapper ontologyClassMapper;
    private final OntologyRelationMapper ontologyRelationMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public OntologyClass createClass(CreateOntologyClassRequest request) {
        OntologyClass ontologyClass = new OntologyClass();
        ontologyClass.setClassName(request.getClassName());
        ontologyClass.setParentClassId(request.getParentClassId());
        ontologyClass.setDescription(request.getDescription());

        try {
            if (request.getProperties() != null) {
                ontologyClass.setPropertiesJson(objectMapper.writeValueAsString(request.getProperties()));
            }
            if (request.getConstraints() != null) {
                ontologyClass.setConstraintsJson(objectMapper.writeValueAsString(request.getConstraints()));
            }
        } catch (Exception e) {
            log.error("Failed to serialize properties/constraints", e);
            throw new BusinessException("Invalid properties or constraints format");
        }

        ontologyClass.setCreateTime(LocalDateTime.now());
        ontologyClass.setUpdateTime(LocalDateTime.now());
        ontologyClass.setDeleted(0);

        ontologyClassMapper.insert(ontologyClass);
        log.info("Created ontology class: {}", ontologyClass.getId());
        return ontologyClass;
    }

    @Override
    public OntologyClass getClass(String classId) {
        return ontologyClassMapper.selectById(classId);
    }

    @Override
    public Page<OntologyClass> listClasses(Integer page, Integer size) {
        Page<OntologyClass> pageParam = new Page<>(page, size);
        return ontologyClassMapper.selectPage(pageParam, null);
    }

    @Override
    @Transactional
    public void deleteClass(String classId) {
        OntologyClass ontologyClass = getClass(classId);
        if (ontologyClass == null) {
            throw new BusinessException("Ontology class not found: " + classId);
        }
        ontologyClassMapper.deleteById(classId);
        log.info("Deleted ontology class: {}", classId);
    }

    @Override
    @Transactional
    public OntologyRelation createRelation(CreateOntologyRelationRequest request) {
        // 验证源类和目标类存在
        OntologyClass sourceClass = getClass(request.getSourceClassId());
        OntologyClass targetClass = getClass(request.getTargetClassId());
        if (sourceClass == null || targetClass == null) {
            throw new BusinessException("Source or target class not found");
        }

        OntologyRelation relation = new OntologyRelation();
        relation.setRelationName(request.getRelationName());
        relation.setSourceClassId(request.getSourceClassId());
        relation.setTargetClassId(request.getTargetClassId());
        relation.setDescription(request.getDescription());
        relation.setCardinality(request.getCardinality());
        relation.setCreateTime(LocalDateTime.now());
        relation.setUpdateTime(LocalDateTime.now());
        relation.setDeleted(0);

        ontologyRelationMapper.insert(relation);
        log.info("Created ontology relation: {}", relation.getId());
        return relation;
    }

    @Override
    public List<OntologyRelation> getRelationsByClass(String classId) {
        return ontologyRelationMapper.selectList(null);
    }

    @Override
    @Transactional
    public void deleteRelation(String relationId) {
        OntologyRelation relation = ontologyRelationMapper.selectById(relationId);
        if (relation == null) {
            throw new BusinessException("Ontology relation not found: " + relationId);
        }
        ontologyRelationMapper.deleteById(relationId);
        log.info("Deleted ontology relation: {}", relationId);
    }

    @Override
    public boolean validateOntology() {
        // 检查所有关系的源类和目标类是否存在
        List<OntologyRelation> relations = ontologyRelationMapper.selectList(null);
        for (OntologyRelation relation : relations) {
            OntologyClass source = getClass(relation.getSourceClassId());
            OntologyClass target = getClass(relation.getTargetClassId());
            if (source == null || target == null) {
                log.warn("Invalid relation {}: source or target class missing", relation.getId());
                return false;
            }
        }
        log.info("Ontology validation passed");
        return true;
    }
}
