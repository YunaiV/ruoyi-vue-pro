# Docker Build & Up

目标: 快速部署体验系统，帮助了解系统之间的依赖关系。
依赖：docker compose v2，删除`name: yudao-system`，降低`version`版本为`3.3`以下，支持`docker-compose`。

## 功能文件列表

```text
.
├── Docker-HOWTO.md                 
├── docker-compose.yml              
├── docker.env                      <-- 提供docker-compose环境变量配置
├── yudao-server
│   └── Dockerfile
└── yudao-ui-admin
    ├── .dockerignore
    ├── Dockerfile
    └── nginx.conf                  <-- 提供基础配置，gzip压缩、api转发
```

## 构建 jar 包

```shell
# 创建maven缓存volume
docker volume create --name yudao-maven-repo

docker run -it --rm --name yudao-maven \
    -v yudao-maven-repo:/root/.m2 \
    -v $PWD:/usr/src/mymaven \
    -w /usr/src/mymaven \
    maven mvn clean install package '-Dmaven.test.skip=true'
```

## 构建启动服务

```shell
docker compose --env-file docker.env up -d
```

首次运行会自动构建容器。可以通过`docker compose build [service]`来手动构建所有或某个docker镜像

`--env-file docker.env`为可选参数，只是展示了通过`.env`文件配置容器启动的环境变量，`docker-compose.yml`本身已经提供足够的默认参数来正常运行系统。

## 服务器的宿主机端口映射

- admin ui: http://localhost:8080
- api server: http://localhost:48080
- mysql: root/123456, port: 3306
- redis: port: 6379
