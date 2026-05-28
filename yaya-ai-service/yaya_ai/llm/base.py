"""LLM abstraction — all clients expose async chat()."""
from __future__ import annotations

from abc import ABC, abstractmethod
from pathlib import Path


class BaseLLM(ABC):
    @abstractmethod
    async def chat(self, prompt: str) -> str:
        """Send a text prompt, return text response."""
        ...

    async def chat_with_audio(self, prompt: str, audio_path: str | Path) -> str:
        """Send prompt + audio file. Default: not supported (override per-provider)."""
        raise NotImplementedError(
            f"{type(self).__name__} does not support audio input."
        )
