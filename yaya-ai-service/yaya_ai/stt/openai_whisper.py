"""OpenAI Whisper API STT (cloud, async)."""
from __future__ import annotations

from pathlib import Path

from openai import AsyncOpenAI
import httpx

from .._retry import with_retry
from .base import BaseSTT, STTResult


# Default soft prompt for IELTS speaking practice. Whisper uses ``prompt`` as
# a style/context hint that *also* improves language detection accuracy
# without forcing a single language. Per D-107: speakers may switch to
# Chinese fragments when stuck — this is a product feature, not a bug.
DEFAULT_PROMPT = (
    "This is an IELTS speaking practice answer in English. "
    "The speaker may occasionally switch to Chinese fragments when stuck. "
    "Transcribe each segment in its original language."
)


class OpenAIWhisperSTT(BaseSTT):
    """OpenAI hosted Whisper API.

    Args:
        api_key: real OpenAI key (required, never read from env here).
        model: defaults to ``whisper-1``.
        timeout: per-request timeout in seconds.
        prompt: optional context hint to improve detection. Pass ``""`` to
            disable; pass ``None`` to use the IELTS default.
    """

    def __init__(
        self,
        *,
        api_key: str,
        model: str = "whisper-1",
        timeout: float = 30.0,
        prompt: str | None = None,
        max_retries: int = 3,
        proxy: str | None = None,
    ):
        if not api_key:
            raise ValueError("OpenAIWhisperSTT requires api_key")
        # Disable OpenAI SDK's internal retries; tenacity handles retry uniformly.
        client_kwargs = {"api_key": api_key, "timeout": timeout, "max_retries": 0}
        if proxy:
            client_kwargs["http_client"] = httpx.AsyncClient(proxy=proxy, timeout=timeout)
        self.client = AsyncOpenAI(**client_kwargs)
        self.model = model
        self.default_prompt = DEFAULT_PROMPT if prompt is None else prompt
        self._transcribe = with_retry(
            max_attempts=max_retries, provider="openai-whisper"
        )(self._transcribe_raw)

    async def _transcribe_raw(
        self,
        audio_path: str | Path,
        language: str | None = None,
        prompt: str | None = None,
    ) -> STTResult:
        path = Path(audio_path)
        with path.open("rb") as f:
            kwargs: dict = {
                "model": self.model,
                "file": f,
                "response_format": "verbose_json",
            }
            if language:
                kwargs["language"] = language
            effective_prompt = self.default_prompt if prompt is None else prompt
            if effective_prompt:
                kwargs["prompt"] = effective_prompt
            response = await self.client.audio.transcriptions.create(**kwargs)

        return STTResult(
            text=response.text,
            language=getattr(response, "language", None),
            provider="openai-whisper",
            audio_duration=getattr(response, "duration", None),
        )

    async def transcribe(
        self,
        audio_path: str | Path,
        language: str | None = None,
        prompt: str | None = None,
    ) -> STTResult:
        return await self._transcribe(audio_path, language=language, prompt=prompt)
