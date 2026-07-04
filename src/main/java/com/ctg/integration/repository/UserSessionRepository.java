package com.ctg.integration.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ctg.integration.entity.UserSession;

/**
 * 会话数据访问层
 *
 * @author CTG
 * @since 2026-07-01
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findBySessionToken(String sessionToken);

    Optional<UserSession> findByRefreshToken(String refreshToken);

    List<UserSession> findByUserIdAndIsActiveTrue(Long userId);

    List<UserSession> findByUserId(Long userId);

    @Query("SELECT s FROM UserSession s WHERE s.userId = :userId AND s.isActive = true AND s.expireAt > :now")
    List<UserSession> findActiveSessionsByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.userId = :userId")
    void invalidateAllUserSessions(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.expireAt < :now")
    void cleanExpiredSessions(@Param("now") LocalDateTime now);

    @Modifying
    void deleteByExpireAtBefore(LocalDateTime before);
}
