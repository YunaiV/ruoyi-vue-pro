# Yaya RuoYi Full Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move Yaya onto a RuoYi-Vue-Pro full-version backend/admin platform while preserving the current Yaya AI capability as an independent Python service.

**Architecture:** RuoYi owns admin, RBAC, member identity, payment, product/order operations, and the Java business API. All Yaya business tables and orchestration live under `yudao-module-yaya`; they reference RuoYi member/pay/system APIs instead of modifying those modules directly. The current Python AI package is extracted behind a signed internal HTTP service so Java orchestrates tasks and Python executes model-heavy work.

**Tech Stack:** RuoYi-Vue-Pro `master-jdk17`, Spring Boot, MyBatis Plus, PostgreSQL, Redis, Vue 3 + Element Plus admin UI, FastAPI/Python for `yaya-ai-service`, Docker Compose for local deployment.

---

## Current Baseline

- New platform repo: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform`
- Current branch: `yaya/platform-a`
- Upstream RuoYi pin: `74b73e4c777b80bab2cdffcec3079886f0a2e98f`
- Current Yaya source repo: `/Volumes/LamarHD/Yaya/YAYA-pages-for-Windows-version-dev`
- Phase 0 acceptance doc: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/docs/yaya-migration/ACCEPTANCE_PHASE_0.md`
- Current validated endpoint: `GET http://127.0.0.1:48080/admin-api/yaya/health`
- Current backend containers:
  - `yaya-ruoyi-pg-phase0` on `127.0.0.1:55432`
  - `yaya-ruoyi-redis-phase0` on `127.0.0.1:56379`
  - `yaya-ruoyi-server-phase0` on `127.0.0.1:48080`

## Execution Rules

- Work on `yaya/platform-a` until an explicit release branch is created.
- Keep every phase independently buildable and commit after every green task.
- Do not move Yaya-specific tables into `system`, `member`, `pay`, or `mall`.
- Use `member_user.id` as the long-term Yaya user reference.
- Use RuoYi `pay` for real payments and Yaya entitlement tables for product access decisions.
- Java talks to Python AI only through HTTP with `X-Yaya-Internal-Key` and `X-Yaya-Request-Id`.
- Preserve legacy API behavior through an explicit compatibility layer before retiring the old FastAPI frontend routes.
- Do not modify unrelated dirty files in `/Volumes/LamarHD/Yaya/YAYA-pages-for-Windows-version-dev`.

## Target Runtime Layout

```text
/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/
  yudao-server/
  yudao-module-yaya/
    yudao-module-yaya-api/
    yudao-module-yaya-biz/
  yudao-ui-admin-vue3/
  yaya-ai-service/
  tools/yaya-migration/
  deploy/yaya-ruoyi/
  docs/yaya-migration/
```

## Data Ownership Map

| Legacy model or route | Target owner | Target table or endpoint group |
|---|---|---|
| `ContentSeason`, `ContentModule` | `yudao-module-yaya` | `yaya_content_season`, `yaya_content_module` |
| `ImportBatch`, `SourceDocument`, `SourceTopicSnapshot` | `yudao-module-yaya` | `yaya_import_batch`, `yaya_source_document`, `yaya_source_topic_snapshot` |
| `PracticeTopic`, `PracticeQuestion`, `TopicRelation`, `Tag` | `yudao-module-yaya` | `yaya_practice_topic`, `yaya_practice_question`, `yaya_topic_relation`, `yaya_tag`, `yaya_topic_tag` |
| `User`, `UserCredential`, `AuthSession` | RuoYi member/system | `member_user`, RuoYi auth/session mechanisms, plus `yaya_legacy_user_map` |
| `UserProfile`, pet, activity, inventory, letters | `yudao-module-yaya` | `yaya_user_profile`, `yaya_user_activity`, `yaya_user_pet_state`, `yaya_user_inventory_item`, `yaya_ai_companion_letter` |
| `Favorite`, `UserTopicState`, `PracticeAttempt` | `yudao-module-yaya` | `yaya_favorite`, `yaya_user_topic_state`, `yaya_practice_attempt` |
| `Recording`, `Transcript`, `Evaluation` | Java orchestration + Python AI | `yaya_recording`, `yaya_transcript`, `yaya_evaluation`, `yaya_ai_task` |
| `UserMemoryItem`, chunks, embeddings | Java metadata + Python/vector service | `yaya_memory_item`, `yaya_memory_review_state`, `yaya_chunk`, `yaya_content_embedding` |
| `/api/admin/*` | RuoYi admin API | `/admin-api/yaya/*` |
| `/api/practice/*`, `/api/topics*` | RuoYi app API + compatibility | `/app-api/yaya/practice/*` and `/admin-api/yaya/compat/*` where needed |
| `/api/evaluations*`, `/api/recordings` | RuoYi app API + AI service | `/app-api/yaya/recordings`, `/app-api/yaya/evaluations` |
| membership/payment | RuoYi member/pay + Yaya entitlement | `yaya_member_plan`, `yaya_member_entitlement`, `pay_order` |

---

## Task 1: Freeze Phase 0 And Prepare Execution Branch

**Files:**
- Read: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/docs/yaya-migration/ACCEPTANCE_PHASE_0.md`
- Read: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/docs/yaya-migration/MODULE_BOUNDARY.md`
- Read: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/docs/yaya-migration/AI_SERVICE_CONTRACT.md`
- Read: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/docs/yaya-migration/DATABASE_DECISION.md`
- Modify: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/docs/yaya-migration/EXECUTION_STATUS.md`

- [ ] **Step 1: Confirm platform repo is clean**

Run:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform
git status --short --branch
git log --oneline --decorate -8
```

Expected:

```text
## yaya/platform-a
d07080d711 docs: record Phase 0 acceptance
c6e9d5ef0b feat: add Yaya module skeleton
28a0b8a505 docs: decide database path for Yaya RuoYi platform
9f6f7ca3a7 build: include PostgreSQL driver for server runtime
088cd1fb9e docs: define Yaya AI service contract
d14987a576 docs: define Yaya RuoYi module boundary
4d48ff6a3e docs: record RuoYi baseline build
b07f9233a1 docs: pin RuoYi upstream for Yaya platform
```

- [ ] **Step 2: Confirm backend runtime still responds**

Run:

```bash
curl -fsS http://127.0.0.1:48080/admin-api/yaya/health
```

Expected:

```json
{"code":0,"msg":"","data":"ok"}
```

- [ ] **Step 3: Create execution status document**

Create `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/docs/yaya-migration/EXECUTION_STATUS.md`:

