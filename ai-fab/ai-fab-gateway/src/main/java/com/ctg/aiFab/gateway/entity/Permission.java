package com.ctg.aiFab.gateway.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 权限实体
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@TableName("sys_permission")
public class Permission {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String permissionCode;

    private String permissionName;

    private String resourceType; // API, MENU, BUTTON

    private String resourceUrl;

    private String httpMethod; // GET, POST, PUT, DELETE

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
