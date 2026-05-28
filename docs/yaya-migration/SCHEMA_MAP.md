# Yaya PostgreSQL Schema Map

Date: 2026-05-28
Branch: yaya/platform-a

## Purpose

This schema is the first PostgreSQL baseline for moving Yaya business data into
`yudao-module-yaya` while keeping RuoYi system, member, pay, and mall modules as
separate owners.

## SQL Files

| File | Table group |
|---|---|
| `sql/postgresql/yaya/20260528_yaya_content.sql` | content modules, seasons, source imports, topics, questions, relations, tags |
| `sql/postgresql/yaya/20260528_yaya_member_entitlement.sql` | legacy user mapping, Yaya member plans, member entitlement records |
| `sql/postgresql/yaya/20260528_yaya_ai_task.sql` | recordings, transcripts, AI tasks, evaluation reports |

## Table Map

| Legacy model or capability | Target table |
|---|---|
| `ContentModule` | `yaya_content_module` |
| `ContentSeason` | `yaya_content_season` |
| `ImportBatch` | `yaya_import_batch` |
| `SourceDocument` | `yaya_source_document` |
| `SourceTopicSnapshot` | `yaya_source_topic_snapshot` |
| `PracticeTopic` | `yaya_practice_topic` |
| `PracticeQuestion` | `yaya_practice_question` |
| `TopicRelation` | `yaya_topic_relation` |
| `Tag` | `yaya_tag` |
| `TopicTag` | `yaya_topic_tag` |
| Legacy `User` identity bridge | `yaya_legacy_user_map` with `member_user_id` |
| Membership products | `yaya_member_plan` |
| Membership access decisions | `yaya_member_entitlement` |
| `Recording` | `yaya_recording` |
| `Transcript` | `yaya_transcript` |
| `Evaluation` orchestration | `yaya_evaluation` and `yaya_ai_task` |

## RuoYi Alignment

- Primary keys are `int8` and are intended for RuoYi/MyBatis Plus ID assignment.
- Audit columns follow the RuoYi convention: `creator`, `create_time`,
  `updater`, `update_time`, `deleted`, `tenant_id`.
- Yaya user-owned rows reference RuoYi `member_user.id` by `member_user_id`.
- The schema avoids physical foreign keys in the first baseline, matching
  common RuoYi operational practice and keeping imports/replays simple.
- Vector tables are intentionally deferred until the runtime image includes
  pgvector; the current local acceptance database is `postgres:14.2`.

## Verification

Initial red check before these files were created:

```bash
docker exec yaya-ruoyi-pg-phase0 psql -U root -d ruoyi-vue-pro -Atc "select count(*) from information_schema.tables where table_schema='public' and table_name like 'yaya_%';"
```

Result:

```text
0
```

Expected after applying all three files:

```text
17 yaya_* tables
```

Actual local verification after applying all three files:

```text
17
```

Verified table list:

```text
yaya_ai_task
yaya_content_module
yaya_content_season
yaya_evaluation
yaya_import_batch
yaya_legacy_user_map
yaya_member_entitlement
yaya_member_plan
yaya_practice_question
yaya_practice_topic
yaya_recording
yaya_source_document
yaya_source_topic_snapshot
yaya_tag
yaya_topic_relation
yaya_topic_tag
yaya_transcript
```

Backend health after schema import:

```bash
curl -fsS http://127.0.0.1:48080/admin-api/yaya/health
```

```json
{"code":0,"msg":"","data":"ok"}
```