```markdown
# Yaya RuoYi Execution Status

Date: 2026-05-28
Branch: yaya/platform-a

## Completed

- Phase 0 backend foundation accepted.
- RuoYi upstream pinned.
- PostgreSQL runtime selected and verified.
- `yudao-module-yaya` skeleton compiles into `yudao-server`.
- `GET /admin-api/yaya/health` returns `{"code":0,"msg":"","data":"ok"}`.

## Active Phase

Phase 1 - Admin frontend checkout and local login smoke.

## Blockers

- No runnable admin UI exists in the selected backend repo. The official `yudao-ui-admin-vue3` checkout must be added before browser-based admin testing.
```

- [ ] **Step 4: Commit status document**

Run:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform
git add docs/yaya-migration/EXECUTION_STATUS.md
git commit -m "docs: start Yaya migration execution status"
```

Expected: one documentation commit.

---

## Task 2: Add And Pin RuoYi Admin UI

**Files:**
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/.gitmodules`
- Create directory: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yudao-ui-admin-vue3`
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/docs/yaya-migration/UI_UPSTREAM.md`
- Modify: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/docs/yaya-migration/EXECUTION_STATUS.md`

- [ ] **Step 1: Add official admin UI as a submodule**

Run:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform
git submodule add https://github.com/yudaocode/yudao-ui-admin-vue3.git yudao-ui-admin-vue3
git submodule status yudao-ui-admin-vue3
```

Expected:

```text
One submodule status line ending with `yudao-ui-admin-vue3`.
```

- [ ] **Step 2: Record UI pin**

Run:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yudao-ui-admin-vue3
git rev-parse HEAD
git remote -v
```

Create `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/docs/yaya-migration/UI_UPSTREAM.md`:

```markdown
# RuoYi Admin UI Upstream Pin

Source repository: https://github.com/yudaocode/yudao-ui-admin-vue3
Pinned commit: record the exact 40-character SHA returned by `git rev-parse HEAD`
Parent repository: /Volumes/LamarHD/Yaya/yaya-ruoyi-platform

Purpose:
- Provide the Vue 3 + Element Plus admin frontend for Yaya operations.
- Keep frontend source separate but pinned through a Git submodule.
- Allow Yaya menu, API client, and admin pages to evolve alongside the RuoYi backend.
```

- [ ] **Step 3: Install frontend dependencies**

Run:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yudao-ui-admin-vue3
corepack enable
pnpm install --frozen-lockfile
```

Expected: install exits `0`.

- [ ] **Step 4: Configure local admin API base URL**

Inspect the UI env files:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yudao-ui-admin-vue3
ls -la .env*
rg -n "VITE|BASE|API|REQUEST|SERVER|48080|admin-api" .env* src
```

Set the local API base to:

```text
http://127.0.0.1:48080
```

Do not hardcode production credentials. If the upstream env file uses a different variable name, update the local development env file only and record the exact key in `docs/yaya-migration/UI_UPSTREAM.md`.

- [ ] **Step 5: Start admin UI**

Run:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yudao-ui-admin-vue3
pnpm dev --host 127.0.0.1 --port 8080
```

Expected: dev server listens on `http://127.0.0.1:8080`.

- [ ] **Step 6: Browser smoke**

Open:

```text
http://127.0.0.1:8080
```

Verify:

- Login page renders.
- Login request reaches `127.0.0.1:48080`.
- After login, RuoYi dashboard renders.
- `GET /admin-api/yaya/health` still returns `{"code":0,"msg":"","data":"ok"}`.

- [ ] **Step 7: Commit UI baseline**

Run:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform
git add .gitmodules yudao-ui-admin-vue3 docs/yaya-migration/UI_UPSTREAM.md docs/yaya-migration/EXECUTION_STATUS.md
git commit -m "build: add RuoYi admin UI submodule"
```

Expected: one commit with `.gitmodules`, submodule pointer, and UI pin doc.

---

## Task 3: Define Yaya PostgreSQL Schema

**Files:**
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/sql/postgresql/yaya/20260528_yaya_content.sql`
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/sql/postgresql/yaya/20260528_yaya_member_entitlement.sql`
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/sql/postgresql/yaya/20260528_yaya_ai_task.sql`
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/docs/yaya-migration/SCHEMA_MAP.md`

- [ ] **Step 1: Create schema directory**

Run:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform
mkdir -p sql/postgresql/yaya
```

- [ ] **Step 2: Add content schema**

Create `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/sql/postgresql/yaya/20260528_yaya_content.sql` with these table groups:

```sql
CREATE TABLE IF NOT EXISTS yaya_content_module (
  id int8 NOT NULL,
  module_key varchar(80) NOT NULL,
  name varchar(120) NOT NULL,
  description text NOT NULL DEFAULT '',
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_content_module PRIMARY KEY (id),
  CONSTRAINT uk_yaya_content_module_key UNIQUE (module_key)
);

CREATE TABLE IF NOT EXISTS yaya_content_season (
  id int8 NOT NULL,
  season_key varchar(40) NOT NULL,
  name varchar(120) NOT NULL,
  active int2 NOT NULL DEFAULT 1,
  defaulted int2 NOT NULL DEFAULT 0,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_content_season PRIMARY KEY (id),
  CONSTRAINT uk_yaya_content_season_key UNIQUE (season_key)
);

CREATE TABLE IF NOT EXISTS yaya_import_batch (
  id int8 NOT NULL,
  season_key varchar(40) NOT NULL,
  source_label varchar(120) NOT NULL,
  status varchar(40) NOT NULL,
  summary jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_by varchar(120) NOT NULL DEFAULT 'local-admin',
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_import_batch PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS yaya_practice_topic (
  id int8 NOT NULL,
  legacy_uuid varchar(64) NULL,
  season_id int8 NOT NULL,
  part int2 NOT NULL,
  stable_key varchar(180) NOT NULL,
  topic_no int4 NULL,
  title_en text NOT NULL DEFAULT '',
  title_zh text NOT NULL DEFAULT '',
  topic_type varchar(80) NOT NULL DEFAULT '',
  category varchar(120) NOT NULL DEFAULT '',
  prompt_en text NOT NULL DEFAULT '',
  prompt_zh text NOT NULL DEFAULT '',
  display_order int4 NOT NULL DEFAULT 0,
  review_status varchar(40) NOT NULL DEFAULT 'draft',
  publish_status varchar(40) NOT NULL DEFAULT 'draft',
  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_practice_topic PRIMARY KEY (id),
  CONSTRAINT uk_yaya_topic_stable_key UNIQUE (season_id, part, stable_key)
);

CREATE TABLE IF NOT EXISTS yaya_practice_question (
  id int8 NOT NULL,
  legacy_uuid varchar(64) NULL,
  topic_id int8 NOT NULL,
  question_role varchar(60) NOT NULL DEFAULT 'question',
  prompt_en text NOT NULL DEFAULT '',
  prompt_zh text NOT NULL DEFAULT '',
  cue_bullets jsonb NOT NULL DEFAULT '[]'::jsonb,
  display_order int4 NOT NULL DEFAULT 0,
  prepare_seconds int4 NULL,
  answer_seconds int4 NULL,
  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_practice_question PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_yaya_topic_season_part ON yaya_practice_topic (season_id, part, publish_status);
CREATE INDEX IF NOT EXISTS idx_yaya_question_topic ON yaya_practice_question (topic_id, display_order);
```

