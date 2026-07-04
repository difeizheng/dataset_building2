# 知识库建设子系统 (kb-fab)

三峡集团人工智能知识库建设子系统

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Java 17 + Spring Boot 3.2 + MyBatis-Plus |
| 数据库 | 达梦 DM8（国产化） |
| 图库 | UGE/GeaGraph（国产） |
| 向量库 | Proton/PGVector（国产） |
| 全文检索 | Elasticsearch 8.11 |
| 缓存 | Redis 7 |
| 文档 | OpenAPI 3.0 (SpringDoc) |
| 测试 | JUnit 5 + Mockito + JaCoCo |

## 模块结构

```
kb-fab/
├── extraction      知识抽取引擎（NER、关系抽取、属性抽取、事件抽取）
├── ontology        本体建模工具
├── storage         知识存储（达梦+图库+向量库三库协同）
├── retrieval       混合检索（向量+图谱+全文+重排）
├── rag            RAG问答服务
├── quality        知识质量评估
├── version        知识版本管理
├── access         知识访问权限控制
└── common         公共组件（枚举/DTO/异常/配置）
```

## 快速启动

### 前置条件
- JDK 17+
- Maven 3.8+
- 达梦 DM8 数据库
- 国产图库（UGE/GeaGraph）
- 国产向量库（Proton/PGVector）
- Elasticsearch 8.11+
- Redis 7

### 构建

```bash
cd kb-fab
mvn clean package -DskipTests
```

### 运行

```bash
java -jar target/kb-fab-1.0.0-SNAPSHOT.jar
```

### 测试

```bash
mvn test
```

### 测试覆盖率

```bash
mvn test jacoco:report
```

目标：≥ 80%

## API 文档

启动后访问：http://localhost:8082/swagger-ui.html

## 核心接口

### 知识抽取
- `POST /api/v1/kb/extraction/tasks` - 创建抽取任务
- `POST /api/v1/kb/extraction/tasks/{taskId}/execute` - 执行抽取任务
- `GET /api/v1/kb/extraction/tasks/{taskId}` - 获取任务详情

### 本体建模
- `POST /api/v1/kb/ontology/classes` - 创建本体类
- `POST /api/v1/kb/ontology/relations` - 创建本体关系
- `POST /api/v1/kb/ontology/validate` - 验证本体一致性

### 知识存储
- `POST /api/v1/kb/storage/knowledge` - 存储知识条目
- `GET /api/v1/kb/storage/knowledge/{id}` - 获取知识条目
- `PUT /api/v1/kb/storage/knowledge/{id}` - 更新知识条目
- `POST /api/v1/kb/storage/knowledge/{id}/publish` - 发布知识

### 知识检索
- `POST /api/v1/kb/retrieval/search` - 执行混合检索
- `GET /api/v1/kb/retrieval/simple` - 简单检索

### RAG问答
- `POST /api/v1/kb/rag/ask` - 执行问答
- `GET /api/v1/kb/rag/simple` - 简单问答

### 知识质量
- `POST /api/v1/kb/quality/evaluate/{knowledgeId}` - 执行质量评估
- `GET /api/v1/kb/quality/latest/{knowledgeId}` - 获取最新评估结果

### 知识版本
- `GET /api/v1/kb/version/{knowledgeId}` - 查询版本历史
- `POST /api/v1/kb/version/{knowledgeId}/rollback/{targetVersion}` - 回滚到指定版本

### 知识权限
- `POST /api/v1/kb/access/grant` - 授予访问权限
- `POST /api/v1/kb/access/revoke/{accessId}` - 撤销访问权限
- `GET /api/v1/kb/access/check` - 检查访问权限

## Mandate 落地

### M4 国产化
- ✅ 达梦 DM8 数据库
- ✅ 国产图库（UGE/GeaGraph）
- ✅ 国产向量库（Proton/PGVector）
- ✅ Elasticsearch 全文检索
- ❌ 禁止 MySQL/PG/Oracle
- ❌ 禁止 Neo4j
- ❌ 禁止原生 Milvus

### P0 核心功能
- ✅ 知识抽取引擎（NER、关系抽取、属性抽取、事件抽取）
- ✅ 本体建模工具
- ✅ 知识存储（达梦+图库+向量库三库协同）
- ✅ 混合检索（向量+图谱+全文+重排）
- ✅ RAG问答服务

### P1 知识管理
- ✅ 知识质量评估（完整性、一致性、准确性）
- ✅ 知识版本管理（版本快照、回滚）
- ✅ 知识访问权限控制（READ/WRITE/ADMIN）

### 性能指标
- 检索响应时间 < 500ms
- 单测覆盖率 ≥ 80%
- 无 Critical/High 安全漏洞

## 数据库初始化

```bash
# 连接达梦数据库
dmSQL SYSDBA/SYSDBA@localhost:5236

# 执行初始化脚本
START sql/init.sql
```

## 架构特点

### 三库协同
1. **达梦 DM8**：存储知识条目元数据、结构化数据
2. **国产图库**：存储知识图谱、实体关系
3. **国产向量库**：存储知识向量、支持语义检索

### 混合检索
- 向量检索（权重 0.4）
- 图谱检索（权重 0.3）
- 全文检索（权重 0.3）
- 智能重排序

### RAG问答
- 检索增强生成
- 多轮对话支持
- 引用溯源

## 开发规范

### 代码规范
- 遵循阿里巴巴Java开发手册
- 使用 Lombok 简化代码
- 统一异常处理
- 统一响应格式

### 测试规范
- 单元测试覆盖率 ≥ 80%
- 使用 JUnit 5 + Mockito
- 集成测试使用 Testcontainers

### 安全规范
- 无硬编码密钥
- 参数化查询防SQL注入
- 输入验证
- 权限控制

## 部署说明

### 环境变量

```bash
# 数据库配置
DB_USERNAME=SYSDBA
DB_PASSWORD=SYSDBA

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379

# 图库配置
GRAPH_DB_TYPE=uge
GRAPH_DB_URL=http://localhost:7474

# 向量库配置
VECTOR_DB_TYPE=proton
VECTOR_DB_URL=http://localhost:8080

# Elasticsearch配置
ES_URL=http://localhost:9200
```

### Docker部署（待实现）

```bash
docker build -t kb-fab:1.0.0 .
docker run -p 8082:8082 kb-fab:1.0.0
```

## 许可证

Copyright © 2026 三峡集团
