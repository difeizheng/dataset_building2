package com.ctg.integration.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ctg.integration.entity.DataClassification;

/**
 * 数据分级数据访问层
 *
 * @author CTG
 * @since 2026-07-01
 */
@Repository
public interface DataClassificationRepository extends JpaRepository<DataClassification, Long> {

    Optional<DataClassification> findBySecurityLevel(String securityLevel);

    Optional<DataClassification> findByClassificationName(String classificationName);

    List<DataClassification> findByRequireEncryptionTrue();

    List<DataClassification> findByRequireMaskingTrue();

    boolean existsBySecurityLevel(String securityLevel);
}
