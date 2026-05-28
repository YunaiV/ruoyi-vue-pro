# Yaya RuoYi Execution Status

Date: 2026-05-28
Branch: yaya/platform-a

## Completed

- Phase 0 backend foundation accepted.
- RuoYi upstream pinned.
- PostgreSQL runtime selected and verified.
- `yudao-module-yaya` skeleton compiles into `yudao-server`.
- `GET /admin-api/yaya/health` returns `{"code":0,"msg":"","data":"ok"}`.
- Full migration execution plan is committed in `docs/superpowers/plans/2026-05-28-yaya-ruoyi-full-migration.md`.
- Official `yudao-ui-admin-vue3` is pinned as a Git submodule.
- Admin UI dependency install and `build:local` completed successfully.
- Admin UI login smoke passed against the local RuoYi backend.
- Yaya PostgreSQL schema baseline is applied locally with 17 `yaya_%` tables.
- Yaya content domain DO, mapper, service, and service tests are implemented.
- Yaya admin content API controllers, request/response VOs, and import endpoint placeholders are implemented.
- `PATCH /admin-api/yaya/topics/{id}` preserves omitted fields for partial admin edits.
- Yaya content import pipeline is implemented and verified with the legacy 26Q1 snapshot.
- The import pipeline validates required snapshot files, manifest counts, and question-topic references before writing database state.
- Legacy content snapshot exported to `docs/yaya-migration/content-import-snapshots/20260528-192813`.
- Yaya admin content topic and import batch pages are implemented in the RuoYi Vue3 admin UI.
- Yaya dynamic menu and permission seed data is implemented in `sql/postgresql/yaya/20260528_yaya_admin_menu.sql`.
- Task 7 quality review fixes are applied: topic detail responses preserve question `legacyUuid`, and Yaya role-menu seed idempotency is scoped to `tenant_id = 1`.
- Yaya app practice APIs are implemented under `/app-api/yaya/practice/*`.
- App practice persistence tables are implemented in `sql/postgresql/yaya/20260528_yaya_practice_app.sql`: `yaya_favorite`, `yaya_user_topic_state`, and `yaya_practice_attempt`.
- App practice topic state updates now use atomic `attempt_count = attempt_count + 1` updates for repeated member attempts.
- Standalone `yaya-ai-service` FastAPI package is implemented with internal health, signed internal auth, and evaluation task accept/get endpoints.
- Legacy `yaya_ai` source package is copied into `yaya-ai-service`; provider SDK dependencies are optional under the `ai` extra.

## Active Phase

Phase 6 - Java AI client and task orchestration.

## Known Issues

- `corepack` is not available on this machine; local frontend commands used `npx --yes pnpm@10.25.0`.
- Port `8080` is occupied by another local Docker service, so the admin UI dev server is running on `127.0.0.1:18081`.
- `pnpm ts:check` fails in the upstream admin UI checkout with existing type debt. `build:local` and browser login smoke pass.
- The logged-in dashboard has one non-blocking external demo avatar image error from `test.yudao.iocoder.cn` returning HTTP 502.
- Vector and embedding tables are deferred until the local PostgreSQL runtime includes pgvector.
- The local legacy Yaya PostgreSQL database is on `127.0.0.1:5433`, not the example `5432` in the original plan.
- Task 7 uses RuoYi dynamic `system_menu` seed data instead of static router module entries, because the admin shell builds business routes from backend menus.

## Runtime Snapshot

- Backend: `yaya-ruoyi-server-phase0` on `127.0.0.1:48080`
- Admin UI: `yaya-ruoyi-admin-ui` screen session on `127.0.0.1:18081`
- PostgreSQL: `yaya-ruoyi-pg-phase0` on `127.0.0.1:55432`
- Redis: `yaya-ruoyi-redis-phase0` on `127.0.0.1:56379`
- Import snapshot root mounted in backend container at `/app/content-import-snapshots`.

## Latest Verification

