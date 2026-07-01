package com.ctg.aiFab.gateway.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    private String password;

    private String realName;

    private String email;

    private String phone;

    private Integer status; // 0-禁用 1-正常

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
