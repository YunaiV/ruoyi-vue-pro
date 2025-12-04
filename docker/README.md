# 芋道 Docker 部署指南

## 目录结构

```
docker/
├── server/
│   ├── Dockerfile.dev         # 后端开发环境（远程调试）
│   └── Dockerfile.prod        # 后端生产环境（多阶段构建）
├── admin-vben/
│   ├── Dockerfile.dev         # 前端开发环境
│   ├── Dockerfile.prod        # 前端生产环境
│   └── nginx.prod.conf        # Nginx 配置
├── docker-compose.infra.yml   # 仅基础设施（PostgreSQL + Redis）
├── docker-compose.dev.yml     # 开发环境
├── docker-compose.prod.yml    # 生产环境
└── .env.example               # 环境变量示例
```

## 快速开始

### 仅启动基础设施

```bash
# 仅需 PostgreSQL + Redis（本地 IDE 开发时使用）
docker compose -f docker/docker-compose.infra.yml up -d
```

### 开发环境

```bash
# 1. 构建后端 jar 包
mvn clean package -DskipTests -pl yudao-server -am

# 2. 启动基础设施 + 后端
docker compose -f docker/docker-compose.infra.yml -f docker/docker-compose.dev.yml up -d

# 3. 查看日志
docker compose -f docker/docker-compose.dev.yml logs -f server
```

**访问地址：**
- 后端 API: http://localhost:48080
- 远程调试: localhost:5005

### 生产环境

```bash
# 1. 配置环境变量
cp docker/.env.example docker/.env
vim docker/.env  # 修改密码

# 2. 启动基础设施 + 应用
docker compose -f docker/docker-compose.infra.yml -f docker/docker-compose.prod.yml up -d --build
```

**访问地址：**
- 前端: http://localhost:80
- 后端 API: http://localhost:48080

## 环境变量

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `POSTGRES_DB` | `ruoyi-vue-pro` | 数据库名 |
| `POSTGRES_USER` | `postgres` | 数据库用户 |
| `POSTGRES_PASSWORD` | `123456` | 数据库密码 |
| `REDIS_PASSWORD` | - | Redis 密码（可选） |
| `SERVER_PORT` | `48080` | 后端端口 |
| `DEBUG_PORT` | `5005` | 调试端口 |
| `ADMIN_PORT` | `80` | 前端端口 |

## 常用命令

```bash
# 查看状态
docker compose -f docker/docker-compose.dev.yml ps

# 查看日志
docker compose -f docker/docker-compose.dev.yml logs -f server

# 重启后端
docker compose -f docker/docker-compose.dev.yml restart server

# 停止
docker compose -f docker/docker-compose.dev.yml down

# 清理数据卷
docker compose -f docker/docker-compose.dev.yml down -v
```
