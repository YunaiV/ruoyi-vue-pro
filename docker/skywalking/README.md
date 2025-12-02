# SkyWalking 链路追踪部署指南

## 简介

SkyWalking 是一个开源的 APM（应用性能监控）系统，提供分布式追踪、服务网格遥测分析、度量聚合和可视化等功能。

## 快速启动

### 1. 启动 SkyWalking 服务

```bash
cd docker/skywalking
docker-compose up -d
```

### 2. 检查服务状态

```bash
docker-compose ps
docker-compose logs -f skywalking-oap
```

### 3. 访问 SkyWalking UI

- **地址**: http://localhost:8080
- 首次启动需要等待 1-2 分钟

## 集成 Java 应用

### 下载 SkyWalking Agent

```bash
# 下载 Agent（版本需与 OAP Server 匹配）
wget https://archive.apache.org/dist/skywalking/java-agent/9.1.0/apache-skywalking-java-agent-9.1.0.tgz
tar -xzf apache-skywalking-java-agent-9.1.0.tgz
```

### 启动应用（附加 Agent）

```bash
java -javaagent:/path/to/skywalking-agent/skywalking-agent.jar \
     -Dskywalking.agent.service_name=yudao-server \
     -Dskywalking.collector.backend_service=127.0.0.1:11800 \
     -jar yudao-server/target/yudao-server.jar \
     --spring.profiles.active=local
```

### 使用启动脚本

项目提供了便捷的启动脚本：

```bash
# 使用 SkyWalking Agent 启动
./scripts/start-with-skywalking.sh
```

## 服务端口说明

| 服务 | 端口 | 说明 |
|------|------|------|
| SkyWalking UI | 8080 | Web 管理界面 |
| SkyWalking OAP gRPC | 11800 | Agent 数据上报端口 |
| SkyWalking OAP REST | 12800 | REST API 端口 |

## 配置说明

### 存储选项

默认使用 H2 内存数据库（适合开发测试），生产环境建议使用 Elasticsearch：

```yaml
environment:
  SW_STORAGE: elasticsearch
  SW_STORAGE_ES_CLUSTER_NODES: elasticsearch:9200
```

### Agent 配置参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `skywalking.agent.service_name` | 服务名称 | `Your_ApplicationName` |
| `skywalking.collector.backend_service` | OAP 地址 | `127.0.0.1:11800` |
| `skywalking.agent.sample_n_per_3_secs` | 每3秒采样数 | `-1`（全采样） |

## 常用操作

### 停止服务

```bash
docker-compose down
```

### 清理数据

```bash
docker-compose down -v
```

### 查看日志

```bash
docker-compose logs -f skywalking-oap
docker-compose logs -f skywalking-ui
```

## 故障排查

### 1. OAP 启动失败

检查端口是否被占用：
```bash
lsof -i:11800
lsof -i:12800
```

### 2. Agent 无法连接 OAP

确认 OAP 服务已启动并健康：
```bash
curl http://localhost:12800/healthcheck
```

### 3. UI 无数据显示

- 确认 Agent 配置正确
- 检查应用是否有请求产生
- 查看 Agent 日志（应用 logs 目录下的 skywalking-api.log）

## 参考文档

- [SkyWalking 官方文档](https://skywalking.apache.org/docs/)
- [SkyWalking Java Agent](https://skywalking.apache.org/docs/skywalking-java/latest/en/setup/service-agent/java-agent/readme/)
