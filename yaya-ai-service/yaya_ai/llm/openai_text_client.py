"""OpenAI text-only chat client.

Drop-in replacement for ``AnthropicLLM`` in the text-scoring, chunk-improvement,
polish-pack, and memory-extraction paths. Wraps ``AsyncOpenAI.chat.completions``
and reuses the project's shared retry helper so transient errors are handled
the same way as the rest of the AI stack.
"""
from __future__ import annotations

from pathlib import Path

from openai import AsyncOpenAI

from .._retry import with_retry
from .base import BaseLLM


class OpenAITextLLM(BaseLLM):
    """OpenAI Chat Completions wrapper for text-only tasks.

    Args:
        api_key:    real OpenAI key (no env reads here -- yaya_api.config injects).
        model:      e.g. ``gpt-4o-mini`` or ``gpt-4o``.
        base_url:   optional override for OpenAI-compatible proxies.
        timeout:    per-request seconds.
        max_tokens: response cap.
        temperature: deterministic for scoring (0.0 default).
        max_retries: tenacity attempts.
    """

    def __init__(
        self,
        *,
        api_key: str,
        model: str,
        base_url: str | None = None,
        timeout: float = 75.0,
        max_tokens: int = 4000,
        temperature: float = 0.0,
        max_retries: int = 3,
    ):
        if not api_key:
            raise ValueError("OpenAITextLLM requires api_key")
        if not model:
            raise ValueError("OpenAITextLLM requires model")
        self.client = AsyncOpenAI(
            api_key=api_key,
            base_url=base_url,
            timeout=timeout,
            max_retries=0,
        )
        self.model = model
        self.max_tokens = max_tokens
        self.temperature = temperature
        self._chat = with_retry(max_attempts=max_retries, provider="openai-text")(
            self._chat_raw
        )

    async def _chat_raw(self, prompt: str) -> str:
        response = await self.client.chat.completions.create(
            model=self.model,
            max_tokens=self.max_tokens,
            temperature=self.temperature,
            messages=[{"role": "user", "content": prompt}],
        )
        text = response.choices[0].message.content if response.choices else ""
        if not text:
            raise RuntimeError("OpenAI text completion returned an empty response")
        return text

    async def chat(self, prompt: str) -> str:
        return await self._chat(prompt)

    async def chat_with_audio(self, prompt: str, audio_path: str | Path) -> str:
        # Text-only client. Use OpenAIAudioLLM (gpt-4o-audio) for audio prompts.
        raise NotImplementedError(
            "OpenAITextLLM does not support audio input; use OpenAIAudioLLM."
        )
