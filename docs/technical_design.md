# 集成与管控子系统 - 技术文档

## 1. 架构设计

### 1.1 系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway                               │
└─────────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
┌───────▼───────┐    ┌────────▼────────┐    ┌──────▼──────┐
│  认证服务      │    │   安全管控服务   │    │  集成服务    │
│  - SSO登录    │    │  - 审计日志     │    │  - AI中台   │
│  - MFA认证    │    │  - 数据脱敏     │    │  - 大模型   │
│  - 会话管理   │    │  - 数据加密     │    │  - 大数据   │
└───────────────┘    └─────────────────┘    └─────────────┘
        │                     │                     │
        └─────────────────────┼─────────────────────┘
                              │
                    ┌─────────▼─────────┐
                    │    数据访问层      │
                    │  - JPA/Hibernate  │
                    │  - Redis缓存      │
                    └───────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
┌───────▼───────┐    ┌────────▼────────┐    ┌──────▼──────┐
│   MySQL       │    │     Redis       │    │  外部系统   │
│  - 用户数据   │    │   - 会话缓存    │    │  - AI中台   │
│  - 审计日志   │    │   - 令牌存储    │    │  - 大模型   │
│  - 权限数据   │    │                 │    │  - 大数据   │
└───────────────┘    └─────────────────┘    └─────────────┘
```

### 1.2 模块划分

| 模块 | 职责 | 关键类 |
|------|------|--------|
| 认证模块 | 用户认证、令牌管理 | AuthenticationService, JwtTokenProvider |
| 安全模块 | 审计日志、数据脱敏 | AuditLogService, DataMaskingEngine |
| 集成模块 | 外部系统对接 | IntegrationService |
| 监控模块 | 系统监控、告警 | MonitorService |
| 用户模块 | 用户管理、权限管理 | UserService |
| 分级模块 | 数据分级管理 | DataClassificationService |

## 2. 核心功能实现

### 2.1 统一身份认证

#### 2.1.1 认证流程

```
1. 用户提交用户名/密码
2. 验证用户凭证
3. 检查账户状态（是否锁定、是否启用）
4. 验证双因子认证（如需要）
5. 生成JWT访问令牌和刷新令牌
6. 创建会话记录
7. 记录审计日志
8. 返回令牌和用户信息
```

#### 2.1.2 JWT令牌结构

```json
{
  "sub": "username",
  "roles": ["ROLE_ADMIN", "ROLE_USER"],
  "jti": "unique-token-id",
  "iat": 1234567890,
  "exp": 1234567890
}
```

#### 2.1.3 三权分立

| 角色类型 | 权限范围 |
|----------|----------|
| ADMIN | 系统配置、用户管理、角色分配 |
| AUDITOR | 审计日志查看、安全审计、合规检查 |
| OPERATOR | 日常业务操作、数据查询 |

### 2.2 安全管控

#### 2.2.1 审计日志

记录所有关键操作：
- 认证相关：登录、登出、密码修改
- 用户管理：创建、更新、删除用户
- 数据操作：查询、导出、导入
- 系统配置：配置变更

#### 2.2.2 数据脱敏

支持多种脱敏策略：
- 手机号：138****5678
- 身份证：110***********1234
- 邮箱：t***t@example.com
- 姓名：张*
- 银行卡：6222 **** **** 1234

#### 2.2.3 国密算法

- **SM2**: 256位非对称加密
- **SM3**: 256位哈希摘要
- **SM4**: 128位对称加密（CBC模式）

### 2.3 系统集成

#### 2.3.1 集成接口

| 系统 | 接口 | 功能 |
|------|------|------|
| AI中台 | /integration/ai-platform | 模型调用、算力管理 |
| 大模型平台 | /integration/llm | 问答、生成能力 |
| 大数据平台 | /integration/big-data | 数据接入与分发 |
| 三峡行云 | /integration/xingyun | 审批流程 |
| WPS服务 | /integration/wps | 文档处理 |
| 签章系统 | /integration/signature | 数字化签章 |

#### 2.3.2 集成模式

- 同步调用：REST API
- 异步调用：消息队列（待实现）
- 回调通知：Webhook（待实现）

### 2.4 运维监控

#### 2.4.1 监控指标

- CPU使用率
- 内存使用情况
- JVM堆内存
- 线程数
- 请求响应时间
- 错误率

#### 2.4.2 告警级别

| 级别 | 说明 | 响应时间 |
|------|------|----------|
| INFO | 信息提示 | 无需响应 |
| WARNING | 警告 | 1小时内 |
| CRITICAL | 严重 | 15分钟内 |
| FATAL | 致命 | 立即响应 |

## 3. 数据库设计

### 3.1 核心表

- `sys_user`: 用户表
- `sys_role`: 角色表
- `sys_permission`: 权限表
- `sys_user_role`: 用户角色关联表
- `sys_role_permission`: 角色权限关联表
- `audit_log`: 审计日志表
- `user_session`: 用户会话表
- `data_classification`: 数据分级表
- `monitor_alert`: 监控告警表

### 3.2 索引设计

- 用户表：username, email, department
- 审计日志表：user_id, operation, created_at
- 会话表：session_token, user_id, expire_at
- 告警表：severity, status, triggered_at

## 4. 安全设计

### 4.1 认证安全

- 密码使用BCrypt加密存储
- JWT令牌使用HMAC-SHA256签名
- 支持令牌刷新和失效机制
- 登录失败次数限制和账户锁定

### 4.2 数据安全

- 敏感数据使用SM4加密存储
- 数据传输使用HTTPS
- 数据脱敏根据安全级别自动处理
- 审计日志完整记录所有操作

### 4.3 接口安全

- 所有接口需要JWT认证
- 支持基于角色的访问控制
- 请求参数验证
- 全局异常处理

## 5. 部署说明

### 5.1 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 6.0+

### 5.2 配置项

```yaml
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/integration_control
spring.datasource.username=root
spring.datasource.password=your_password

