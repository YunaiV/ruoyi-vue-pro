"""FastAPI entrypoint for the standalone Yaya AI service."""

from __future__ import annotations

from fastapi import FastAPI

from app.evaluations import router as evaluations_router


app = FastAPI(title="Yaya AI Service", version="0.1.0")


@app.get("/internal/health")
async def health() -> dict[str, bool | str]:
    return {"ok": True, "service": "yaya-ai-service"}


app.include_router(evaluations_router)
