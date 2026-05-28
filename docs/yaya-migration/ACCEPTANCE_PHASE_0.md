# Phase 0 Acceptance

Date: 2026-05-28

## Scope

This acceptance records the first executable RuoYi foundation for Yaya Platform A.

Repository: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform`
Branch: `yaya/platform-a`
RuoYi upstream branch: `master-jdk17`
RuoYi upstream pin: `74b73e4c777b80bab2cdffcec3079886f0a2e98f`
Implementation head before this acceptance document: `c6e9d5ef0b3f`

## Completed Foundation

- RuoYi upstream is pinned in `docs/yaya-migration/UPSTREAM.md`.
- Baseline build result is recorded in `docs/yaya-migration/BASELINE.md`.
- Yaya module boundary is recorded in `docs/yaya-migration/MODULE_BOUNDARY.md`.
- AI service boundary is recorded in `docs/yaya-migration/AI_SERVICE_CONTRACT.md`.
- Database decision is recorded in `docs/yaya-migration/DATABASE_DECISION.md`.
- `yudao-module-yaya` is wired into the Maven reactor and `yudao-server`.
- `GET /admin-api/yaya/health` is available as the first admin-side Yaya endpoint.

## Verification Evidence

### Targeted Unit Test

Command:

```bash
docker run --rm -u "$(id -u):$(id -g)" \
  -v "$PWD":/workspace \
  -v "$HOME/.m2/repository":/m2/repository \
  -e HOME=/tmp/maven-home \
  -w /workspace \
  maven:3.9.9-eclipse-temurin-17 \
  mvn -Dmaven.repo.local=/m2/repository \
    -pl yudao-module-yaya/yudao-module-yaya-biz -am \
    -Dtest=YayaHealthControllerTest \
    -Dsurefire.failIfNoSpecifiedTests=false test
```

Result:

```text
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 4.058 s
```

### Backend Package

Command:

```bash
docker run --rm -u "$(id -u):$(id -g)" \
  -v "$PWD":/workspace \
  -v "$HOME/.m2/repository":/m2/repository \
  -e HOME=/tmp/maven-home \
  -w /workspace \
  maven:3.9.9-eclipse-temurin-17 \
  mvn -Dmaven.repo.local=/m2/repository -DskipTests clean package
```

Result:

```text
yudao-module-yaya-biz .............................. SUCCESS
yudao-server ....................................... SUCCESS
BUILD SUCCESS
Total time: 28.111 s
Artifact: yudao-server/target/yudao-server.jar
```

### Database

Runtime database: PostgreSQL 14.2 container `yaya-ruoyi-pg-phase0`
Runtime Redis: Redis 7 container `yaya-ruoyi-redis-phase0`

Commands and results:

```bash
docker exec yaya-ruoyi-pg-phase0 psql -U root -d ruoyi-vue-pro -Atc "select count(*) from information_schema.tables where table_schema='public';"
60

docker exec yaya-ruoyi-pg-phase0 psql -U root -d ruoyi-vue-pro -Atc "select count(*) from system_users;"
20
```

### Backend Runtime

Container: `yaya-ruoyi-server-phase0`
Port: `127.0.0.1:48080`

Startup evidence:

```text
Tomcat started on port 48080 (http) with context path '/'
Started YudaoServerApplication in 8.99 seconds
```

Health endpoint:

```bash
curl -i -sS http://127.0.0.1:48080/admin-api/yaya/health
```

Result:

```text
HTTP/1.1 200
{"code":0,"msg":"","data":"ok"}
```

The endpoint is annotated with both `@PermitAll` and `@TenantIgnore` because the first runtime check returned a missing tenant identifier error before the tenant ignore fix.

## Admin Frontend Status

The selected RuoYi backend repository does not include a runnable admin UI checkout under `yudao-ui`.

Command:

```bash
find yudao-ui -maxdepth 4 -name package.json -print
```

Result: no output.

Only README links are present under `yudao-ui`. Admin frontend login smoke is therefore not executable from this repository alone. The next implementation step should clone, vendor, or submodule the official `yudao-ui-admin-vue3` frontend before browser login testing.

## Current Yaya Source Repo Status

Command:

```bash
git -C /Volumes/LamarHD/Yaya/YAYA-pages-for-Windows-version-dev status --short --branch
```

Result:

```text
## main...origin/main
 M .codex/config.toml
?? .playwright-cli/
?? docs/superpowers/plans/2026-05-28-yaya-ruoyi-platform-a.md
```

The `.codex/config.toml` and `.playwright-cli/` entries are unrelated local changes and were not modified by this foundation work.

## Acceptance Result

Phase 0 backend foundation is accepted for the next migration slice:

- RuoYi backend builds in Docker with JDK 17.
- PostgreSQL and Redis runtime dependencies are proven.
- The backend starts from the packaged jar.
- The first Yaya module endpoint responds successfully through the RuoYi admin API prefix.

Open item:

- Admin UI checkout is not present in the selected backend repository and must be added before frontend login and menu-placeholder testing.
