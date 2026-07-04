package com.ctg.integration.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ctg.integration.entity.AuditLog;

/**
 * 审计日志数据访问层
 *
 * @author CTG
 * @since 2026-07-01
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<AuditLog> findByOperationOrderByCreatedAtDesc(AuditLog.OperationType operation, Pageable pageable);

    Page<AuditLog> findByResourceTypeOrderByCreatedAtDesc(String resourceType, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startTime AND :endTime ORDER BY a.createdAt DESC")
    Page<AuditLog> findByCreatedAtBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId AND a.createdAt BETWEEN :startTime AND :endTime ORDER BY a.createdAt DESC")
    List<AuditLog> findByUserIdAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.result = 'FAILURE' AND a.createdAt >= :since")
    long countFailuresSince(@Param("since") LocalDateTime since);

    @Query("SELECT a.operation, COUNT(a) FROM AuditLog a WHERE a.createdAt >= :since GROUP BY a.operation")
    List<Object[]> countByOperationSince(@Param("since") LocalDateTime since);

    long deleteByCreatedAtBefore(LocalDateTime before);
}
