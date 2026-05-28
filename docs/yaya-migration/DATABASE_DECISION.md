# Database Decision

Decision: PostgreSQL for RuoYi Phase 0.

Rationale: The upstream PostgreSQL schema imported, the backend booted after adding the PostgreSQL JDBC driver to the server runtime, and this keeps Yaya platform data aligned with the existing PostgreSQL/pgvector direction.

## RuoYi database

- Engine: PostgreSQL
- Schema source: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/sql/postgresql/ruoyi-vue-pro.sql`
- Quartz schema source: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/sql/postgresql/quartz.sql`
- Test database: Docker `postgres:14.2`, container `yaya-ruoyi-pg-phase0`, host port `127.0.0.1:55432`

Import command:

```bash
docker run --name yaya-ruoyi-pg-phase0 \
  -e POSTGRES_USER=root \
  -e POSTGRES_PASSWORD=123456 \
  -e POSTGRES_DB=ruoyi-vue-pro \
  -p 127.0.0.1:55432:5432 \
  -v /Volumes/LamarHD/Yaya/yaya-ruoyi-platform/sql/postgresql/quartz.sql:/docker-entrypoint-initdb.d/01_quartz.sql:ro \
  -v /Volumes/LamarHD/Yaya/yaya-ruoyi-platform/sql/postgresql/ruoyi-vue-pro.sql:/docker-entrypoint-initdb.d/02_ruoyi-vue-pro.sql:ro \
  -d postgres:14.2
```

Import verification:

```bash
docker exec yaya-ruoyi-pg-phase0 psql -U root -d ruoyi-vue-pro -Atc "select count(*) from information_schema.tables where table_schema='public';"
```

```text
60
```

Backend boot command:

```bash
docker run --name yaya-ruoyi-server-phase0 \
  -p 127.0.0.1:48080:48080 \
  -v /Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yudao-server/target/yudao-server.jar:/app/yudao-server.jar:ro \
  -e TZ=Asia/Shanghai \
  maven:3.9.9-eclipse-temurin-17 \
  java -jar /app/yudao-server.jar \
    --spring.profiles.active=local \
    --spring.datasource.dynamic.datasource.master.url=jdbc:postgresql://host.docker.internal:55432/ruoyi-vue-pro \
    --spring.datasource.dynamic.datasource.master.username=root \
    --spring.datasource.dynamic.datasource.master.password=123456 \
    --spring.datasource.dynamic.datasource.master.driver-class-name=org.postgresql.Driver \
    --spring.datasource.dynamic.datasource.slave.lazy=true \
    --spring.datasource.dynamic.datasource.slave.url=jdbc:postgresql://host.docker.internal:55432/ruoyi-vue-pro \
    --spring.datasource.dynamic.datasource.slave.username=root \
    --spring.datasource.dynamic.datasource.slave.password=123456 \
    --spring.datasource.dynamic.datasource.slave.driver-class-name=org.postgresql.Driver \
    --spring.data.redis.host=host.docker.internal \
    --spring.data.redis.port=56379
```

Backend boot result:

```text
Tomcat started on port 48080 (http) with context path '/'
Started YudaoServerApplication in 9.592 seconds
项目启动成功！
```

HTTP verification:

```bash
curl -i -sS http://127.0.0.1:48080/admin-api/system/auth/get-permission-info
```

```text
HTTP/1.1 200
{"code":401,"msg":"账号未登录","data":null}
```

## Conversion path notes

The plan's converter command did not match the current upstream script interface:

```bash
python3 convertor.py postgres > /tmp/yaya-ruoyi-postgres.sql
```

```text
convertor.py: error: the following arguments are required: path
```

The documented script form also failed in the current local parser dependency path:

```bash
/tmp/yaya-ruoyi-sql-venv/bin/python convertor.py postgres ../mysql/ruoyi-vue-pro.sql > /tmp/yaya-ruoyi-postgres.sql
```

```text
TypeError: string indices must be integers, not 'str'
```

Decision evidence therefore uses the upstream committed PostgreSQL scripts under `sql/postgresql/`, which imported successfully.

## Runtime dependency note

RuoYi's MyBatis starter declares `org.postgresql:postgresql` as an optional dependency, so the initial fat jar did not include `org.postgresql.Driver`. `yudao-server/pom.xml` now declares `org.postgresql:postgresql` directly so PostgreSQL runtime boot is reproducible.

## Yaya AI/vector database

- Engine: PostgreSQL
- Required extension: pgvector
- Source models: `/Volumes/LamarHD/Yaya/YAYA-pages-for-Windows-version-dev/apps/api/yaya_api/db/models.py`
- Evidence: the current Yaya models import `pgvector.sqlalchemy.Vector` and define `Vector(1536)` columns for memory/content embeddings.

## Revisit trigger

Revisit this decision before Phase 3 payment/member data becomes production-like.
