"""STT provider factory."""
from __future__ import annotations

from typing import Any

from .base import BaseSTT
from .deepgram import DeepgramSTT
from .openai_whisper import OpenAIWhisperSTT


def get_stt(provider: str, **kwargs: Any) -> BaseSTT:
    name = (provider or "openai").strip().lower()
    if name == "deepgram":
        return DeepgramSTT(
            api_key=kwargs["api_key"],
            model=kwargs.get("model", "nova-3"),
            timeout=float(kwargs.get("timeout", 180.0)),
            max_retries=int(kwargs.get("max_retries", 3)),
        )
    if name in ("openai", "openai-whisper", "whisper"):
        return OpenAIWhisperSTT(
            api_key=kwargs["api_key"],
            model=kwargs.get("model", "whisper-1"),
            timeout=float(kwargs.get("timeout", 30.0)),
            prompt=kwargs.get("prompt"),
            max_retries=int(kwargs.get("max_retries", 3)),
            proxy=kwargs.get("proxy"),
        )
    raise ValueError(f"Unknown STT provider: {provider}")
