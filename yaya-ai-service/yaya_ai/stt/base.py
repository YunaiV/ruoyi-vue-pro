"""Abstract base class for STT services."""
from __future__ import annotations

from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from pathlib import Path


@dataclass
class WordTiming:
    word: str
    start: float
    end: float
    probability: float = 1.0


@dataclass
class STTResult:
    text: str
    language: str | None = None
    confidence: float | None = None
    provider: str = ""
    words: list[WordTiming] = field(default_factory=list)
    audio_duration: float | None = None


class BaseSTT(ABC):
    """All STT providers implement this async interface."""

    @abstractmethod
    async def transcribe(
        self,
        audio_path: str | Path,
        language: str | None = None,
        prompt: str | None = None,
    ) -> STTResult:
        """Transcribe an audio file.

        Providers that can return word-level timings should populate
        ``STTResult.words``; callers that don't need them simply ignore the field.

        ``language`` (optional) hints / forces a specific language. Most callers
        should leave it None to allow auto-detection (D-107: multilingual is a
        product feature).
        ``prompt`` (optional) is a soft context hint passed to the provider
        when supported. ``None`` means "use provider default"; ``""`` means
        "no prompt".
        """
        ...
