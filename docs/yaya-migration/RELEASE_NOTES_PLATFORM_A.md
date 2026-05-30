# Platform A Release Notes

Date: 2026-05-30
Branch: yaya/platform-a
Commit range: `74b73e4c77..c593ad6079`

## Summary

Platform A migrates Yaya onto the RuoYi-Vue-Pro backend/admin base while keeping
Yaya-specific business logic under `yudao-module-yaya`. RuoYi remains responsible
for admin, RBAC, member identity, and payment infrastructure; Yaya owns content,
practice, AI orchestration, recording, entitlement, member plan, compatibility,
and import workflows.

## Included Work

- PostgreSQL schema and seed SQL for Yaya content, AI tasks, practice app,
  entitlements, member orders, and admin menus.
- Java backend module `yudao-module-yaya` with admin APIs, app APIs, legacy
  compatibility APIs, AI task orchestration, recording, evaluation, entitlement,
  member plan, and RuoYi Pay integration.
- Python `yaya-ai-service` exposed as an internal HTTP service.
- RuoYi admin submodule pages for content topics, import batches, and member
  plans.
- Docker Compose baseline for PostgreSQL, Redis, Java backend, admin UI, and AI
  service.
- Legacy frontend compatibility work so current user-facing pages can call the
  RuoYi compatibility API.
- Full acceptance record in `docs/yaya-migration/ACCEPTANCE_FULL_MIGRATION.md`.

## Verified Commands

- Docker Maven `mvn -Dmaven.repo.local=/m2/repository test`: `BUILD SUCCESS`
  in `01:48 min`
- Docker Maven `mvn -Dmaven.repo.local=/m2/repository -DskipTests clean package`:
  `BUILD SUCCESS` in `34.893 s`
- `yaya-ai-service`: `./.venv/bin/python -m pytest -q`: 5 passed, 1 upstream
  deprecation warning
- Admin UI Docker Node/Corepack pnpm `lint`: exit 0 with one existing generated
  type warning
- Admin UI Docker Node/Corepack pnpm `build`: `Build successful`
- `docker compose build yudao-server yudao-ui-admin`: both local images built
- `docker compose up -d yudao-server yudao-ui-admin`: backend and admin UI
  recreated and started

## Running Ports

Current local `.env` overrides in `deploy/yaya-ruoyi/.env`:

- Admin UI: `http://127.0.0.1:18081`
- Backend API: `http://127.0.0.1:48081`
- AI service: `http://127.0.0.1:18080`
- PostgreSQL: `127.0.0.1:15432`
- Redis: `127.0.0.1:16379`

The default planned ports remain available through Compose defaults when no
local override is present: admin UI `8080`, backend `48080`, AI service `18080`.

## Deployment Compose Command

From `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/deploy/yaya-ruoyi`:

```bash
docker compose build
docker compose up -d
docker compose ps
```

For a clean local database, PostgreSQL initialization mounts the RuoYi baseline
SQL plus the Yaya SQL files under `sql/postgresql/yaya/`. For an existing local
database, replay the Yaya admin menu SQL idempotently if menu entries are
missing:

```bash
docker exec -i yaya-ruoyi-postgres-1 \
  psql -U root -d ruoyi-vue-pro \
  -f /docker-entrypoint-initdb.d/15-yaya-admin-menu.sql
```

## Runtime Checks

- `GET /admin-api/yaya/health`: `{"code":0,"msg":"","data":"ok"}`
- Admin nginx proxy to `/admin-api` and `/app-api`: verified through `18081`
- `POST /admin-api/yaya/import-batches/26Q1:preview`: topics `146`, questions
  `606`, errors `[]`
- `POST /admin-api/yaya/import-batches/26Q1:run`: topics `146`, questions
  `606`, errors `[]`
- `GET /internal/health`: `{"ok":true,"service":"yaya-ai-service"}`
- Admin browser flows: login, content list, import preview/run, reversible topic
  edit, topic publish, and member plan page
- App practice route: returns published imported topics
- Anonymous evaluation request: returns controlled login error

## Submodule Notes

The admin UI submodule pointer in this branch references local submodule commit
`08613cdb feat: add Yaya member plan admin view`, after:

- `20e654fb feat: add Yaya content admin pages`
- `499f2866 build: ignore admin docker build artifacts`

Push the submodule commits before or together with the platform branch so remote
checkouts can resolve the gitlink.

## Production Gates

- Configure real payment channels and a public Pay callback domain.
- Configure object storage and retention for recording files.
- Configure production AI provider keys, model selection, retry limits, and cost
  controls.
- Configure domain, TLS, proxy/CDN, and database backup policy.
- Move local secrets and `.env` values into the target deployment secret manager.
- Replace local-only port overrides with production values.
