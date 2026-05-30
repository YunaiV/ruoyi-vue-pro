# Full Migration Acceptance

Date: 2026-05-30
Branch: yaya/platform-a

This acceptance run used the local Docker Compose port overrides in
`deploy/yaya-ruoyi/.env`: backend `48081`, admin UI `18081`, AI service
`18080`, PostgreSQL `15432`, Redis `16379`.

## Verified Commands

- Maven test:
  - Command: Docker Maven `mvn -Dmaven.repo.local=/m2/repository test`
  - Result: `BUILD SUCCESS`
  - Duration: `01:48 min`
  - Notable module totals:
    - `yudao-module-system`: 463 tests, 0 failures, 0 errors, 9 skipped
    - `yudao-module-wms`: 80 tests, 0 failures, 0 errors, 0 skipped
    - `yudao-module-pay`: 165 tests, 0 failures, 0 errors, 35 skipped
    - `yudao-module-yaya-biz`: 75 tests, 0 failures, 0 errors, 0 skipped
- Maven package:
  - Command: Docker Maven `mvn -Dmaven.repo.local=/m2/repository -DskipTests clean package`
  - Result: `BUILD SUCCESS`
  - Duration: `34.893 s`
  - Artifact: `yudao-server/target/yudao-server.jar`
- Python pytest:
  - Command: `./.venv/bin/python -m pytest -q`
  - Result: 5 passed, 1 upstream `StarletteDeprecationWarning`
- Admin UI lint:
  - Command: Docker Node 22 with Corepack pnpm, `pnpm lint`
  - Result: exit 0, with one existing generated-type warning in `src/types/auto-imports.d.ts`
- Admin UI build:
  - Command: Docker Node 22 with Corepack pnpm, `pnpm build`
  - Result: `Build successful. Please see dist-prod directory`
- Docker compose:
  - Command: `docker compose build yudao-server yudao-ui-admin`
  - Result: `yaya-ruoyi-server:local` and `yaya-ruoyi-admin:local` built
  - Command: `docker compose up -d yudao-server yudao-ui-admin`
  - Result: server and admin containers recreated and started
  - `docker compose ps` showed PostgreSQL, Redis, AI service, backend, and admin UI running

## Verified Runtime Routes

- `GET http://127.0.0.1:48081/admin-api/yaya/health`
  - Result: `{"code":0,"msg":"","data":"ok"}`
- `GET http://127.0.0.1:18081/admin-api/yaya/health`
  - Result: `{"code":0,"msg":"","data":"ok"}` through admin nginx proxy
- `GET http://127.0.0.1:18081/app-api/yaya/practice/topics?part=3&season=26Q1&q=...`
  - Result: total `1`, including topic `26q1-part3-...` with title `攒钱买的东西`
- `POST http://127.0.0.1:18081/admin-api/yaya/import-batches/26Q1:preview`
  - Result: season `26Q1`, topics `146`, questions `606`, errors `[]`
- `POST http://127.0.0.1:18081/admin-api/yaya/import-batches/26Q1:run`
  - Result: season `26Q1`, topics `146`, questions `606`, errors `[]`
- `GET http://127.0.0.1:18080/internal/health`
  - Result: `{"ok":true,"service":"yaya-ai-service"}`
- `GET http://127.0.0.1:18081/admin-api/yaya/member-plans`
  - Result: 3 seeded plans: `free-trial`, `monthly-pro`, `quarterly-pro`
- `PATCH http://127.0.0.1:18081/admin-api/yaya/topics/146/publish-status`
  - Result: archived and re-published successfully; final topic state is `published`
- `PATCH http://127.0.0.1:18081/admin-api/yaya/topics/146`
  - Result: reversible topic edit succeeded; category was restored to empty after verification
- `POST http://127.0.0.1:18081/app-api/yaya/evaluations` without login
  - Result: controlled `401` unauthenticated response

## Verified Browser Flows

- Admin login:
  - Playwright CLI opened `http://127.0.0.1:18081`
  - Default tenant `芋道源码`, user `admin`, password `admin123` logged in successfully
  - Landing URL: `http://127.0.0.1:18081/index`
- Yaya content list:
  - `http://127.0.0.1:18081/yaya/content-topics` rendered
  - Yaya menu expanded with `Content Topics`, `Import Batches`, and `Member Plans`
  - Topic list showed `146` rows and published imported topics
- Import preview/run:
  - `http://127.0.0.1:18081/yaya/import-batches` rendered
  - Browser preview showed season `26Q1`, topics `146`, questions `606`, and no errors
  - Browser run import confirmation completed and returned the same counts
- Topic edit and publish:
  - Browser opened the topic edit dialog and saved a reversible category edit
  - Admin API confirmed the edit, then restored the category field
  - Admin API publish-status route archived and re-published topic `146`; browser list showed the topic as `Published`
- App practice list:
  - Admin nginx proxy returned published app practice data through `/app-api/yaya/practice/topics`
- Evaluation request:
  - Anonymous request returned a controlled login error instead of an unhandled failure
- Membership plan page:
  - `http://127.0.0.1:18081/yaya/member-plans` rendered
  - Seeded plans displayed with price, duration, active status, and edit/disable actions

Browser console note: the only runtime console error observed during these pages was an upstream demo avatar asset returning 502 from `test.yudao.iocoder.cn`; Yaya API and page routes loaded successfully.

## Remaining Production Gates

- Real payment channel credentials and public callback domain.
- Object storage credentials and retention policy for recording files.
- Production AI provider keys, model choice, retry policy, and cost limits.
- Domain, TLS, CDN/proxy configuration, and backup policy.
- Secret management for Compose or the target deployment platform.
- Replace local-only port overrides with deployment-specific values.
