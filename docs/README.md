# OpenAPI 文档索引

## 概述

三峡集团人工智能数据集建设和能力封装服务平台包含四个子系统，每个子系统都有独立的 OpenAPI 文档。

## 子系统 API 文档

### 1. 集成与管控子系统 (gov-fab)

**Swagger UI**: http://localhost:8080/api/swagger-ui.html
**OpenAPI JSON**: http://localhost:8080/api/v3/api-docs
**OpenAPI YAML**: `docs/openapi-gov-fab.yaml`

**主要模块**:
- 统一身份认证 (Auth)
- 用户管理 (User)
- 角色权限 (Role/Permission)
- 审计日志 (AuditLog)
- 数据分级 (DataClassification)
- 系统集成 (AI中台/大模型/行云/WPS/签章)
- 运维监控 (Monitor)

### 2. 数据建设子系统 (data-fab)

**Swagger UI**: http://localhost:8081/swagger-ui.html
**OpenAPI JSON**: http://localhost:8081/v3/api-docs
**OpenAPI YAML**: `docs/openapi-data-fab.yaml`

**主要模块**:
- 数据集管理 (Dataset)
- 数据样本管理 (DataSample)
- ETL 清洗 (EtlRule/EtlTask)
- 标注工作台 (LabelTask/LabelRecord)
- 质量管控 (QaTask)
- 数据集交付 (Delivery)

### 3. 知识管理子系统 (kb-fab)

**Swagger UI**: http://localhost:8082/swagger-ui.html
**OpenAPI JSON**: http://localhost:8082/v3/api-docs
**OpenAPI YAML**: `docs/openapi-kb-fab.yaml`

**主要模块**:
- 知识抽取 (Extraction)
- 本体管理 (Ontology)
- 知识存储 (Storage)
- 质量评估 (Quality)
- RAG 检索 (Rag)
- 混合检索 (Retrieval)
- 版本管理 (Version)

### 4. AI 能力子系统 (ai-fab)

**Gateway API**:
- **Swagger UI**: http://localhost:8083/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8083/v3/api-docs

**Python Backend**:
- **API Root**: http://localhost:8000/docs
- **OpenAPI JSON**: http://localhost:8000/openapi.json

**Vue Frontend**:
- **URL**: http://localhost:3000

**主要模块**:
- 智能问诊 (Diagnosis)
- 辅助决策 (Decision)
- 文档处理 (Document)
- 图像识别 (Image)
- 音视频处理 (Media)
- 标书分析 (Bid)

## 聚合 API 文档

各子系统通过 API Gateway 统一对外提供服务，完整系统部署后可通过统一的 API 文档访问。

## 本地开发

启动完整本地环境：

```bash
# 复制环境变量配置
cp .env.example .env

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 停止所有服务
docker-compose down
```

## 环境要求

- Docker 20.10+
- Docker Compose 1.29+
- 16GB+ RAM (推荐)
- 50GB+ 磁盘空间
