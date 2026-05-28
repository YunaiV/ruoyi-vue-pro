# Yaya Module Boundary

Date: 2026-05-28
Branch: yaya/platform-a

This document defines the target ownership boundary for migrating the current
Yaya FastAPI product into the RuoYi platform.

## Capability Mapping

| Current capability | Current source | New owner | Phase |
|---|---|---|---|
| Auth/login | apps/api/yaya_api/routes/auth.py | RuoYi system/member | Phase 3 |
| Topic seasons | apps/api/yaya_api/db/models.py | yudao-module-yaya | Phase 2 |
| Part 1/2/3 topics | apps/api/yaya_api/routes/practice.py | yudao-module-yaya | Phase 2 |
| Admin import/edit/publish | apps/api/yaya_api/routes/admin.py | yudao-module-yaya admin UI | Phase 2 |
| Recordings | apps/api/yaya_api/main.py | yudao-module-yaya metadata + object storage | Phase 4 |
| Evaluations | apps/api/yaya_api/services/scoring_service.py | Java task orchestration + Python AI execution | Phase 4 |
| Polish pack | apps/api/yaya_api/main.py | Python AI service behind Java task | Phase 4 |
| Memory/chunks | apps/api/yaya_api/routes/memory.py | yudao-module-yaya + yaya-ai-service | Phase 4 |
| Membership | not implemented as product system | RuoYi member/mall/pay + yaya entitlement | Phase 3 |
| Payment | not implemented as product system | RuoYi pay | Phase 3 |

## Module Rules

- `yudao-module-yaya` may depend on RuoYi system/member/pay/mall APIs.
- RuoYi system/member/pay/mall modules must not depend on `yudao-module-yaya`.
- Python AI service is called only through HTTP with signed internal service credentials.
- Public user APIs can be compatibility routes during migration, but final ownership belongs to Java.

## Source Evidence

Current Yaya product repository inspected:

- `/Volumes/LamarHD/Yaya/YAYA-pages-for-Windows-version-dev/README.md`
- `/Volumes/LamarHD/Yaya/YAYA-pages-for-Windows-version-dev/apps/api/README.md`
- `/Volumes/LamarHD/Yaya/YAYA-pages-for-Windows-version-dev/apps/api/yaya_api/db/models.py`

The README files describe the current FastAPI API boundary, practice/admin
routes, recording upload, evaluation orchestration, polish-pack flow, memory
features, and Python AI package. The SQLAlchemy models confirm current tables
for seasons, topics, users/auth sessions, recordings, evaluations, and memory
items. No implemented membership or payment product system was identified in
the inspected source evidence.
