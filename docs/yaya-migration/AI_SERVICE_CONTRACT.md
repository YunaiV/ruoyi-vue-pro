# Yaya AI Service Contract

This contract defines the internal HTTP boundary between the RuoYi Java
platform and the standalone Python `yaya-ai-service`.

## Internal Auth

Every internal endpoint except `GET /internal/health` requires:

```http
X-Yaya-Internal-Key: ${YAYA_INTERNAL_KEY}
X-Yaya-Request-Id: 7f3b4be8-02aa-4965-949a-5b7d6d5d0e8c
```

Python rejects missing or invalid `X-Yaya-Internal-Key` values with HTTP 401.
`X-Yaya-Request-Id` is required for request tracing and should be logged by
both Java and Python.

## Internal Endpoints

### GET /internal/health

Response:

```json
{
  "ok": true,
  "service": "yaya-ai-service"
}
```

### POST /internal/evaluations

Java creates and owns `task_id`, persists the evaluation row, and calls Python
to accept asynchronous AI work.

Request:

```json
{
  "task_id": "018fb4be-02aa-4965-949a-5b7d6d5d0e8c",
  "evaluation_id": "018fb4be-02aa-4965-949a-5b7d6d5d0e8c",
  "recording_id": "018fb4be-1111-4965-949a-5b7d6d5d0e8c",
  "user_id": "10001",
  "topic_id": "part2-family-home",
  "audio": {
    "storage_path": "recordings/10001/018fb4be.webm",
    "mime_type": "audio/webm",
    "duration_seconds": 122.4
  },
  "topic": {
    "title_en": "Describe a room you like",
    "title_zh": "描述一个你喜欢的房间",
    "prompt_en": "Describe a room you like.",
    "prompt_zh": "描述一个你喜欢的房间。",
    "cue_bullets": [
      "where it is",
      "what it looks like",
      "what you do there"
    ]
  },
  "options": {
    "run_text_route": true,
    "run_audio_route": true,
    "run_pronunciation_route": true,
    "run_improvement": true
  }
}
```

Accepted response:

```json
{
  "task_id": "018fb4be-02aa-4965-949a-5b7d6d5d0e8c",
  "status": "PENDING",
  "accepted": true
}
```

### GET /internal/evaluations/{task_id}

Response:

```json
{
  "task_id": "018fb4be-02aa-4965-949a-5b7d6d5d0e8c",
  "evaluation_id": "018fb4be-02aa-4965-949a-5b7d6d5d0e8c",
  "status": "SUCCEEDED",
  "progress": {
    "stage": "done",
    "message": "evaluation complete"
  },
  "result": {
    "scores": {
      "fc": 6.5,
      "lr": 6.0,
      "gra": 6.0,
      "p": 6.0,
      "overall": 6.0
    },
    "text_route_scores": null,
    "audio_route_scores": null,
    "pron_route_scores": null,
    "band_lower": 5.5,
    "band_upper": 6.5,
    "fluency_metrics": null,
    "report": {
      "summary": "Concise evaluation summary.",
      "feedback": {
        "fc": "Fluency and coherence feedback.",
        "lr": "Lexical resource feedback.",
        "gra": "Grammar range and accuracy feedback.",
        "p": "Pronunciation feedback."
      },
      "p_is_estimate": false,
      "transcript": "Candidate transcript text.",
      "transcriptSource": "openai-whisper",
      "transcriptLanguage": "en",
      "topicId": "part2-family-home",
      "attemptId": null,
      "questionId": null,
      "topicTitle": "Describe a room you like",
      "source": "yaya-ai-multi-route",
      "configuredRoutes": {
        "text": true,
        "audio": true,
        "pronunciation": true
      },
      "errors": {},
      "bandRange": {
        "lower": 5.5,
        "upper": 6.5
      },
      "suggestions": null
    }
  },
  "error": null,
  "created_at": "2026-05-28T00:00:00Z",
  "updated_at": "2026-05-28T00:01:30Z"
}
```

For non-terminal tasks, `result` is `null`. For `FAILED`, `error` contains a
short machine-readable `code` and human-readable `message`.

## State And Idempotency Rules

- `PENDING`: Python accepted the Java-owned `task_id`; work has not started.
- `RUNNING`: Python started STT, scoring, pronunciation, or improvement work.
- `SUCCEEDED`: Python completed successfully and `result` is immutable.
- `FAILED`: Python reached a terminal error; `error` is immutable.
- `CANCELLED`: Java cancelled or superseded the task; Python must not continue
  expensive model calls after observing cancellation.
- `POST /internal/evaluations` is idempotent by Java-owned `task_id`. Retrying
  the same `task_id` with the same payload returns the existing accepted task.
- Reusing a `task_id` with a different payload is a conflict and must not create
  a second task.
- Terminal states (`SUCCEEDED`, `FAILED`, `CANCELLED`) are immutable.

## Source Evidence

Inspected source files:

- `/Volumes/LamarHD/Yaya/YAYA-pages-for-Windows-version-dev/apps/api/yaya_api/services/scoring_service.py`
- `/Volumes/LamarHD/Yaya/YAYA-pages-for-Windows-version-dev/apps/api/yaya_api/services/topic_transfer.py`
- `/Volumes/LamarHD/Yaya/YAYA-pages-for-Windows-version-dev/packages/yaya-ai/README.md`

## Local Implementation

- Service package: `/Volumes/LamarHD/Yaya/yaya-ruoyi-platform/yaya-ai-service`
- FastAPI entrypoint: `app.main:app`
- Internal auth dependency: `app.auth.require_internal_auth`
- Evaluation task router: `app.evaluations.router`
- Legacy AI source package copied into the service as `yaya_ai`; provider SDK
  dependencies remain under the optional `ai` extra so the internal contract can
  be tested without model credentials.
- Current Task 9 implementation accepts evaluation tasks and stores their
  `PENDING` state in memory. Java orchestration will own durable task rows and
  result polling in the next migration task.
