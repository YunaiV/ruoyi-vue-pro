## 0. 友情提示

在 `sql/tools` 目录下，我们提供一些数据库相关的工具，包括测试数据库的快速启动、MySQL 转换其它数据库等等。

注意！所有的操作，必须在 `sql/tools` 目录下执行。

## 1. 测试数据库的快速启动

基于 Docker Compose，快速启动 MySQL、Oracle、PostgreSQL、SQL Server 等数据库。

注意！使用 Docker Compose 启动完测试数据后，因为会自动导入项目的 SQL 脚本，所以可能需要等待 1-2 分钟。

### 1.1 MySQL

```Bash
docker compose up -d mysql
```

#### 1.2 Oracle

```Bash
docker compose up -d oracle
```

暂不支持 MacBook Apple Silicon，因为 Oracle 官方没有提供 Apple Silicon 版本的 Docker 镜像。

### 1.3 PostgreSQL

```Bash
docker compose up -d postgres
```

### 1.4 SQL Server

```Bash
docker compose up -d sqlserver
# 注意：启动完 sqlserver 后，需要手动再执行如下命令，因为 SQL Server 不支持初始化脚本
docker compose exec sqlserver bash /tmp/create_schema.sh
```

暂不支持 MacBook Apple Silicon，因为 SQL Server 官方没有提供 Apple Silicon 版本的 Docker 镜像。

### 1.5 DM 达梦

TODO 暂未支持

## 2. MySQL 转换其它数据库

### 2.1 实现原理

通过读取 MySQL 的 `sql/mysql/ruoyi-vue-pro.sql` 数据库文件，转换成 Oracle、PostgreSQL、SQL Server 等数据库的脚本。

### 2.2 使用方法

① 安装依赖库 `simple-ddl-parser`

```bash
pip install simple-ddl-parser
# pip3 install simple-ddl-parser
```

② 执行如下命令打印生成 postgres 的脚本内容，其他可选参数有：`oracle`、`sqlserver`

```Bash
python3 convertor.py postgres
# python3 convertor.py postgres > tmp.sql
```

程序将 SQL 脚本打印到终端，可以重定向到临时文件 `tmp.sql`。

确认无误后，可以利用 IDEA 进行格式化。当然，也可以直接导入到数据库中。