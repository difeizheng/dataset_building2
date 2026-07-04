# AI能力封装子系统 (ai-fab)

三峡集团人工智能能力封装子系统 — 统一AI工作台

## 技术栈

| 层 | 技术 |
|----|------|
| 前端 | Vue 3 + Element Plus + Vite |
| 后端 | Java 17 + Spring Boot 3.2 + MyBatis-Plus |
| Python服务 | Python 3.11 + FastAPI |
| 数据库 | 达梦 DM8（国产化） |
| 缓存 | Redis 7 |
| 文档 | OpenAPI 3.0 (SpringDoc) |
| 测试 | JUnit 5 + Vitest + Pytest |

## 模块结构

```
ai-fab/
├── ai-fab-frontend/     Vue3前端 - AI应用门户
├── ai-fab-gateway/      Spring Boot后端 - API网关 + RBAC + 限流
├── ai-fab-python/       Python服务 - 文档/图像/音视频/诊断/决策/招投标
└── README.md
```

## 功能清单

### P0 - 核心功能

- [x] AI应用门户（Vue3前端）
  - [x] 统一AI工作台界面
  - [x] 大模型问答界面
  - [x] 知识库问答界面
  - [x] 智能助手入口
- [x] 场景化能力接口（后端API）
  - [x] 智能问答API
  - [x] 文档智能处理API
  - [x] 图像理解API
  - [x] 音视频理解API
- [x] 能力编排引擎
  - [x] API网关
  - [x] 能力组合编排
  - [x] 权限控制（RBAC）
  - [x] 限流与熔断

### P1 - 扩展功能

- [x] 故障诊断API
- [x] 决策分析API
- [x] 招投标智能辅助API

## 快速启动

### 前置条件
- JDK 17+
- Maven 3.8+
- Python 3.11+
- Node.js 18+
- 达梦 DM8 数据库
- Redis 7

### 1. 启动后端网关

```bash
cd ai-fab-gateway
mvn clean package -DskipTests
java -jar target/ai-fab-gateway-1.0.0-SNAPSHOT.jar
```

### 2. 启动Python服务

```bash
cd ai-fab-python
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

### 3. 启动前端

```bash
cd ai-fab-frontend
npm install
npm run dev
```

访问：http://localhost:3000

## API文档

- 后端网关：http://localhost:8080/swagger-ui.html
- Python服务：http://localhost:8000/docs

## 测试

```bash
# 后端测试
cd ai-fab-gateway && mvn test

# Python测试
cd ai-fab-python && pytest

# 前端测试
cd ai-fab-frontend && npm run test:coverage
```

## 技术约束

- ✅ 前端：Vue3 + Element Plus
- ✅ 后端：Spring Boot + Python
- ✅ API网关：自研（Spring Boot实现）
- ✅ 大模型：调用集团AI中台（不重复训练）
- ✅ 国产化：达梦DM8数据库

## 安全特性

- JWT认证
- RBAC权限控制
- Redis滑动窗口限流
- 全局异常处理
- 参数校验

## 性能指标

- API响应时间 < 3秒
- 前端页面加载 < 2秒
- 单测覆盖率 ≥ 80%
