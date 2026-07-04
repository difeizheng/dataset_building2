package com.ctg.integration.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ctg.integration.entity.Permission;

/**
 * 权限数据访问层
 *
 * @author CTG
 * @since 2026-07-01
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByCode(String code);

    boolean existsByCode(String code);

    List<Permission> findByType(Permission.PermissionType type);

    List<Permission> findByResourceContaining(String resource);
}
