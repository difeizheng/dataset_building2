# 集成与管控子系统

## 项目概述

集成与管控子系统是人工智能数据集建设和能力封装服务的核心模块，提供统一身份认证、安全管控、系统集成和运维监控功能。

## 技术栈

- **后端框架**: Spring Boot 3.2.5
- **数据库**: 达梦 DM8（国产化）
- **缓存**: Redis
- **认证**: OAuth2.0 + JWT + 国密SM2/SM3/SM4
- **监控**: Prometheus + Micrometer
- **文档**: OpenAPI 3.0 (SpringDoc)

## 功能模块

### 1. 统一身份认证

- SSO单点登录
- 双因子认证（国密数字证书）
- RBAC权限管理
- 会话管理
- 三权分立（管理员、审计员、操作员）

### 2. 安全管控

- 操作审计日志
- 数据安全分级管控（L1-L4）
- 数据脱敏引擎
- 数据加密存储（国密算法）
- 访问控制策略
- 安全监控与告警

### 3. 系统集成接口

- AI中台对接
- 大模型平台对接
- 大数据平台对接
- 三峡行云审批流程对接
- WPS服务中台对接
- 数字化签章系统对接

### 4. 运维监控平台

- 系统运行状态监控
- 性能指标采集
- 日志聚合与分析
- 异常告警
- 健康检查

## 项目结构

```
src/main/java/com/ctg/integration/
├── config/              # 配置类
├── controller/          # REST控制器
├── crypto/              # 国密算法工具
├── dto/                 # 数据传输对象
├── entity/              # JPA实体
├── exception/           # 异常处理
├── repository/          # 数据访问层
├── security/            # 安全组件
└── service/             # 业务服务
    └── impl/            # 服务实现
```

## 快速开始

### 环境要求

- JDK 17+
- 达梦 DM8
- Redis 6.0+
- Maven 3.8+

### 构建运行

```bash
# 编译项目
mvn clean package

# 运行测试
mvn test

# 启动应用
java -jar target/integration-control-1.0.0-SNAPSHOT.jar
```

### 配置说明

主要配置项在 `application.yml` 中：

- 数据库连接配置
- Redis连接配置
- JWT密钥配置
- 国密算法配置
- 集成系统配置

## API文档

启动应用后访问：
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/api-docs

## 安全特性

### 国密算法支持

- **SM2**: 非对称加密，用于数字签名和密钥交换
- **SM3**: 哈希摘要，用于数据完整性校验
- **SM4**: 对称加密，用于数据加密存储

### 数据分级管控

| 级别 | 名称 | 加密 | 脱敏 | 说明 |
|------|------|------|------|------|
| L1 | 公开数据 | 否 | 否 | 可公开访问 |
| L2 | 内部数据 | 否 | 轻度 | 内部使用 |
| L3 | 敏感数据 | 是 | 中度 | 需要保护 |
| L4 | 机密数据 | 是 | 完全 | 严格保护 |

### 三权分立

- **管理员**: 系统配置和用户管理
- **审计员**: 审计日志查看和安全审计
- **操作员**: 日常业务操作

## 测试覆盖率

目标覆盖率: ≥80%

运行覆盖率报告:
```bash
mvn test jacoco:report
```

报告位置: `target/site/jacoco/index.html`

## 许可证

Copyright © 2026 CTG. All rights reserved.
