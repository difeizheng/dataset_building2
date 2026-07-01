package com.ctg.kbFab.ontology.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.kbFab.ontology.dto.CreateOntologyClassRequest;
import com.ctg.kbFab.ontology.dto.CreateOntologyRelationRequest;
import com.ctg.kbFab.ontology.entity.OntologyClass;
import com.ctg.kbFab.ontology.entity.OntologyRelation;

import java.util.List;

/**
 * 本体建模服务接口
 *
 * @author Developer
 * @since 2026-07-01
 */
public interface OntologyService {

    /**
     * 创建本体类
     */
    OntologyClass createClass(CreateOntologyClassRequest request);

    /**
     * 获取本体类
     */
    OntologyClass getClass(String classId);

    /**
     * 分页查询本体类
     */
    Page<OntologyClass> listClasses(Integer page, Integer size);

    /**
     * 删除本体类
     */
    void deleteClass(String classId);

    /**
     * 创建本体关系
     */
    OntologyRelation createRelation(CreateOntologyRelationRequest request);

    /**
     * 获取类的所有关系
     */
    List<OntologyRelation> getRelationsByClass(String classId);

    /**
     * 删除本体关系
     */
    void deleteRelation(String relationId);

    /**
     * 验证本体一致性
     */
    boolean validateOntology();
}
