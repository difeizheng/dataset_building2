# 三峡集团人工智能数据集建设和能力封装服务平台

三峡集团人工智能数据集建设和能力封装服务平台是基于微服务架构的数据集全生命周期管理平台，包含四个核心子系统。

## 系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│                    集成与管控子系统 (gov-fab)                     │
│  统一身份认证 | 安全管控 | 系统集成 | 运维监控                      │
├─────────────┬─────────────────┬─────────────────┬───────────────┤
│ 数据建设子系统│   知识管理子系统   │   AI能力子系统   │               │
│ (data-fab)  │   (kb-fab)      │   (ai-fab)      │               │
│             │                 │                 │               │
│ 四模态数据   │  知识抽取与存储    │  AI模型推理     │               │
│ 采集/清洗   │  质量评估        │  应用编排       │               │
│ 标注/质检   │  知识检索        │  大模型对接     │               │
└─────────────┴─────────────────┴─────────────────┴───────────────┘
```

## 子系统

| 子系统 | 分支 | 技术栈 | 描述 |
|--------|------|--------|------|
| gov-fab | agent/developer/gov-fab-rebuild | Spring Boot 3.2 + JPA | 统一身份认证、安全管控、系统集成、运维监控 |
| data-fab | agent/developer/data-fab-fix | Spring Boot 3.2 + MyBatis-Plus | 数据集全生命周期管理（四模态采集、ETL、标注、质检、交付） |
| kb-fab | agent/developer/kb-fab-rebuild | Spring Boot 3.2 + MyBatis-Plus | 知识抽取、存储、质量评估、检索 |
| ai-fab | agent/developer/ai-fab-rebuild | Python FastAPI + Vue | AI能力封装、大模型对接、应用编排 |

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Java 17 + Spring Boot 3.2, Python 3.11 + FastAPI |
| 数据库 | 达梦 DM8（国产化） |
| 缓存 | Redis 7 |
| 前端 | Vue 3 + Vite |
| 文档 | OpenAPI 3.0 (SpringDoc) |
| 测试 | JUnit 5 + Mockito, Playwright |

## 模块结构

### data-fab（数据建设子系统）
```
data-fab/
├── ingest-service     数据接入模块（四模态数据接入 + L1-L4分级）
├── etl-service        数据清洗与预处理流水线
├── label-service      标注工作台（双盲标注 + IAA仲裁）
├── qa-service         质量管控中心（四模态阈值 + 三审三校）
├── delivery-service   数据集交付（FAIR/Croissant/版本管理）
└── common             公共组件（枚举/DTO/异常/配置）
```

### gov-fab（集成与管控子系统）
```
src/main/java/com/ctg/integration/
├── config/              配置类
├── controller/          REST控制器
├── crypto/              国密算法工具
├── dto/                 数据传输对象
├── entity/              JPA实体
├── exception/           异常处理
├── repository/          数据访问层
├── security/            安全组件
└── service/             业务服务
    └── impl/            服务实现
```

### kb-fab（知识管理子系统）
```
kb-fab/
├── access/              知识访问控制
├── extraction/          知识抽取引擎
├── storage/             知识存储服务
├── quality/            质量评估
└── common/              公共组件
```

### ai-fab（AI能力子系统）
```
ai-fab/
├── ai-fab-python/        Python FastAPI后端
│   └── app/
│       ├── api/         API路由
│       ├── core/        核心模块
│       └── services/    AI服务
└── ai-fab-frontend/     Vue 3前端
    └── src/
        ├── api/         API调用
        ├── views/       页面视图
        └── stores/      状态管理
```

## 快速启动

### 前置条件
- JDK 17+
- Maven 3.8+
- Python 3.11+
- Node.js 18+
- 达梦 DM8 数据库
- Redis 7

### 构建（data-fab 示例）

```bash
cd data-fab
mvn clean package -DskipTests
```

### 运行

```bash
java -jar target/data-fab-1.0.0-SNAPSHOT.jar
```

### 测试

```bash
mvn test
```

## API 文档

启动后访问：http://localhost:8080/swagger-ui.html

## 核心接口

### 数据接入
- `POST /api/v1/data/datasets` - 创建数据集
- `POST /api/v1/data/samples` - 创建数据样本
- `GET /api/v1/data/samples` - 分页查询数据样本

### 数据清洗
- `POST /api/v1/etl/rules` - 创建清洗规则
- `POST /api/v1/etl/tasks` - 创建清洗任务
- `POST /api/v1/etl/tasks/{id}/execute` - 执行清洗任务

### 标注工作台
- `POST /api/v1/label/tasks` - 创建标注任务
- `POST /api/v1/label/submit` - 提交标注
- `POST /api/v1/label/arbitrate` - 触发仲裁
- `GET /api/v1/label/tasks/{id}/iaa` - 计算IAA得分

### 质量管控
- `POST /api/v1/qa/evaluate` - 执行质量评估
- `POST /api/v1/qa/tasks/{id}/manual-review` - 人工复审
- `POST /api/v1/qa/tasks/{id}/expert-review` - 专家终审
- `POST /api/v1/qa/tasks/{id}/publish` - 发布数据集

### 数据集交付
- `POST /api/v1/delivery/publish` - 发布数据集
- `GET /api/v1/delivery/{id}/download` - 生成下载令牌

## Mandate 落地

### M1 数据分级 L1~L4
- `DataLevel` 枚举定义四级分级
- 采集即分级，每个数据样本必须指定分级
- L4 核心数据禁止下载/外发

### M2 四模态质量阈值
- `QualityThresholdEngine` 硬编码全部阈值
- 文本：完整率>99.5%、毒性<0.1%
- 图像：IoU>0.85、违规<0.05%
- 音频：SNR>20dB、CER<3%
- 视频：>720p、>24fps
- 三审三校流程引擎

### M3 标注一致性 IAA
- `IaaEngine` 实现 Cohen/Fleiss Kappa 计算
- Kappa >= 0.85 → 采纳
- 0.70 <= Kappa < 0.85 → 仲裁
- Kappa < 0.70 → 自动重标

### M4 国产化
- 达梦 DM8 数据库
- MyBatis-Plus DM 分页插件
- 无 MySQL/PG/Oracle 依赖

## 测试覆盖率

```
mvn test jacoco:report
```

目标：≥ 80%

## 安全特性

### 国密算法支持（gov-fab）

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

### 三权分立（gov-fab）

- **管理员**: 系统配置和用户管理
- **审计员**: 审计日志查看和安全审计
- **操作员**: 日常业务操作

## 许可证

Copyright © 2026 CTG. All rights reserved.
