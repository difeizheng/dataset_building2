package com.ctg.kbFab.ontology;

import com.ctg.kbFab.ontology.entity.OntologyClass;
import com.ctg.kbFab.ontology.entity.OntologyRelation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 本体建模实体测试
 */
class OntologyEntityTest {

    @Test
    void testOntologyClass() {
        OntologyClass cls = new OntologyClass();
        cls.setId("cls-001");
        cls.setClassName("Equipment");
        cls.setDescription("设备类");
        cls.setParentClassId(null);

        assertEquals("cls-001", cls.getId());
        assertEquals("Equipment", cls.getClassName());
        assertEquals("设备类", cls.getDescription());
        assertNull(cls.getParentClassId());
    }

    @Test
    void testOntologyRelation() {
        OntologyRelation rel = new OntologyRelation();
        rel.setId("rel-001");
        rel.setRelationName("belongsTo");
        rel.setSourceClassId("cls-001");
        rel.setTargetClassId("cls-002");
        rel.setCardinality("N:1");

        assertEquals("rel-001", rel.getId());
        assertEquals("belongsTo", rel.getRelationName());
        assertEquals("cls-001", rel.getSourceClassId());
        assertEquals("cls-002", rel.getTargetClassId());
        assertEquals("N:1", rel.getCardinality());
    }
}
