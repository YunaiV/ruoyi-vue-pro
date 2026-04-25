# Docker Build & Up

目标: 快速部署体验系统，帮助了解系统之间的依赖关系。
依赖：docker compose v2。

## 功能文件列表

```text
.
├── Docker-HOWTO.md                 
├── docker-compose.yml              
├── docker.env                      <-- docker-compose 环境变量配置（DB名/密码等）
└── yudao-server
    └── Dockerfile                  <-- 后端镜像（需先本地 mvn package）
```

前端镜像由 `yudao-ui-deepay/Dockerfile` 构建（多阶段：node:18-alpine → nginx:alpine）。

## 构建 jar 包

```shell
# 创建 maven 缓存 volume
docker volume create --name deepay-maven-repo

docker run -it --rm --name deepay-maven \
    -v deepay-maven-repo:/root/.m2 \
    -v $PWD:/usr/src/mymaven \
    -w /usr/src/mymaven \
    maven mvn clean install package '-Dmaven.test.skip=true'
```

## 构建并启动所有服务

```shell
# 使用 docker.env 中的配置（DB 名 deepay，密码 deepay393163）
docker compose --env-file docker.env up -d

# 仅重建某个服务（例如前端）
docker compose build frontend
docker compose up -d frontend
```

首次运行会自动构建容器。

## 服务端口

| 服务 | 地址 |
|------|------|
| Deepay 前端 | http://localhost:80 |
| 后端 API | http://localhost:48080 |
| MySQL | localhost:3306  root / deepay393163 |
| Redis | localhost:6379 |
