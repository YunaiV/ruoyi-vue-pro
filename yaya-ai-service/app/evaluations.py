"""Internal evaluation task contract.

Task 9 establishes the durable HTTP boundary. Actual model-heavy processing is
plugged in by later orchestration work; until then, accepted tasks are tracked
in memory and exposed through the same state contract Java will consume.
"""

from __future__ import annotations

import hashlib
import json
from datetime import UTC, datetime
from enum import StrEnum
from threading import RLock
from typing import Any

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel, ConfigDict, Field

from app.auth import require_internal_auth


class EvaluationStatus(StrEnum):
    PENDING = "PENDING"
    RUNNING = "RUNNING"
    SUCCEEDED = "SUCCEEDED"
    FAILED = "FAILED"
    CANCELLED = "CANCELLED"


class AudioPayload(BaseModel):
    storage_path: str
    mime_type: str | None = None
    duration_seconds: float | None = None


class TopicPayload(BaseModel):
    title_en: str | None = None
    title_zh: str | None = None
    prompt_en: str | None = None
    prompt_zh: str | None = None
    cue_bullets: list[str] = Field(default_factory=list)


class EvaluationOptions(BaseModel):
    run_text_route: bool = True
    run_audio_route: bool = True
    run_pronunciation_route: bool = True
    run_improvement: bool = True


class EvaluationCreateRequest(BaseModel):
    model_config = ConfigDict(extra="allow")

    task_id: str
    evaluation_id: str
    recording_id: str | None = None
    user_id: str | None = None
    topic_id: str | None = None
    audio: AudioPayload | None = None
    topic: TopicPayload | None = None
    options: EvaluationOptions = Field(default_factory=EvaluationOptions)


class EvaluationAcceptedResponse(BaseModel):
    task_id: str
    status: EvaluationStatus
    accepted: bool


class EvaluationProgress(BaseModel):
    stage: str
    message: str | None = None


class EvaluationError(BaseModel):
    code: str
    message: str


class EvaluationTaskResponse(BaseModel):
    task_id: str
    evaluation_id: str
    status: EvaluationStatus
    progress: EvaluationProgress | None = None
    result: dict[str, Any] | None = None
    error: EvaluationError | None = None
    created_at: datetime
    updated_at: datetime


class EvaluationTaskRecord(BaseModel):
    payload_hash: str
    response: EvaluationTaskResponse


class EvaluationTaskStore:
    def __init__(self) -> None:
        self._lock = RLock()
        self._records: dict[str, EvaluationTaskRecord] = {}

    def accept(self, payload: EvaluationCreateRequest) -> EvaluationTaskResponse:
        payload_hash = _hash_payload(payload)
        now = datetime.now(UTC)
        with self._lock:
            existing = self._records.get(payload.task_id)
            if existing is not None:
                if existing.payload_hash != payload_hash:
                    raise HTTPException(status_code=409, detail="task_id payload conflict")
                return existing.response

            response = EvaluationTaskResponse(
                task_id=payload.task_id,
                evaluation_id=payload.evaluation_id,
                status=EvaluationStatus.PENDING,
                progress=EvaluationProgress(stage="accepted", message="evaluation accepted"),
                result=None,
                error=None,
                created_at=now,
                updated_at=now,
            )
            self._records[payload.task_id] = EvaluationTaskRecord(
                payload_hash=payload_hash,
                response=response,
            )
            return response

    def get(self, task_id: str) -> EvaluationTaskResponse:
        with self._lock:
            record = self._records.get(task_id)
            if record is None:
                raise HTTPException(status_code=404, detail="evaluation task not found")
            return record.response


store = EvaluationTaskStore()
router = APIRouter(prefix="/internal/evaluations", dependencies=[Depends(require_internal_auth)])


@router.post("", response_model=EvaluationAcceptedResponse)
async def create_evaluation(payload: EvaluationCreateRequest) -> EvaluationAcceptedResponse:
    task = store.accept(payload)
    return EvaluationAcceptedResponse(task_id=task.task_id, status=task.status, accepted=True)


@router.get("/{task_id}", response_model=EvaluationTaskResponse)
async def get_evaluation(task_id: str) -> EvaluationTaskResponse:
    return store.get(task_id)


def _hash_payload(payload: EvaluationCreateRequest) -> str:
    encoded = json.dumps(
        payload.model_dump(mode="json", exclude_none=False, exclude_unset=False),
        sort_keys=True,
        separators=(",", ":"),
    )
    return hashlib.sha256(encoded.encode("utf-8")).hexdigest()
