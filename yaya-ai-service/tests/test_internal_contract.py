from fastapi.testclient import TestClient

from app.main import app


client = TestClient(app)


def _headers(request_id: str = "req-test-1") -> dict[str, str]:
    return {
        "X-Yaya-Internal-Key": "local-internal-key",
        "X-Yaya-Request-Id": request_id,
    }


def _payload(task_id: str = "task-1") -> dict:
    return {
        "task_id": task_id,
        "evaluation_id": f"evaluation-{task_id}",
        "recording_id": f"recording-{task_id}",
        "user_id": "10001",
        "topic_id": "part2-family-home",
        "audio": {
            "storage_path": "recordings/10001/example.webm",
            "mime_type": "audio/webm",
            "duration_seconds": 122.4,
        },
        "topic": {
            "title_en": "Describe a room you like",
            "title_zh": "描述一个你喜欢的房间",
            "prompt_en": "Describe a room you like.",
            "prompt_zh": "描述一个你喜欢的房间。",
            "cue_bullets": ["where it is", "what it looks like"],
        },
        "options": {
            "run_text_route": True,
            "run_audio_route": True,
            "run_pronunciation_route": True,
            "run_improvement": True,
        },
    }


def test_health_does_not_require_internal_headers():
    response = client.get("/internal/health")

    assert response.status_code == 200
    assert response.json() == {"ok": True, "service": "yaya-ai-service"}


def test_internal_endpoints_require_key_and_request_id():
    payload = _payload("auth-task")

    missing_request_id = client.post(
        "/internal/evaluations",
        headers={"X-Yaya-Internal-Key": "local-internal-key"},
        json=payload,
    )
    invalid_key = client.post(
        "/internal/evaluations",
        headers={
            "X-Yaya-Internal-Key": "wrong",
            "X-Yaya-Request-Id": "req-auth-task",
        },
        json=payload,
    )

    assert missing_request_id.status_code == 400
    assert invalid_key.status_code == 401


def test_create_and_get_evaluation_task_contract():
    payload = _payload("task-create-get")

    created = client.post("/internal/evaluations", headers=_headers("req-create"), json=payload)
    fetched = client.get("/internal/evaluations/task-create-get", headers=_headers("req-get"))

    assert created.status_code == 200
    assert created.json() == {
        "task_id": "task-create-get",
        "status": "PENDING",
        "accepted": True,
    }
    assert fetched.status_code == 200
    body = fetched.json()
    assert body["task_id"] == "task-create-get"
    assert body["evaluation_id"] == "evaluation-task-create-get"
    assert body["status"] == "PENDING"
    assert body["progress"] == {
        "stage": "accepted",
        "message": "evaluation accepted",
    }
    assert body["result"] is None
    assert body["error"] is None
    assert body["created_at"]
    assert body["updated_at"]


def test_create_evaluation_is_idempotent_by_task_id():
    payload = _payload("task-idempotent")

    first = client.post("/internal/evaluations", headers=_headers("req-idem-1"), json=payload)
    second = client.post("/internal/evaluations", headers=_headers("req-idem-2"), json=payload)

    assert first.status_code == 200
    assert second.status_code == 200
    assert second.json() == first.json()


def test_reusing_task_id_with_different_payload_conflicts():
    payload = _payload("task-conflict")
    changed = _payload("task-conflict")
    changed["topic_id"] = "part3-city"

    first = client.post("/internal/evaluations", headers=_headers("req-conflict-1"), json=payload)
    second = client.post("/internal/evaluations", headers=_headers("req-conflict-2"), json=changed)

    assert first.status_code == 200
    assert second.status_code == 409
