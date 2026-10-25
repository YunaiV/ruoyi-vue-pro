## 0. 友情提示

在 `sql/tools` 目录下，我们提供一些数据库相关的工具，包括测试数据库的快速启动、MySQL 转换其它数据库等等。

注意！所有的操作，必须在 `sql/tools` 目录下执行。

## 1. 测试数据库的快速启动

基于 Docker Compose，快速启动 MySQL、Oracle、PostgreSQL、SQL Server、OceanBase、TiDB 等数据库。

注意！使用 Docker Compose 启动完测试数据后，因为会自动导入项目的 SQL 脚本，所以可能需要等待 1-2 分钟。

### 1.1 MySQL

```Bash
docker compose up -d mysql
```

#### 1.2 Oracle

```Bash
## x86 版本
docker compose up -d oracle

## MacBook Apple Silicon
docker compose up -d oracle_m1
```

> 注意：如果使用 MacBook Apple Silicon 版本，它的 ORACLE_SID 不是 XE，而是 FREE！！！

> 注意：项目 Oracle 脚本使用 `varchar2(n char)` 保持和 MySQL `varchar(n)` 一致的字符长度语义，索引、约束、序列名不超过 30 字节（Oracle 12.2 以下或 `COMPATIBLE` < 12.2 时的标识符长度上限）。使用 `sqlplus` 手动导入时，需要先执行 `SET DEFINE OFF`，避免数据里的 `&` 被当作变量替换；建议同时执行 `WHENEVER SQLERROR EXIT SQL.SQLCODE`，遇到错误立即停止。

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
docker compose exec dm8 bash -c 'printf "SET DEFINE OFF;\n" > /tmp/schema-with-define-off.sql && cat /tmp/schema.sql >> /tmp/schema-with-define-off.sql && /opt/dmdbms/bin/disql SYSDBA/SYSDBA001 \`/tmp/schema-with-define-off.sql'
exit
```

> 注意：项目 DM8 脚本使用 `varchar(n char)` 保持和 MySQL `varchar(n)` 一致的字符长度语义，建议初始化 DM8 时使用 `PAGE_SIZE=16`、`UNICODE_FLAG=1`。`sql/tools/docker-compose.yaml` 已按该配置提供示例。使用 `disql` 导入时需要先执行 `SET DEFINE OFF;`，避免数据里的 `&` 被当作变量替换。

### 1.6 KingbaseES 人大金仓

① 下载人大金仓 Docker 镜像：

* [x86_64 版本](https://kingbase.oss-cn-beijing.aliyuncs.com/KESV8R3/V009R001C001B0025-安装包-docker/x86_64/kdb_x86_64_V009R001C001B0025.tar) 【Windows 选择这个】
* [aarch64 版本](https://kingbase.oss-cn-beijing.aliyuncs.com/KESV8R3/V009R001C001B0025-安装包-docker/aarch64/kdb_aarch64_V009R001C001B0025.tar) 【MacBook Apple Silicon 选择这个】

② 加载镜像文件，在镜像 tar 文件所在目录运行：

```Bash
docker load -i kdb_x86_64_V009R001C001B0025.tar
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

### 1.8 HighGo 瀚高数据库

① 下载瀚高官方 Docker 镜像，并加载镜像文件。加载后，将镜像打成本地标签：

```Bash
docker load -i <highgo-image>.tar
docker tag <image>:<tag> highgo:local
```

② 在项目 `sql/tools` 目录下运行：

```Bash
docker compose up -d highgo
```

> 注意：`highgo` 服务属于 `highgo` profile，执行 `docker compose up -d` 时不会启动，需要像上面一样指定服务名。不同瀚高镜像的数据目录可能不同，如果容器无法启动，请按镜像实际 `PGDATA` 修改 `docker-compose.yaml` 中的 `highgo` 数据卷挂载目录。

③ 初始化数据库：使用 `postgres:14.2` 镜像中的 `psql` 客户端，创建 `ruoyi-vue-pro` 数据库，并依次导入 `sql/highgo/quartz.sql`、`sql/highgo/ruoyi-vue-pro.sql`：

```Bash
HIGHGO_PASSWORD='数据库密码' docker compose run --rm highgo-init
```

> 注意：默认使用瀚高的 `highgo` 账号连接 `highgo` 数据库执行建库，可以通过 `HIGHGO_USER`、`HIGHGO_MAINTENANCE_DB` 环境变量修改；密码以镜像或安装时的实际设置为准，脚本不会修改数据库的认证配置。初始化只允许创建全新的数据库：数据库已存在（包括空库）或重复执行时，脚本拒绝导入并返回非零状态；导入失败时保留数据库便于排查。该入口只适用于 PostgreSQL 客户端支持的认证方式（例如 md5、scram-sha-256），数据库开启 SM3 等专有认证时，请使用瀚高客户端手动执行上述两个 SQL 文件。

