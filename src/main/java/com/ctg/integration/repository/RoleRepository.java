package com.ctg.integration.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ctg.integration.entity.Role;
import com.ctg.integration.entity.User;

/**
 * 角色数据访问层
 *
 * @author CTG
 * @since 2026-07-01
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);

    boolean existsByCode(String code);

    List<Role> findByRoleType(User.RoleType roleType);

    List<Role> findByEnabled(Boolean enabled);

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.code = :code")
    Optional<Role> findByCodeWithPermissions(@Param("code") String code);
}