- Python exporter syntax check: `/tmp/yaya-migration-venv/bin/python -m py_compile tools/yaya-migration/export_legacy_content.py` returned success.
- Frontend changed-file formatting and lint checks passed for Yaya admin UI files.
- Admin UI `npx --yes pnpm@10.25.0 build:local` returned success after Task 7 frontend changes.
- `YayaContentServiceImplTest`, `YayaImportServiceImplTest`, `YayaTopicControllerTest`, `YayaImportControllerTest`: 19 tests, 0 failures, 0 errors after the `legacyUuid` response fix.
- Backend package: `mvn -Dmaven.repo.local=/m2/repository -DskipTests clean package` returned `BUILD SUCCESS` after Task 7 backend changes.
- Yaya admin menu SQL applied twice locally with `menus=9`, tenant-1 `role_menu=9`, all-tenant `role_menu=9`, and `system_menu_seq.last_value=9015`; repeated execution did not duplicate role-menu rows.
- Authenticated permission smoke confirmed Yaya menu entries and permissions: `yaya:topic:query`, `yaya:topic:create`, `yaya:topic:update`, `yaya:topic:publish`, `yaya:import:preview`, `yaya:import:run`.
- Authenticated topic detail smoke confirmed the first returned question includes legacy UUID `96750dd4-d00b-4179-9662-b1dac424b49d`.
- Browser smoke on `127.0.0.1:18081` confirmed:
  - `/yaya/content-topics` renders filters, table columns including `Updated Time`, and the view dialog with a `Questions` tab.
  - `/yaya/import-batches` preview renders `26Q1`, `146` topics, and `606` questions.
  - `Run Import` completes after confirmation and returns the same 26Q1 summary.
- Backend responded after import pipeline restart; `/admin-api/actuator/health` is auth-gated and returned `{"code":401,"msg":"账号未登录","data":null}`.
- Authenticated import smoke with `admin/admin123` and `tenant-id: 1`:
  - `POST /admin-api/yaya/import-batches/26Q1:preview` returned `{"seasonKey":"26Q1","topics":146,"questions":606,"errors":[]}`.
  - `POST /admin-api/yaya/import-batches/26Q1:run` returned `{"seasonKey":"26Q1","topics":146,"questions":606,"errors":[]}`.
  - PostgreSQL counts after repeated run: `yaya_practice_topic=146`, `yaya_practice_question=606`, active `yaya_practice_question=606`, `yaya_import_batch=5`.
  - Latest import batch summary: `id=5`, `season_key=26Q1`, `status=completed`, `writtenQuestions=606`, `updatedTopics=146`.
- Task 8 focused backend tests: `YayaAppPracticeControllerTest`, `YayaPracticeServiceImplTest`: 7 tests, 0 failures, 0 errors.
- Task 8 Yaya backend slice: `YayaContentServiceImplTest`, `YayaImportServiceImplTest`, `YayaTopicControllerTest`, `YayaImportControllerTest`, `YayaAppPracticeControllerTest`, `YayaPracticeServiceImplTest`: 26 tests, 0 failures, 0 errors.
- Task 8 SQL migration reapplied idempotently against local PostgreSQL.
- Task 8 backend package: `mvn -Dmaven.repo.local=/m2/repository -DskipTests clean package` returned `BUILD SUCCESS`.
- Anonymous app practice smoke without `tenant-id`:
  - `GET /app-api/yaya/practice/topics?part=3&season=26Q1&pageNo=1&pageSize=1` returned `total=53`, first topic `id=146`, `favorite=false`, `practiced=false`.
  - `GET /app-api/yaya/practice/topics/146` returned `questionCount=6`, `favorite=false`, `practiced=false`.
- Member app practice smoke with `Authorization: Bearer test10003` and `tenant-id: 1`:
  - `POST /app-api/yaya/practice/favorites` returned `favoriteId=3`.
  - Two `POST /app-api/yaya/practice/attempts` calls returned `attemptId=3` and `attemptId=4`.
  - Authenticated detail returned `favorite=true`, `practiced=true`, `questionCount=6`.
  - PostgreSQL counts for `member_user_id=10003`, `topic_id=146`: `favorite=1`, `attempt=2`, `state=1`, `attempt_count=2`.
- Task 9 Python service install/test: `. .venv/bin/activate && pip install -e . && pytest -q` returned `5 passed, 1 warning`.
- Task 9 Python compile check: `python -m compileall -q app yaya_ai` returned success.
- Task 9 service smoke:
  - `GET /internal/health` on `127.0.0.1:18080` returned `{"ok":true,"service":"yaya-ai-service"}`.
  - Protected `POST /internal/evaluations` returned HTTP `400` without `X-Yaya-Request-Id` and HTTP `401` with an invalid key.
  - Valid `POST /internal/evaluations` returned `{"task_id":"smoke-task-1","status":"PENDING","accepted":true}`.
  - Retrying the same `task_id` with the same payload returned the existing accepted task.
  - `GET /internal/evaluations/smoke-task-1` returned `status=PENDING`, `progress.stage=accepted`, `result=null`, and `error=null`.
- Python AI service runtime is running in screen session `yaya-ai-service` on `127.0.0.1:18080`.
