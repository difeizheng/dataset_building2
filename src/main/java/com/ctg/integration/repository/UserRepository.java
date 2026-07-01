package com.ctg.integration.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ctg.integration.entity.User;

/**
 * 用户数据访问层
 *
 * @author CTG
 * @since 2026-07-01
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmployeeId(String employeeId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByEnabled(Boolean enabled);

    List<User> findByDepartment(String department);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.loginFailCount >= :maxFailCount AND u.enabled = true")
    List<User> findLockedUsers(@Param("maxFailCount") Integer maxFailCount);
}