Add `yaya_source_document`, `yaya_source_topic_snapshot`, `yaya_topic_relation`, `yaya_tag`, and `yaya_topic_tag` in the same file using the same audit columns.

- [ ] **Step 3: Add member and entitlement schema**

Create `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/sql/postgresql/yaya/20260528_yaya_member_entitlement.sql` with:

```sql
CREATE TABLE IF NOT EXISTS yaya_legacy_user_map (
  id int8 NOT NULL,
  legacy_uuid varchar(64) NOT NULL,
  legacy_external_id varchar(120) NOT NULL,
  member_user_id int8 NOT NULL,
  migration_status varchar(40) NOT NULL DEFAULT 'mapped',
  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_legacy_user_map PRIMARY KEY (id),
  CONSTRAINT uk_yaya_legacy_uuid UNIQUE (legacy_uuid),
  CONSTRAINT uk_yaya_legacy_external_id UNIQUE (legacy_external_id)
);

CREATE TABLE IF NOT EXISTS yaya_member_plan (
  id int8 NOT NULL,
  plan_key varchar(80) NOT NULL,
  name varchar(120) NOT NULL,
  description text NOT NULL DEFAULT '',
  price_cent int8 NOT NULL DEFAULT 0,
  currency varchar(12) NOT NULL DEFAULT 'CNY',
  duration_days int4 NOT NULL DEFAULT 30,
  active int2 NOT NULL DEFAULT 1,
  benefits jsonb NOT NULL DEFAULT '{}'::jsonb,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_member_plan PRIMARY KEY (id),
  CONSTRAINT uk_yaya_member_plan_key UNIQUE (plan_key)
);

CREATE TABLE IF NOT EXISTS yaya_member_entitlement (
  id int8 NOT NULL,
  member_user_id int8 NOT NULL,
  plan_id int8 NOT NULL,
  source_type varchar(40) NOT NULL,
  source_id int8 NULL,
  status varchar(40) NOT NULL DEFAULT 'active',
  starts_at timestamp NOT NULL,
  ends_at timestamp NOT NULL,
  idempotency_key varchar(128) NOT NULL,
  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_member_entitlement PRIMARY KEY (id),
  CONSTRAINT uk_yaya_entitlement_idempotency UNIQUE (idempotency_key)
);

CREATE INDEX IF NOT EXISTS idx_yaya_entitlement_user_status ON yaya_member_entitlement (member_user_id, status, ends_at);
```

- [ ] **Step 4: Add AI task schema**

Create `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/sql/postgresql/yaya/20260528_yaya_ai_task.sql` with:

```sql
CREATE TABLE IF NOT EXISTS yaya_recording (
  id int8 NOT NULL,
  member_user_id int8 NOT NULL,
  topic_id int8 NULL,
  storage_path text NOT NULL,
  mime_type varchar(120) NOT NULL,
  duration_seconds numeric(10,2) NULL,
  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_recording PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS yaya_ai_task (
  id int8 NOT NULL,
  task_key varchar(64) NOT NULL,
  task_type varchar(40) NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'PENDING',
  request_payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  result_payload jsonb NULL,
  error_payload jsonb NULL,
  started_at timestamp NULL,
  finished_at timestamp NULL,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_ai_task PRIMARY KEY (id),
  CONSTRAINT uk_yaya_ai_task_key UNIQUE (task_key)
);

CREATE TABLE IF NOT EXISTS yaya_evaluation (
  id int8 NOT NULL,
  member_user_id int8 NOT NULL,
  recording_id int8 NULL,
  topic_id int8 NULL,
  ai_task_id int8 NULL,
  status varchar(40) NOT NULL DEFAULT 'PENDING',
  score_overall numeric(4,1) NULL,
  report jsonb NULL,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_evaluation PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_yaya_ai_task_status ON yaya_ai_task (status, create_time);
CREATE INDEX IF NOT EXISTS idx_yaya_evaluation_user ON yaya_evaluation (member_user_id, create_time);
```

- [ ] **Step 5: Apply schema to local PostgreSQL**

