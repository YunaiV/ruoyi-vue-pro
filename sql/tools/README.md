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

### 1.5 DM 达梦

① 下载达梦 Docker 镜像：<https://eco.dameng.com/download/> 地址，点击“Docker 镜像”选项，进行下载。

② 加载镜像文件，在镜像 tar 文件所在目录运行：

```Bash
docker load -i dm8_20240715_x86_rh6_rq_single.tar
```

③ 在项目 `sql/tools` 目录下运行：

```Bash
docker compose up -d dm8
# 注意：启动完 dm 后，需要手动再执行如下命令，因为 dm 不支持初始化脚本
docker compose exec dm8 bash -c '/opt/dmdbms/bin/disql SYSDBA/SYSDBA001 \`/tmp/schema.sql'
exit
```

### 1.6 KingbaseES 人大金仓

① 下载人大金仓 Docker 镜像：

> x86_64 版本: https://kingbase.oss-cn-beijing.aliyuncs.com/KESV8R3/V009R001C001B0025-安装包-docker/x86_64/kdb_x86_64_V009R001C001B0025.tar

> aarch64 版本：https://kingbase.oss-cn-beijing.aliyuncs.com/KESV8R3/V009R001C001B0025-安装包-docker/aarch64/kdb_aarch64_V009R001C001B0025.tar

② 加载镜像文件，在镜像 tar 文件所在目录运行：

```Bash
docker load -i x86_64/kdb_x86_64_V009R001C001B0025.tar
```

③ 在项目 `sql/tools` 目录下运行：

```Bash
docker compose up -d kingbase
# 注意：启动完 kingbase 后，需要手动再执行如下命令
docker compose exec kingbase bash -c 'ksql -U $DB_USER -d test -f /tmp/schema.sql'
```

### 1.7 华为 OpenGauss

```Bash
docker compose up -d opengauss
# 注意：启动完 opengauss 后，需要手动再执行如下命令
docker compose exec opengauss bash -c '/usr/local/opengauss/bin/gsql -U $GS_USERNAME -W $GS_PASSWORD -d postgres -f /tmp/schema.sql'
```

## 1.X 容器的销毁重建

开发测试过程中，有时候需要创建全新干净的数据库。由于测试数据 Docker 容器采用数据卷 Volume 挂载数据库实例的数据目录，因此销毁数据需要停止容器后，删除数据卷，然后再重新创建容器。

以 postgres 为例，操作如下：

```Bash
docker compose down postgres
docker volume rm ruoyi-vue-pro_postgres
```

## 2. MySQL 转换其它数据库

### 2.1 实现原理

通过读取 MySQL 的 `sql/mysql/ruoyi-vue-pro.sql` 数据库文件，转换成 Oracle、PostgreSQL、SQL Server、达梦、人大金仓 等数据库的脚本。

### 2.2 使用方法

① 安装依赖库 `simple-ddl-parser`

```bash
pip install simple-ddl-parser
# pip3 install simple-ddl-parser
```

② 执行如下命令打印生成 postgres 的脚本内容，其他可选参数有：`oracle`、`sqlserver`、`dm8`、`kingbase`：

```Bash
python3 convertor.py postgres
# python3 convertor.py postgres > tmp.sql
```

程序将 SQL 脚本打印到终端，可以重定向到临时文件 `tmp.sql`。

确认无误后，可以利用 IDEA 进行格式化。当然，也可以直接导入到数据库中。
