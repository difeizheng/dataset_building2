-- 集成与管控子系统数据库初始化脚本
-- 数据库: 达梦 DM8（国产化要求 M4）
-- 满足等保三级要求、三权分立

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    employee_id VARCHAR(20),
    department VARCHAR(50),
    enabled BIT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    last_login_at TIMESTAMP,
    login_fail_count INT DEFAULT 0,
    locked_until TIMESTAMP
);
CREATE INDEX idx_user_username ON sys_user(username);
CREATE INDEX idx_user_email ON sys_user(email);
CREATE INDEX idx_user_department ON sys_user(department);

-- 角色表（三权分立: ADMIN/AUDITOR/OPERATOR）
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    role_type VARCHAR(20) NOT NULL,  -- ADMIN/AUDITOR/OPERATOR
    enabled BIT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE INDEX idx_role_code ON sys_role(code);
CREATE INDEX idx_role_type ON sys_role(role_type);

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    permission_type VARCHAR(20) NOT NULL,  -- MENU/BUTTON/API/DATA
    resource VARCHAR(255),
    method VARCHAR(50),
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_permission_code ON sys_permission(code);
CREATE INDEX idx_permission_type ON sys_permission(permission_type);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
);

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES sys_permission(id) ON DELETE CASCADE
);

-- 审计日志表（等保三级要求：完整可追溯）
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    username VARCHAR(64) NOT NULL,
    real_name VARCHAR(100),
    client_ip VARCHAR(50),
    user_agent VARCHAR(500),
    operation VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50),
    resource_id VARCHAR(100),
    resource_name VARCHAR(255),
    description VARCHAR(500),
    request_method VARCHAR(10),
    request_url VARCHAR(500),
    request_params CLOB,
    response_status INT,
    result VARCHAR(20) NOT NULL,  -- SUCCESS/FAILURE
    error_message CLOB,
    duration BIGINT,
    data_level VARCHAR(10),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_audit_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_operation ON audit_log(operation);
CREATE INDEX idx_audit_created_at ON audit_log(created_at);
CREATE INDEX idx_audit_resource_type ON audit_log(resource_type);
CREATE INDEX idx_audit_result ON audit_log(result);

-- 用户会话表
CREATE TABLE IF NOT EXISTS user_session (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    username VARCHAR(64) NOT NULL,
    session_token VARCHAR(255) NOT NULL UNIQUE,
    refresh_token VARCHAR(255),
    client_ip VARCHAR(50),
    user_agent VARCHAR(500),
    device_type VARCHAR(50),
    login_at TIMESTAMP NOT NULL,
    expire_at TIMESTAMP NOT NULL,
    last_active_at TIMESTAMP,
    is_active BIT NOT NULL DEFAULT 1,
    mfa_verified BIT NOT NULL DEFAULT 0
);
CREATE INDEX idx_session_user_id ON user_session(user_id);
CREATE INDEX idx_session_token ON user_session(session_token);
CREATE INDEX idx_session_expire ON user_session(expire_at);
CREATE INDEX idx_session_active ON user_session(is_active);

-- 数据分级表（L1-L4）
CREATE TABLE IF NOT EXISTS data_classification (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    classification_name VARCHAR(100) NOT NULL,
    security_level VARCHAR(10) NOT NULL,  -- L1/L2/L3/L4
    require_encryption BIT NOT NULL DEFAULT 0,
    require_masking BIT NOT NULL DEFAULT 0,
    masking_rule CLOB,
    access_policy VARCHAR(255),
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE INDEX idx_data_class_level ON data_classification(security_level);

-- 监控告警表
CREATE TABLE IF NOT EXISTS monitor_alert (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    alert_name VARCHAR(100) NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,  -- INFO/WARNING/CRITICAL/FATAL
    status VARCHAR(20) NOT NULL,    -- TRIGGERED/ACKNOWLEDGED/RESOLVED
    message VARCHAR(500),
    source VARCHAR(100),
    metric_name VARCHAR(100),
    metric_value DOUBLE,
    threshold DOUBLE,
    triggered_at TIMESTAMP NOT NULL,
    acknowledged_at TIMESTAMP,
    acknowledged_by VARCHAR(64),
    resolved_at TIMESTAMP
);
CREATE INDEX idx_alert_severity ON monitor_alert(severity);
CREATE INDEX idx_alert_status ON monitor_alert(status);
CREATE INDEX idx_alert_triggered_at ON monitor_alert(triggered_at);

-- 初始化三权分立角色
INSERT INTO sys_role (code, name, description, role_type) VALUES ('ROLE_ADMIN', '系统管理员', '系统管理权限', 'ADMIN');
INSERT INTO sys_role (code, name, description, role_type) VALUES ('ROLE_AUDITOR', '审计管理员', '审计管理权限', 'AUDITOR');
INSERT INTO sys_role (code, name, description, role_type) VALUES ('ROLE_OPERATOR', '操作员', '日常操作权限', 'OPERATOR');

-- 初始化数据分级（L1-L4）
INSERT INTO data_classification (classification_name, security_level, require_encryption, require_masking, description) VALUES ('公开数据', 'L1', 0, 0, '可公开访问的数据');
INSERT INTO data_classification (classification_name, security_level, require_encryption, require_masking, description) VALUES ('内部数据', 'L2', 0, 1, '内部使用数据，需要轻度脱敏');
INSERT INTO data_classification (classification_name, security_level, require_encryption, require_masking, description) VALUES ('敏感数据', 'L3', 1, 1, '敏感数据，需要加密存储和脱敏');
INSERT INTO data_classification (classification_name, security_level, require_encryption, require_masking, description) VALUES ('机密数据', 'L4', 1, 1, '机密数据，需要严格加密和完全脱敏');

-- 初始化管理员账户（密码: admin123，使用BCrypt加密）
INSERT INTO sys_user (username, password, real_name, email, enabled) VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '系统管理员', 'admin@ctg.com', 1);

-- 为管理员分配管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);
