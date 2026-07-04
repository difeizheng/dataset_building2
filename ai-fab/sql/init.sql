-- AI能力封装子系统数据库初始化脚本
-- 达梦DM8数据库

-- 用户表
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    status INT DEFAULT 1 COMMENT '0-禁用 1-正常',
    mfa_enabled INT DEFAULT 0 COMMENT '0-未启用 1-已启用双因子认证',
    mfa_secret VARCHAR(255) COMMENT 'TOTP密钥',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 角色表
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    status INT DEFAULT 1 COMMENT '0-禁用 1-正常',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 权限表
CREATE TABLE sys_permission (
    id BIGINT PRIMARY KEY,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    permission_name VARCHAR(100) NOT NULL,
    resource_type VARCHAR(20) COMMENT 'API, MENU, BUTTON',
    resource_url VARCHAR(255),
    http_method VARCHAR(10),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 用户角色关联表
CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id)
);

-- 角色权限关联表
CREATE TABLE sys_role_permission (
    id BIGINT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES sys_role(id),
    FOREIGN KEY (permission_id) REFERENCES sys_permission(id)
);

-- 初始化数据
-- 管理员用户（密码：admin123，BCrypt加密）
INSERT INTO sys_user (id, username, password, real_name, email, status) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'admin@ctg.com', 1);

-- 角色
INSERT INTO sys_role (id, role_code, role_name, description, status) VALUES
(1, 'admin', '系统管理员', '拥有所有权限', 1),
(2, 'user', '普通用户', '基础使用权限', 1);

-- 权限
INSERT INTO sys_permission (id, permission_code, permission_name, resource_type, resource_url, http_method) VALUES
(1, 'ai:chat', '智能问答', 'API', '/api/v1/ai/chat', 'POST'),
(2, 'document:process', '文档处理', 'API', '/api/v1/document/process', 'POST'),
(3, 'image:understand', '图像理解', 'API', '/api/v1/image/understand', 'POST'),
(4, 'media:process', '音视频处理', 'API', '/api/v1/media/process', 'POST'),
(5, 'diagnosis:analyze', '故障诊断', 'API', '/api/v1/diagnosis/analyze', 'POST'),
(6, 'decision:analyze', '决策分析', 'API', '/api/v1/decision/analyze', 'POST'),
(7, 'bid:assist', '招投标辅助', 'API', '/api/v1/bid/assist', 'POST');

-- 管理员拥有所有权限
INSERT INTO sys_user_role (id, user_id, role_id) VALUES (1, 1, 1);

-- 管理员角色拥有所有权限
INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES
(1, 1, 1), (2, 1, 2), (3, 1, 3), (4, 1, 4), (5, 1, 5), (6, 1, 6), (7, 1, 7);

-- 普通用户角色拥有基础权限
INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES
(8, 2, 1), (9, 2, 2), (10, 2, 3);
