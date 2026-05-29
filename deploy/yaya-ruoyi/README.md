# Yaya RuoYi Docker Compose Baseline

This directory provides the Task 16 local deployment baseline for the migrated
Yaya platform.

## Services

| Service | Container port | Default host port |
|---|---:|---:|
| `postgres` | `5432` | `15432` |
| `redis` | `6379` | `16379` |
| `yudao-server` | `48080` | `48080` |
| `yudao-ui-admin` | `80` | `8080` |
| `yaya-ai-service` | `18080` | `18080` |

## Prerequisites

Build the backend jar before starting Compose:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform
docker run --rm -u "$(id -u):$(id -g)" \
  -v "$PWD":/workspace \
  -v "$HOME/.m2":/m2 \
  -w /workspace \
  maven:3.9.9-eclipse-temurin-17 \
  mvn -Dmaven.repo.local=/m2/repository -DskipTests clean package
```

The server image expects:

```text
yudao-server/target/yudao-server.jar
```

## Start

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform/deploy/yaya-ruoyi
cp .env.example .env
docker compose up -d --build
docker compose ps
```

If host ports `48080` or `8080` are already occupied, override only the host
ports in `.env`, for example:

```text
YUDAO_SERVER_PORT=48081
YUDAO_UI_ADMIN_PORT=18081
```

Container-to-container URLs stay unchanged.

## Smoke Checks

```bash
curl -fsS http://127.0.0.1:48080/admin-api/yaya/health
curl -fsS http://127.0.0.1:18080/internal/health
```

Expected:

```json
{"code":0,"msg":"","data":"ok"}
{"ok":true,"service":"yaya-ai-service"}
```

## Database Initialization

PostgreSQL initializes from:

```text
sql/postgresql/ruoyi-vue-pro.sql
sql/postgresql/quartz.sql
sql/postgresql/yaya/*.sql
```

The SQL files run only when the `postgres-data` volume is empty. To replay from
scratch, remove the Compose volume intentionally:

```bash
docker compose down -v
```
