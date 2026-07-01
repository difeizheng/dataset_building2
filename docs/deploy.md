# 数据集建设子系统 - 部署手册

## 环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 国产化适配 Ascend/海光 |
| 达梦 DM8 | 8.1+ | 国产数据库 |
| Redis | 7.0+ | 缓存 |
| Maven | 3.8+ | 构建工具 |
| Docker | 20.10+ | 容器化部署（可选） |

## 数据库初始化

```bash
# 连接达梦数据库
dmSQL SYSDBA/SYSDBA@localhost:5236

# 执行初始化脚本
SQL> @sql/init.sql
```

## 配置说明

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:dm://localhost:5236/DATA_FAB
    username: SYSDBA
    password: your_password

  data:
    redis:
      host: localhost
      port: 6379
```

## 构建

```bash
cd data-fab
mvn clean package -DskipTests
```

## 运行

### JAR 方式

```bash
java -jar target/data-fab-1.0.0-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:dm://db-host:5236/DATA_FAB \
  --spring.datasource.password=xxx \
  --spring.data.redis.host=redis-host
```

### Docker 方式

```dockerfile
FROM openjdk:17-slim
COPY target/data-fab-1.0.0-SNAPSHOT.jar /app/data-fab.jar
ENTRYPOINT ["java", "-jar", "/app/data-fab.jar"]
```

```bash
docker build -t data-fab:1.0.0 .
docker run -d -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:dm://db-host:5236/DATA_FAB \
  -e SPRING_DATASOURCE_PASSWORD=xxx \
  -e SPRING_DATA_REDIS_HOST=redis-host \
  data-fab:1.0.0
```

## 验证

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# API文档
open http://localhost:8080/swagger-ui.html
```

## 国产化适配说明

### 达梦 DM8
- JDBC 驱动：`com.dameng:DmJdbcDriver18`
- 分页插件：MyBatis-Plus `DbType.DM`

### 华为 Ascend
- JDK 使用毕昇 JDK（ARM 版本）
- 无需修改代码，JVM 层面适配

### 统信 UOS / 麒麟
- Docker 镜像基于国产 OS 构建
- 无平台特定依赖