Run:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform
docker exec -i yaya-ruoyi-pg-phase0 psql -U root -d ruoyi-vue-pro < sql/postgresql/yaya/20260528_yaya_content.sql
docker exec -i yaya-ruoyi-pg-phase0 psql -U root -d ruoyi-vue-pro < sql/postgresql/yaya/20260528_yaya_member_entitlement.sql
docker exec -i yaya-ruoyi-pg-phase0 psql -U root -d ruoyi-vue-pro < sql/postgresql/yaya/20260528_yaya_ai_task.sql
```

Expected: no SQL errors.

- [ ] **Step 6: Verify schema**

Run:

```bash
docker exec yaya-ruoyi-pg-phase0 psql -U root -d ruoyi-vue-pro -Atc "select count(*) from information_schema.tables where table_schema='public' and table_name like 'yaya_%';"
```

Expected: at least `12`.

- [ ] **Step 7: Commit schema**

Run:

```bash
git add sql/postgresql/yaya docs/yaya-migration/SCHEMA_MAP.md
git commit -m "feat: add Yaya PostgreSQL schema"
```

---

## Task 4: Implement Content Domain In Java

**Files:**
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/enums/YayaErrorCodeConstants.java`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/dal/dataobject/content/*DO.java`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/dal/mysql/content/*Mapper.java`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/service/content/*Service.java`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/test/java/cn/iocoder/yudao/module/yaya/service/content/*ServiceImplTest.java`

- [ ] **Step 1: Write failing mapper/service tests**

Create tests for:

- create season `26Q1`
- create Part 1 topic with two questions
- list published topics by `seasonKey`, `part`, `pageNo`, `pageSize`
- reject duplicate `(season_id, part, stable_key)`

Run:

```bash
docker run --rm -u "$(id -u):$(id -g)" \
  -v "$PWD":/workspace \
  -v "$HOME/.m2/repository":/m2/repository \
  -e HOME=/tmp/maven-home \
  -w /workspace \
  maven:3.9.9-eclipse-temurin-17 \
  mvn -Dmaven.repo.local=/m2/repository \
    -pl yudao-module-yaya/yudao-module-yaya-biz -am \
    -Dtest=YayaContentServiceImplTest \
    -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected before implementation: compilation fails because the service and DO classes do not exist.

- [ ] **Step 2: Implement content DO and mapper classes**

Create DO classes for:

- `YayaContentSeasonDO`
- `YayaImportBatchDO`
- `YayaPracticeTopicDO`
- `YayaPracticeQuestionDO`
- `YayaTopicRelationDO`
- `YayaTagDO`
- `YayaTopicTagDO`

Each DO must extend RuoYi `BaseDO`, use `@TableName("yaya_*")`, and use `Long id`.

- [ ] **Step 3: Implement services**

Create service methods:

```java
Long createSeason(YayaContentSeasonCreateReq req);
Long createTopic(YayaTopicCreateReq req);
void replaceQuestions(Long topicId, List<YayaQuestionSaveReq> questions);
PageResult<YayaTopicListItemResp> getTopicPage(YayaTopicPageReqVO req);
YayaTopicDetailResp getTopicDetail(Long id);
void updateTopicPublishStatus(Long id, String publishStatus);
```

Statuses:

```text
draft
published
archived
```

- [ ] **Step 4: Run targeted tests**

Run the same Maven test command from Step 1.

Expected:

```text
Tests run: at least 1, Failures: 0, Errors: 0
BUILD SUCCESS
```

- [ ] **Step 5: Commit Java content domain**

Run:

```bash
git add yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya yudao-module-yaya/yudao-module-yaya-biz/src/test/java/cn/iocoder/yudao/module/yaya
git commit -m "feat: add Yaya content domain"
```

---

## Task 5: Implement Admin Content APIs

**Files:**
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/controller/admin/content/YayaTopicController.java`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/controller/admin/content/YayaImportController.java`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/controller/admin/content/vo/*`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/test/java/cn/iocoder/yudao/module/yaya/controller/admin/content/*Test.java`

- [ ] **Step 1: Write controller tests**

Test these admin endpoints:

```text
GET   /admin-api/yaya/topics
GET   /admin-api/yaya/topics/{id}
POST  /admin-api/yaya/topics
PATCH /admin-api/yaya/topics/{id}
PUT   /admin-api/yaya/topics/{id}/questions
PATCH /admin-api/yaya/topics/{id}/publish-status
POST  /admin-api/yaya/import-batches/{season}:preview
POST  /admin-api/yaya/import-batches/{season}:run
```

Expected before implementation: compilation fails because controllers and VO classes do not exist.

- [ ] **Step 2: Implement controllers using RuoYi response conventions**

Return `CommonResult<T>` from every endpoint. Use `PageResult<T>` for list endpoints. Admin routes must sit under:

```java
@RequestMapping("/yaya")
```

Controller method paths must match the endpoint list from Step 1.

- [ ] **Step 3: Add permission strings**

Use permission identifiers:

```text
yaya:topic:query
yaya:topic:create
yaya:topic:update
yaya:topic:publish
yaya:import:preview
yaya:import:run
```

- [ ] **Step 4: Run controller tests and package**

Run:

```bash
docker run --rm -u "$(id -u):$(id -g)" \
  -v "$PWD":/workspace \
  -v "$HOME/.m2/repository":/m2/repository \
  -e HOME=/tmp/maven-home \
  -w /workspace \
  maven:3.9.9-eclipse-temurin-17 \
  mvn -Dmaven.repo.local=/m2/repository \
    -pl yudao-module-yaya/yudao-module-yaya-biz -am \
    -Dtest=YayaTopicControllerTest,YayaImportControllerTest \
    -Dsurefire.failIfNoSpecifiedTests=false test

docker run --rm -u "$(id -u):$(id -g)" \
  -v "$PWD":/workspace \
  -v "$HOME/.m2/repository":/m2/repository \
  -e HOME=/tmp/maven-home \
  -w /workspace \
  maven:3.9.9-eclipse-temurin-17 \
  mvn -Dmaven.repo.local=/m2/repository -DskipTests clean package
```

Expected: both commands exit `0`.

- [ ] **Step 5: Runtime smoke**

Restart backend jar and run:

```bash
curl -fsS "http://127.0.0.1:48080/admin-api/yaya/topics?pageNo=1&pageSize=10"
```

Expected without login:

```json
{"code":401,"msg":"账号未登录","data":null}
```

This proves the endpoint is registered and secured.

- [ ] **Step 6: Commit admin APIs**

Run:

```bash
git add yudao-module-yaya
git commit -m "feat: add Yaya admin content APIs"
```

---

## Task 6: Implement Content Import Pipeline

**Files:**
- Read: `/Volumes/LamarHD/Yaya/YAYA-pages-for-Windows-version-dev/apps/api/yaya_api/services/import_26q1.py`
- Read: `/Volumes/LamarHD/Yaya/YAYA-pages-for-Windows-version-dev/apps/api/yaya_api/routes/admin.py`
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/tools/yaya-migration/export_legacy_content.py`
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/tools/yaya-migration/README.md`
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/docs/yaya-migration/content-import-snapshots/`
- Modify: Java import service in `yudao-module-yaya`

- [ ] **Step 1: Export legacy content snapshot**

Create `tools/yaya-migration/export_legacy_content.py` that connects to the legacy database using:

```text
YAYA_LEGACY_DATABASE_URL
```

It must export:

```text
content_modules.json
content_seasons.json
practice_topics.json
practice_questions.json
topic_relations.json
tags.json
topic_tags.json
manifest.json
```

Each record must include `legacy_uuid`, `created_at`, and `updated_at` when present.

- [ ] **Step 2: Run export against current legacy database**

Run:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform
YAYA_LEGACY_DATABASE_URL="postgresql+psycopg://yaya:yaya@127.0.0.1:5432/yaya" \
python3 tools/yaya-migration/export_legacy_content.py \
  --output docs/yaya-migration/content-import-snapshots/$(date +%Y%m%d-%H%M%S)
```

Expected:

```text
manifest.json written
practice_topics: non-zero count
practice_questions: non-zero count
```

- [ ] **Step 3: Implement Java preview import**

`POST /admin-api/yaya/import-batches/{season}:preview` must read the newest snapshot directory and return:

```json
{
  "seasonKey": "26Q1",
  "topics": 39,
  "questions": 227,
  "errors": []
}
```

Use the actual counts from `manifest.json`; do not hardcode them.

- [ ] **Step 4: Implement Java run import**

`POST /admin-api/yaya/import-batches/{season}:run` must:

- create or reuse the season
- upsert topics by `(season_id, part, stable_key)`
- replace questions for each topic
- store `legacy_uuid` for traceability
- write one `yaya_import_batch` row with summary JSON

- [ ] **Step 5: Verify import**

Run:

```bash
curl -fsS -X POST http://127.0.0.1:48080/admin-api/yaya/import-batches/26Q1:preview
curl -fsS -X POST http://127.0.0.1:48080/admin-api/yaya/import-batches/26Q1:run
docker exec yaya-ruoyi-pg-phase0 psql -U root -d ruoyi-vue-pro -Atc "select count(*) from yaya_practice_topic;"
docker exec yaya-ruoyi-pg-phase0 psql -U root -d ruoyi-vue-pro -Atc "select count(*) from yaya_practice_question;"
```

Expected: counts match the legacy snapshot manifest.

- [ ] **Step 6: Commit import pipeline**

Run:

```bash
git add tools/yaya-migration docs/yaya-migration/content-import-snapshots yudao-module-yaya
git commit -m "feat: add Yaya content import pipeline"
```

---

## Task 7: Build Admin UI Pages For Content Operations

**Files:**
- Modify: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yudao-ui-admin-vue3/src/router/modules/*`
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yudao-ui-admin-vue3/src/api/yaya/topic/index.ts`
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yudao-ui-admin-vue3/src/api/yaya/import/index.ts`
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yudao-ui-admin-vue3/src/views/yaya/topic/index.vue`
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yudao-ui-admin-vue3/src/views/yaya/import/index.vue`

- [ ] **Step 1: Add API clients**

Implement API methods:

```ts
getTopicPage(params)
getTopic(id)
createTopic(data)
updateTopic(id, data)
replaceQuestions(id, data)
updatePublishStatus(id, data)
previewImport(season)
runImport(season)
```

- [ ] **Step 2: Add topic list view**

The topic view must include:

- filters: season, part, publish status, keyword
- table columns: part, title, topic type, category, publish status, updated time
- actions: view, edit, publish, archive
- question editor in a dialog or drawer

- [ ] **Step 3: Add import view**

The import view must include:

- season input
- preview button
- run import button
- result summary panel
- errors table

- [ ] **Step 4: Add menu route**

Add Yaya menu entries:

```text
Yaya
  Content Topics
  Import Batches
```

- [ ] **Step 5: Verify UI**

Run:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yudao-ui-admin-vue3
pnpm lint
pnpm build
pnpm dev --host 127.0.0.1 --port 8080
```

Expected:

- lint exits `0`
- build exits `0`
- browser renders topic list
- import preview and run buttons call the backend

- [ ] **Step 6: Commit content UI**

Run:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform
git add yudao-ui-admin-vue3
git commit -m "feat: add Yaya admin content pages"
```

---

## Task 8: Implement App Practice APIs

**Files:**
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/controller/app/practice/YayaAppPracticeController.java`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/controller/app/practice/vo/*`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/service/practice/*`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/test/java/cn/iocoder/yudao/module/yaya/controller/app/practice/YayaAppPracticeControllerTest.java`

- [ ] **Step 1: Write app API tests**

Test:

```text
GET  /app-api/yaya/practice/topics?part=1&season=26Q1&pageNo=1&pageSize=20
GET  /app-api/yaya/practice/topics/{id}
POST /app-api/yaya/practice/favorites
POST /app-api/yaya/practice/attempts
```

Expected before implementation: compilation fails.

- [ ] **Step 2: Implement published-only topic list**

The list endpoint must:

- return only `publish_status = 'published'`
- filter by season key and part
- include `favorite` and `practiced` flags when a logged-in member exists
- return unauthenticated results without user overlays

- [ ] **Step 3: Implement favorite and attempt writes**

Create tables if not already present:

```text
yaya_favorite
yaya_user_topic_state
yaya_practice_attempt
```

Use `member_user_id` for user ownership.

- [ ] **Step 4: Verify**

Run:

```bash
docker run --rm -u "$(id -u):$(id -g)" \
  -v "$PWD":/workspace \
  -v "$HOME/.m2/repository":/m2/repository \
  -e HOME=/tmp/maven-home \
  -w /workspace \
  maven:3.9.9-eclipse-temurin-17 \
  mvn -Dmaven.repo.local=/m2/repository \
    -pl yudao-module-yaya/yudao-module-yaya-biz -am \
    -Dtest=YayaAppPracticeControllerTest \
    -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: tests pass.

- [ ] **Step 5: Runtime smoke**

Run:

```bash
curl -fsS "http://127.0.0.1:48080/app-api/yaya/practice/topics?part=1&season=26Q1&pageNo=1&pageSize=20"
```

Expected: JSON response with a `list` and `total`.

- [ ] **Step 6: Commit app practice APIs**

Run:

```bash
git add sql/postgresql/yaya yudao-module-yaya
git commit -m "feat: add Yaya app practice APIs"
```

---

## Task 9: Extract Python AI Service

**Files:**
- Create directory: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yaya-ai-service`
- Copy/adapt from: `/Volumes/LamarHD/Yaya/YAYA-pages-for-Windows-version-dev/packages/yaya-ai`
- Copy/adapt from: `/Volumes/LamarHD/Yaya/YAYA-pages-for-Windows-version-dev/apps/api/yaya_api/services/scoring_service.py`
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yaya-ai-service/app/main.py`
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yaya-ai-service/app/auth.py`
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yaya-ai-service/app/evaluations.py`
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yaya-ai-service/tests/test_internal_contract.py`

- [ ] **Step 1: Create service package**

Run:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform
mkdir -p yaya-ai-service/app yaya-ai-service/tests
```

- [ ] **Step 2: Add internal health endpoint**

Implement:

```text
GET /internal/health
```

Response:

```json
{"ok":true,"service":"yaya-ai-service"}
```

- [ ] **Step 3: Add internal auth middleware**

All endpoints except `/internal/health` require:

```text
X-Yaya-Internal-Key
X-Yaya-Request-Id
```

Invalid key returns HTTP `401`.
Missing request id returns HTTP `400`.

- [ ] **Step 4: Add evaluation endpoints**

Implement:

```text
POST /internal/evaluations
GET  /internal/evaluations/{task_id}
```

State values:

```text
PENDING
RUNNING
SUCCEEDED
FAILED
CANCELLED
```

`POST /internal/evaluations` is idempotent by `task_id`.

- [ ] **Step 5: Run Python tests**

Run:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yaya-ai-service
python3 -m venv .venv
. .venv/bin/activate
pip install -e .
pytest -q
```

Expected: all Python service tests pass.

- [ ] **Step 6: Run service smoke**

Run:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yaya-ai-service
. .venv/bin/activate
YAYA_INTERNAL_KEY=local-internal-key uvicorn app.main:app --host 127.0.0.1 --port 18080
```

Then:

```bash
curl -fsS http://127.0.0.1:18080/internal/health
```

Expected:

```json
{"ok":true,"service":"yaya-ai-service"}
```

- [ ] **Step 7: Commit AI service extraction**

Run:

```bash
git add yaya-ai-service docs/yaya-migration/AI_SERVICE_CONTRACT.md
git commit -m "feat: add Yaya AI service"
```

---

## Task 10: Implement Java AI Client And Task Orchestration

**Files:**
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/framework/ai/YayaAiProperties.java`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/framework/ai/YayaAiClient.java`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/service/ai/YayaAiTaskService.java`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/dal/dataobject/ai/*DO.java`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/test/java/cn/iocoder/yudao/module/yaya/framework/ai/YayaAiClientTest.java`

- [ ] **Step 1: Write client contract tests**

Use a local mock HTTP server. Verify:

- `X-Yaya-Internal-Key` is sent
- `X-Yaya-Request-Id` is generated when absent
- 401 from Python maps to `YAYA_AI_UNAUTHORIZED`
- accepted evaluation response maps to Java DTO

- [ ] **Step 2: Implement AI properties**

Properties:

```yaml
yaya:
  ai:
    base-url: http://127.0.0.1:18080
    internal-key: local-internal-key
    timeout-seconds: 60
```

- [ ] **Step 3: Implement task service**

Service methods:

```java
Long createEvaluationTask(Long memberUserId, Long recordingId, Long topicId);
void pollTaskResult(Long taskId);
void cancelTask(Long taskId);
```

Do not perform model calls inside a database transaction.

- [ ] **Step 4: Verify**

Run:

```bash
docker run --rm -u "$(id -u):$(id -g)" \
  -v "$PWD":/workspace \
  -v "$HOME/.m2/repository":/m2/repository \
  -e HOME=/tmp/maven-home \
  -w /workspace \
  maven:3.9.9-eclipse-temurin-17 \
  mvn -Dmaven.repo.local=/m2/repository \
    -pl yudao-module-yaya/yudao-module-yaya-biz -am \
    -Dtest=YayaAiClientTest,YayaAiTaskServiceImplTest \
    -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: tests pass.

- [ ] **Step 5: Commit Java AI orchestration**

Run:

```bash
git add yudao-module-yaya yudao-server/src/main/resources
git commit -m "feat: add Yaya AI task orchestration"
```

---

## Task 11: Implement Recording And Evaluation App APIs

**Files:**
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/controller/app/evaluation/YayaAppEvaluationController.java`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/controller/app/recording/YayaAppRecordingController.java`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/service/recording/*`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/service/evaluation/*`

- [ ] **Step 1: Add endpoints**

Implement:

```text
POST /app-api/yaya/recordings
POST /app-api/yaya/evaluations
GET  /app-api/yaya/evaluations/{evaluationId}
POST /app-api/yaya/evaluations/{evaluationId}/polish-pack
```

- [ ] **Step 2: Store uploaded recording metadata**

For the first local version, store files under:

```text
/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/storage/recordings/
```

Persist metadata in `yaya_recording`.

- [ ] **Step 3: Create evaluation task**

`POST /app-api/yaya/evaluations` must:

- validate entitlement before accepting paid-only features
- create `yaya_evaluation`
- create `yaya_ai_task`
- call `POST /internal/evaluations`
- return evaluation id and task status

- [ ] **Step 4: Verify with service running**

Run Python AI service on `127.0.0.1:18080`, backend on `127.0.0.1:48080`, then:

```bash
curl -fsS -X POST http://127.0.0.1:48080/app-api/yaya/evaluations \
  -H "Content-Type: application/json" \
  -d '{"topicId":1,"recordingId":1,"options":{"runTextRoute":true,"runAudioRoute":true,"runPronunciationRoute":true,"runImprovement":true}}'
```

Expected: accepted task JSON or a controlled entitlement/login error. It must not return a server exception.

- [ ] **Step 5: Commit recording/evaluation APIs**

Run:

```bash
git add yudao-module-yaya storage/.gitkeep
git commit -m "feat: add Yaya recording and evaluation APIs"
```

---

## Task 12: Implement Member Plans And Entitlements

**Files:**
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/controller/admin/member/YayaMemberPlanController.java`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/controller/app/member/YayaAppEntitlementController.java`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/service/member/*`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/test/java/cn/iocoder/yudao/module/yaya/service/member/*Test.java`

- [ ] **Step 1: Define default plans**

Seed plans:

```text
free-trial: 0 CNY, 7 days, limited evaluations
monthly-pro: 39 CNY, 30 days, full practice and evaluation access
quarterly-pro: 99 CNY, 90 days, full practice and evaluation access
```

- [ ] **Step 2: Implement admin plan CRUD**

Admin endpoints:

```text
GET   /admin-api/yaya/member-plans
POST  /admin-api/yaya/member-plans
PATCH /admin-api/yaya/member-plans/{id}
PATCH /admin-api/yaya/member-plans/{id}/status
```

- [ ] **Step 3: Implement entitlement check**

App endpoint:

```text
GET /app-api/yaya/entitlements/me
```

Response fields:

```json
{
  "active": true,
  "planKey": "monthly-pro",
  "endsAt": "2026-06-28T00:00:00"
}
```

- [ ] **Step 4: Enforce entitlement in paid APIs**

Paid-gated APIs:

```text
POST /app-api/yaya/evaluations
POST /app-api/yaya/evaluations/{evaluationId}/polish-pack
POST /api/compat/yaya/evaluations
```

Free APIs:

```text
GET /app-api/yaya/practice/topics
GET /app-api/yaya/practice/topics/{id}
```

- [ ] **Step 5: Verify**

Run:

```bash
docker run --rm -u "$(id -u):$(id -g)" \
  -v "$PWD":/workspace \
  -v "$HOME/.m2/repository":/m2/repository \
  -e HOME=/tmp/maven-home \
  -w /workspace \
  maven:3.9.9-eclipse-temurin-17 \
  mvn -Dmaven.repo.local=/m2/repository \
    -pl yudao-module-yaya/yudao-module-yaya-biz -am \
    -Dtest=YayaEntitlementServiceImplTest \
    -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: tests pass.

- [ ] **Step 6: Commit entitlement system**

Run:

```bash
git add yudao-module-yaya sql/postgresql/yaya
git commit -m "feat: add Yaya member plans and entitlements"
```

---

## Task 13: Integrate RuoYi Pay For Membership Orders

**Files:**
- Read: `yudao-module-pay/src/main/java/cn/iocoder/yudao/module/pay/api/order/PayOrderApi.java`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/service/pay/YayaMemberOrderService.java`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/controller/app/pay/YayaAppPayController.java`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/test/java/cn/iocoder/yudao/module/yaya/service/pay/YayaMemberOrderServiceImplTest.java`

- [ ] **Step 1: Add pay module dependency**

Add dependency to `yudao-module-yaya/yudao-module-yaya-biz/pom.xml`:

```xml
<dependency>
    <groupId>cn.iocoder.boot</groupId>
    <artifactId>yudao-module-pay-api</artifactId>
    <version>${revision}</version>
</dependency>
```

- [ ] **Step 2: Create membership order endpoint**

App endpoint:

```text
POST /app-api/yaya/pay/member-orders
```

Request:

```json
{"planKey":"monthly-pro","channelCode":"mock"}
```

Response:

```json
{"orderId":123,"payOrderId":456,"status":"WAITING_PAYMENT"}
```

- [ ] **Step 3: Handle pay callback idempotently**

Implement service method:

```java
void activateEntitlementByPayOrder(Long payOrderId);
```

Rules:

- duplicate callback does not create duplicate entitlement
- failed payment does not activate entitlement
- callback writes `idempotency_key = "pay_order:" + payOrderId`

- [ ] **Step 4: Verify mock payment flow**

Run service tests for:

- create pay order
- simulate paid callback
- duplicate paid callback
- entitlement date range

Expected: all pass.

- [ ] **Step 5: Commit pay integration**

Run:

```bash
git add yudao-module-yaya
git commit -m "feat: integrate RuoYi pay for Yaya membership"
```

---

## Task 14: Implement Legacy Compatibility Layer

**Files:**
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/main/java/cn/iocoder/yudao/module/yaya/controller/compat/YayaCompatController.java`
- Create: `docs/yaya-migration/LEGACY_API_MAP.md`
- Create: `yudao-module-yaya/yudao-module-yaya-biz/src/test/java/cn/iocoder/yudao/module/yaya/controller/compat/YayaCompatControllerTest.java`

- [ ] **Step 1: Document legacy route map**

Create `docs/yaya-migration/LEGACY_API_MAP.md` with:

```markdown
# Legacy API Map

| Legacy route | New route | Compatibility status |
|---|---|---|
| GET /api/health | GET /admin-api/yaya/health | direct |
| GET /api/topics | GET /app-api/yaya/practice/topics | adapter |
| GET /api/topics/{topic_id} | GET /app-api/yaya/practice/topics/{id} | adapter |
| POST /api/recordings | POST /app-api/yaya/recordings | adapter |
| POST /api/evaluations | POST /app-api/yaya/evaluations | adapter |
| GET /api/evaluations/{evaluation_id} | GET /app-api/yaya/evaluations/{id} | adapter |
| POST /api/evaluations/{evaluation_id}/polish-pack | POST /app-api/yaya/evaluations/{id}/polish-pack | adapter |
| GET /api/practice/topics | GET /app-api/yaya/practice/topics | adapter |
| POST /api/practice/favorites | POST /app-api/yaya/practice/favorites | adapter |
| POST /api/practice/attempts | POST /app-api/yaya/practice/attempts | adapter |
```

- [ ] **Step 2: Implement adapters**

Expose compatibility routes under:

```text
/app-api/api/*
```

This keeps old browser paths usable when the frontend API base is changed from `http://127.0.0.1:8000` to the RuoYi app API host.

- [ ] **Step 3: Verify legacy response shape**

For `GET /app-api/api/topics?part=1`, response must preserve:

```json
{
  "topics": [],
  "part": 1
}
```

For evaluation routes, preserve legacy status field names while storing canonical Java states internally.

- [ ] **Step 4: Commit compatibility layer**

Run:

```bash
git add yudao-module-yaya docs/yaya-migration/LEGACY_API_MAP.md
git commit -m "feat: add Yaya legacy API compatibility layer"
```

---

## Task 15: Move Current User-Facing Pages To New API Base

**Files:**
- Read/modify: `/Volumes/LamarHD/Yaya/YAYA-pages-for-Windows-version-dev/apps/web/*.html`
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/docs/yaya-migration/FRONTEND_COMPAT_TESTS.md`

- [ ] **Step 1: Inventory current API calls**

Run:

```bash
cd /Volumes/LamarHD/Yaya/YAYA-pages-for-Windows-version-dev
rg -n "127\\.0\\.0\\.1:8000|/api/" apps/web/*.html
```

Write all discovered route families to `FRONTEND_COMPAT_TESTS.md`.

- [ ] **Step 2: Add configurable API base**

For each HTML page that hardcodes `http://127.0.0.1:8000`, change it to:

```js
const API_BASE = window.YAYA_API_BASE || 'http://127.0.0.1:48080/app-api';
```

- [ ] **Step 3: Run browser checks**

Serve the current static pages and verify:

```text
practice-part2.html
practice-part3.html
mock.html
relay.html
memory-flashcards.html
settings.html
```

Each page must either load data from RuoYi or show a controlled empty state. No page may fail with a JavaScript syntax error.

- [ ] **Step 4: Commit frontend compatibility changes**

Run in the legacy product repo:

```bash
cd /Volumes/LamarHD/Yaya/YAYA-pages-for-Windows-version-dev
git add apps/web docs
git commit -m "feat: point Yaya pages to RuoYi compatibility API"
```

Record the commit in `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/docs/yaya-migration/FRONTEND_COMPAT_TESTS.md`, then commit that doc in the platform repo.

---

## Task 16: Add Docker Compose Deployment Baseline

**Files:**
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/deploy/yaya-ruoyi/docker-compose.yml`
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/deploy/yaya-ruoyi/.env.example`
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/deploy/yaya-ruoyi/README.md`
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yaya-ai-service/Dockerfile`
- Modify: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yudao-server/Dockerfile` if upstream file is missing or incompatible

- [ ] **Step 1: Define services**

Compose services:

```text
postgres
redis
yudao-server
yudao-ui-admin
yaya-ai-service
```

Ports:

```text
postgres: 15432 -> 5432
redis: 16379 -> 6379
yudao-server: 48080 -> 48080
yudao-ui-admin: 8080 -> 80
yaya-ai-service: 18080 -> 18080
```

- [ ] **Step 2: Add environment file**

`.env.example` keys:

```text
POSTGRES_DB=ruoyi-vue-pro
POSTGRES_USER=root
POSTGRES_PASSWORD=123456
REDIS_PASSWORD=
YAYA_INTERNAL_KEY=local-internal-key
YAYA_AI_BASE_URL=http://yaya-ai-service:18080
```

- [ ] **Step 3: Verify compose boot**

Run:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform/deploy/yaya-ruoyi
cp .env.example .env
docker compose up -d --build
docker compose ps
curl -fsS http://127.0.0.1:48080/admin-api/yaya/health
curl -fsS http://127.0.0.1:18080/internal/health
```

Expected:

```json
{"code":0,"msg":"","data":"ok"}
{"ok":true,"service":"yaya-ai-service"}
```

- [ ] **Step 4: Commit deployment baseline**

Run:

```bash
git add deploy/yaya-ruoyi yaya-ai-service/Dockerfile yudao-server/Dockerfile
git commit -m "build: add Yaya RuoYi docker compose baseline"
```

---

## Task 17: End-To-End Acceptance

**Files:**
- Create: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/docs/yaya-migration/ACCEPTANCE_FULL_MIGRATION.md`

- [ ] **Step 1: Backend verification**

Run:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform
docker run --rm -u "$(id -u):$(id -g)" \
  -v "$PWD":/workspace \
  -v "$HOME/.m2/repository":/m2/repository \
  -e HOME=/tmp/maven-home \
  -w /workspace \
  maven:3.9.9-eclipse-temurin-17 \
  mvn -Dmaven.repo.local=/m2/repository test

docker run --rm -u "$(id -u):$(id -g)" \
  -v "$PWD":/workspace \
  -v "$HOME/.m2/repository":/m2/repository \
  -e HOME=/tmp/maven-home \
  -w /workspace \
  maven:3.9.9-eclipse-temurin-17 \
  mvn -Dmaven.repo.local=/m2/repository -DskipTests clean package
```

Expected: both commands exit `0`.

- [ ] **Step 2: Python AI verification**

Run:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yaya-ai-service
. .venv/bin/activate
pytest -q
```

Expected: all tests pass.

- [ ] **Step 3: Admin UI verification**

Run:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yudao-ui-admin-vue3
pnpm lint
pnpm build
```

Expected: both commands exit `0`.

- [ ] **Step 4: Runtime route verification**

Run:

```bash
curl -fsS http://127.0.0.1:48080/admin-api/yaya/health
curl -fsS "http://127.0.0.1:48080/app-api/yaya/practice/topics?part=1&season=26Q1&pageNo=1&pageSize=20"
curl -fsS http://127.0.0.1:18080/internal/health
```

Expected: all routes return controlled JSON.

- [ ] **Step 5: Browser verification**

Verify in browser:

```text
http://127.0.0.1:8080
```

Flows:

- admin login
- Yaya topic list
- import preview
- import run
- publish one topic
- app practice list returns the published topic
- evaluation request creates an AI task or returns controlled entitlement/login error
- membership plan page renders

- [ ] **Step 6: Write acceptance document**

Create `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/docs/yaya-migration/ACCEPTANCE_FULL_MIGRATION.md` with:

```markdown
# Full Migration Acceptance

Date: 2026-05-28
Branch: yaya/platform-a

## Verified Commands

- Maven test:
- Maven package:
- Python pytest:
- Admin UI lint:
- Admin UI build:
- Docker compose:

## Verified Runtime Routes

- GET /admin-api/yaya/health
- GET /app-api/yaya/practice/topics
- POST /admin-api/yaya/import-batches/26Q1:preview
- POST /admin-api/yaya/import-batches/26Q1:run
- GET /internal/health

## Verified Browser Flows

- Admin login:
- Yaya content list:
- Import preview/run:
- Topic publish:
- Membership plan page:

## Remaining Production Gates

- Real payment channel credentials and callback domain.
- Object storage credentials for recording files.
- Production AI provider keys and cost limits.
- Domain, TLS, and backup policy.
```

- [ ] **Step 7: Commit final acceptance**

Run:

```bash
git add docs/yaya-migration/ACCEPTANCE_FULL_MIGRATION.md
git commit -m "docs: record full Yaya migration acceptance"
```

---

## Task 18: Release Branch And Remote Push

**Files:**
- Read: full `git status`
- Read: full `git log`

- [ ] **Step 1: Final repository audit**

Run:

```bash
cd /Volumes/LamarHD/Yaya/yaya-ruoyi-platform
git status --short --branch
git log --oneline --decorate --graph --max-count=30
```

Expected: clean worktree on `yaya/platform-a`.

- [ ] **Step 2: Push feature branch**

Run:

```bash
git push -u origin yaya/platform-a
```

Expected: remote branch created or updated.

- [ ] **Step 3: Decide integration target**

Use this rule:

- push to `develop` when the remote repository has an active `develop` branch
- create a pull request to `main` when `develop` does not exist or the remote enforces PR review
- do not force-push `main`

- [ ] **Step 4: Final deployment handoff**

Write release notes in:

```text
docs/yaya-migration/RELEASE_NOTES_PLATFORM_A.md
```

Include:

- commit range
- verified commands
- running ports
- deployment compose command
- production gates

Commit:

```bash
git add docs/yaya-migration/RELEASE_NOTES_PLATFORM_A.md
git commit -m "docs: add Platform A release notes"
```

Push:

```bash
git push
```

---

## Execution Order

Run tasks in this order:

```text
1 -> 2 -> 3 -> 4 -> 5 -> 6 -> 7 -> 8 -> 9 -> 10 -> 11 -> 12 -> 13 -> 14 -> 15 -> 16 -> 17 -> 18
```

Safe parallelism:

- Task 2 admin UI baseline can run in parallel with Task 3 schema after Phase 0 is frozen.
- Task 9 Python AI service can run in parallel with Task 4 content Java domain.
- Task 12 entitlement service can start after Task 3 schema and does not need Task 9.
- Task 15 frontend compatibility must wait for Task 14 compatibility routes.
- Task 17 acceptance must wait for all prior tasks.

## Completion Definition

The migration is not complete until all of the following are true:

- RuoYi backend builds and tests pass.
- Admin UI builds and logs into the backend.
- PostgreSQL has Yaya content, member entitlement, and AI task tables.
- Legacy content is imported with counts matching the exported manifest.
- Admin can import, edit, and publish topics.
- App practice endpoints return published topics.
- Python AI service responds to internal health and evaluation task endpoints.
- Java can create and poll AI tasks through the Python service.
- Membership plans and entitlement checks work.
- Payment order integration activates entitlement idempotently.
- Current user-facing pages can reach the RuoYi compatibility API.
- Docker Compose starts the full local stack.
- `ACCEPTANCE_FULL_MIGRATION.md` records commands, outputs, browser checks, and remaining production gates.