④ 项目可以使用 PostgreSQL JDBC 驱动（`jdbc:postgresql://127.0.0.1:5866/ruoyi-vue-pro`），也可以使用瀚高 JDBC 驱动（`jdbc:highgo://127.0.0.1:5866/ruoyi-vue-pro`），两者都按 PostgreSQL 处理序列主键、Quartz 等差异，`url` 需要与 `driver-class-name` 配对。驱动需要引入到 `yudao-server/pom.xml` 的 `<dependencies>` 中：`yudao-spring-boot-starter-mybatis` 中的 `org.postgresql:postgresql` 是 `optional` 依赖，只在 `yudao-dependencies` 中声明版本也不会引入 jar 包。详见 [《国产信创数据库》](https://doc.iocoder.cn/xinchuang-db/#_4-highgo-瀚高数据库) 文档。

### 1.9 OceanBase

使用 OceanBase 社区版的 MySQL 模式，支持 x86_64 和 MacBook Apple Silicon。Docker 建议至少分配 8 GB 内存；同时运行 MySQL、Elasticsearch 等服务时，建议分配 12 GB 或更多，确保 OceanBase 启动检查所需的 6 GB 可用内存。首次启动需要等待几分钟。

① 启动数据库，等待租户连接健康检查通过：

```Bash
docker compose up -d --wait --wait-timeout 300 oceanbase
```

② 手动导入项目和 Quartz SQL（脚本会重建同名表，仅用于初始化测试数据库）：

```Bash
docker compose exec oceanbase bash /tmp/create_schema.sh
```

③ 项目继续使用 MySQL JDBC 驱动，将数据源配置中的地址、账号和密码修改为：

```yaml
url: jdbc:mysql://127.0.0.1:2881/ruoyi-vue-pro?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true&rewriteBatchedStatements=true
username: root@test
password: 123456
```

> 注意：`root@test` 连接的是 MySQL 模式的 `test` 租户。配置了多个数据源时，需要同步修改对应配置。数据和部署配置分别保存在 `oceanbase`、`oceanbase_config` 数据卷中，重启不需要重新导入 SQL。镜像说明见 [OceanBase 官方文档](https://github.com/oceanbase/docker-images/blob/main/oceanbase-ce/README_CN.md)。

### 1.10 TiDB

使用 TiDB v8.5.8 官方镜像，启动 PD、TiKV、TiDB 各 1 个节点的测试集群，支持 x86_64 和 MacBook Apple Silicon。相关服务属于 `tidb` profile，执行 `docker compose up -d` 时不会启动。

① 启动数据库，等待 TiDB 健康检查通过：

```Bash
docker compose up -d --wait --wait-timeout 300 tidb
```

② 初始化数据库：设置 `root` 密码为 `123456`，创建 `ruoyi-vue-pro` 数据库，并依次导入 `sql/mysql/ruoyi-vue-pro.sql`、`sql/mysql/quartz.sql`：

```Bash
docker compose run --rm tidb-init
```

> 注意：TiDB 直接复用 MySQL 脚本，不需要单独的 TiDB 脚本。初始化只允许创建全新的数据库：数据库已存在（包括空库）或重复执行时，脚本拒绝导入并返回非零状态，避免重建已有的同名表；导入失败时保留数据库便于排查，重新初始化需要删除数据卷。`TIDB_PASSWORD` 只允许字母、数字和 `_ . @ # % + = -` 字符。

③ 项目继续使用 MySQL JDBC 驱动，将数据源配置中的地址、账号和密码修改为：

```yaml
url: jdbc:mysql://127.0.0.1:4000/ruoyi-vue-pro?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true&rewriteBatchedStatements=true
username: root
password: 123456
```

> 注意：端口只绑定本机 `127.0.0.1:4000`。数据保存在 `tidb_pd`、`tidb_tikv` 数据卷中，重启不需要重新导入 SQL。如需重建，执行 `docker compose down tidb tidb-tikv tidb-pd` 后，删除 `ruoyi-vue-pro_tidb_pd`、`ruoyi-vue-pro_tidb_tikv` 数据卷。该环境仅用于开发测试，生产部署见 [TiDB 官方文档](https://docs.pingcap.com/zh/tidb/v8.5/production-deployment-using-tiup/)。

## 1.X 容器的销毁重建

开发测试过程中，有时候需要创建全新干净的数据库。由于测试数据 Docker 容器采用数据卷 Volume 挂载数据库实例的数据目录，因此销毁数据需要停止容器后，删除数据卷，然后再重新创建容器。

以 postgres 为例，操作如下：

```Bash
docker compose down postgres
docker volume rm ruoyi-vue-pro_postgres
```

## 2. MySQL 转换其它数据库

项目提供了 `sql/tools/convertor.py` 脚本，支持将 MySQL 转换为 Oracle、PostgreSQL、SQL Server、达梦、人大金仓、OpenGauss、瀚高、OceanBase 等数据库的脚本。

### 2.1 实现原理

通过读取 MySQL 的 `sql/mysql/ruoyi-vue-pro.sql` 数据库文件，转换成对应的数据库脚本。

### 2.2 使用方法

① 安装依赖库 `simple-ddl-parser`

```bash
pip install simple-ddl-parser
# pip3 install simple-ddl-parser
```

② 在 `sql/tools/` 目录下，执行如下命令打印生成 postgres 的脚本内容，其他可选参数有：`oracle`、`sqlserver`、`dm8`、`kingbase`、`opengauss`、`highgo`、`oceanbase`：

```Bash
python3 convertor.py postgres
# python3 convertor.py postgres > tmp.sql
# python3 convertor.py highgo ../mysql/ruoyi-vue-pro.sql > ../highgo/ruoyi-vue-pro.sql
```

程序将 SQL 脚本打印到终端，可以重定向到临时文件 `tmp.sql`。

确认无误后，可以利用 IDEA 进行格式化。当然，也可以直接导入到数据库中。

### 2.3 OceanBase MySQL 模式

OceanBase MySQL 模式保留原始 MySQL SQL，不经过其它数据库的字段类型和索引转换。更新 MySQL 脚本后，可通过以下命令同步生成：

```Bash
python3 convertor.py oceanbase ../mysql/ruoyi-vue-pro.sql > ../oceanbase/ruoyi-vue-pro.sql
python3 convertor.py oceanbase ../mysql/quartz.sql > ../oceanbase/quartz.sql
```
