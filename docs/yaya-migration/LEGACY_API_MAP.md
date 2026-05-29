# Legacy API Map

Task 14 exposes legacy Yaya browser paths below the RuoYi app API prefix:

```text
/app-api/api/*
```

This keeps the old frontend contract usable after changing the browser API base
from the FastAPI host to the RuoYi app API host.

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
| GET /api/practice/topics/{topic_id} | GET /app-api/yaya/practice/topics/{id} | adapter |
| POST /api/practice/favorites | POST /app-api/yaya/practice/favorites | adapter |
| POST /api/practice/attempts | POST /app-api/yaya/practice/attempts | adapter |

## Response Compatibility

- Compatibility controller: `cn.iocoder.yudao.module.yaya.controller.app.compat.YayaCompatController`.
- Controller mapping remains `@RequestMapping("/api")`; RuoYi's app controller prefixing exposes it as `/app-api/api/*`.
- `GET /app-api/api/topics?part=1` returns direct JSON with `topics` and `part`, and also includes `items` and `count` for compatibility with the current FastAPI shape.
- `GET /app-api/api/practice/topics` keeps the public practice shape with `items`, `count`, `season`, `part`, `page`, `page_size`, `total_pages`, snake_case topic fields, and `user_state`.
- `GET /app-api/api/practice/topics` applies legacy `topic_type`, `progress_filter`, and `q` filters before pagination. These legacy filters intentionally run in memory because the IELTS season corpus is bounded.
- High-page requests preserve the requested `page` metadata and return `items=[]` when the filtered result set has no rows for that page.
- `GET /app-api/api/practice/topics/{topic_id}` keeps the public practice detail shape with snake_case topic fields, `questions`, `metadata`, `completed_question_ids`, and `user_state`.
- `POST /app-api/api/practice/favorites` accepts either `topic_id` or string `item_id`; `active=false` removes the favorite and returns the removed favorite id when one existed.
- `POST /app-api/api/practice/attempts` accepts legacy `question_id`; the compatibility layer stores it through the canonical attempt metadata and authenticated detail calls return it in `completed_question_ids`.
- Evaluation status values are adapted from Java canonical states to legacy names: `PENDING -> queued`, `RUNNING -> running`, `SUCCEEDED/COMPLETED/DONE -> done`, and `FAILED/CANCELLED -> failed`.
- `GET /app-api/api/evaluations/{evaluation_id}` exposes legacy evaluation fields such as `text_route_scores`, `audio_route_scores`, `pron_route_scores`, `fluency_metrics`, `band_lower`, `band_upper`, `attemptId`, `questionId`, `createdAt`, and `updatedAt`.
- Read-only topic and health compatibility routes are anonymous and tenant-ignored. Recording, evaluation, favorite, and attempt write routes keep app-login enforcement.

## Notes

- `GET /api/health` is listed as direct to the existing admin health route, but the compatibility layer also exposes `/app-api/api/health` as direct JSON for old browser health probes.
- `POST /api/evaluations/{evaluation_id}/polish-pack` delegates to the current Yaya evaluation polish-pack service and returns the legacy `polish_pack_v4` object shape: `schema_version`, `original_zh`, `ai_fixed_en`, `ai_fixed_zh`, `topic_summary_zh`, `answer_quality`, `polish`, and `related_topics`.
