-- ============================================
-- data-fab 数据访问控制与审计日志表
-- 对应 C2 修复 + M-1 审计日志落地
-- ============================================

-- 审计日志表 (WORM - Write Once Read Many)
CREATE TABLE IF NOT EXISTS t_audit_log (
    id BIGINT PRIMARY KEY,
    resource_id BIGINT NOT NULL COMMENT '资源ID (样本ID/数据集ID等)',
    resource_type VARCHAR(32) NOT NULL COMMENT '资源类型: SAMPLE, DATASET, DELIVERY',
    action VARCHAR(32) NOT NULL COMMENT '操作类型: DOWNLOAD, PRINT, EXPORT, VIEW',
    user_id BIGINT COMMENT '用户ID',
    result VARCHAR(32) NOT NULL COMMENT '操作结果: ALLOWED, BLOCKED, PENDING_APPROVAL',
    description VARCHAR(512) COMMENT '描述',
    ip_address VARCHAR(64) COMMENT 'IP地址',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 数据审批表
CREATE TABLE IF NOT EXISTS t_data_approval (
    id BIGINT PRIMARY KEY,
    approval_no VARCHAR(64) NOT NULL UNIQUE COMMENT '审批单号',
    resource_id BIGINT NOT NULL COMMENT '资源ID (样本ID/数据集ID)',
    resource_type VARCHAR(32) NOT NULL COMMENT '资源类型: SAMPLE, DATASET',
    action VARCHAR(32) NOT NULL COMMENT '操作类型: DOWNLOAD, PRINT, EXPORT',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    approver_id BIGINT COMMENT '审批人ID',
    status INT DEFAULT 0 COMMENT '审批状态: 0-待审批, 1-已通过, 2-已拒绝',
    comment VARCHAR(512) COMMENT '审批意见',
    approve_time TIMESTAMP COMMENT '审批时间',
    expire_time TIMESTAMP COMMENT '过期时间',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_audit_log_resource ON t_audit_log(resource_id, resource_type);
CREATE INDEX IF NOT EXISTS idx_audit_log_user ON t_audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_create_time ON t_audit_log(create_time);
CREATE INDEX IF NOT EXISTS idx_approval_no ON t_data_approval(approval_no);
CREATE INDEX IF NOT EXISTS idx_approval_resource ON t_data_approval(resource_id, resource_type, action);
CREATE INDEX IF NOT EXISTS idx_approval_applicant ON t_data_approval(applicant_id);