# Redis配置
spring.data.redis.host=localhost
spring.data.redis.port=6379

# JWT配置
jwt.secret=your-256-bit-secret-key
jwt.expiration=3600000

# 国密算法配置
crypto.sm2.enabled=true
crypto.sm3.enabled=true
crypto.sm4.enabled=true
```

### 5.3 启动命令

```bash
# 开发环境
mvn spring-boot:run

# 生产环境
java -jar target/integration-control-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

## 6. 测试说明

### 6.1 单元测试

- 国密算法测试
- JWT令牌测试
- 数据脱敏测试
- 服务层测试

### 6.2 集成测试

- API接口测试
- 数据库操作测试
- 安全认证测试

### 6.3 运行测试

```bash
# 运行所有测试
mvn test

# 生成覆盖率报告
mvn test jacoco:report
```

## 7. 性能优化

### 7.1 数据库优化

- 使用连接池（HikariCP）
- 合理设计索引
- 分页查询
- 批量操作

### 7.2 缓存策略

- 用户信息缓存
- 权限数据缓存
- 会话信息缓存

### 7.3 异步处理

- 审计日志异步写入
- 集成调用异步处理

## 8. 扩展性设计

### 8.1 插件化集成

- 集成服务采用接口设计
- 支持动态添加新的集成系统
- 配置化启用/禁用集成

### 8.2 策略模式

- 数据脱敏策略可配置
- 加密算法可扩展
- 认证方式可扩展

## 9. 监控与运维

### 9.1 健康检查

- 数据库连接检查
- Redis连接检查
- 外部系统连接检查

### 9.2 日志管理

- 日志分级（DEBUG/INFO/WARN/ERROR）
- 日志轮转
- 日志归档

### 9.3 指标暴露

- Prometheus指标端点：/actuator/prometheus
- 健康检查端点：/actuator/health
- 自定义指标：/monitor/metrics
