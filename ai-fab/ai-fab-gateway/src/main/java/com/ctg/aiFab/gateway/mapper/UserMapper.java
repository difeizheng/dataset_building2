package com.ctg.aiFab.gateway.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ctg.aiFab.gateway.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper
 *
 * @author Developer
 * @since 2026-07-01
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT u.* FROM sys_user u WHERE u.username = #{username} AND u.deleted = 0")
    User findByUsername(@Param("username") String username);

    @Select("SELECT r.role_code FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<String> findRoleCodesByUserId(@Param("userId") Long userId);

    @Select("SELECT p.permission_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = 0")
    List<String> findPermissionCodesByUserId(@Param("userId") Long userId);
}
