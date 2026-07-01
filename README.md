# 数据集建设子系统 (data-fab)

三峡集团人工智能数据集建设和能力封装服务平台 — 数据集建设子系统

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Java 17 + Spring Boot 3.2 + MyBatis-Plus |
| 数据库 | 达梦 DM8（国产化） |
| 缓存 | Redis 7 |
| 文档 | OpenAPI 3.0 (SpringDoc) |
| 测试 | JUnit 5 + Mockito |

## 模块结构

```
data-fab/
├── ingest-service     数据接入模块（四模态数据接入 + L1-L4分级）
├── etl-service        数据清洗与预处理流水线
├── label-service      标注工作台（双盲标注 + IAA仲裁）
├── qa-service         质量管控中心（四模态阈值 + 三审三校）
├── delivery-service   数据集交付（FAIR/Croissant/版本管理）
└── common             公共组件（枚举/DTO/异常/配置）
```

## 快速启动

### 前置条件
- JDK 17+
- Maven 3.8+
- 达梦 DM8 数据库
- Redis 7

### 构建

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
