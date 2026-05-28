"""Anthropic Claude (or Anthropic-protocol-compatible proxy like lanyiapi).

Required constructor args (no env reads here — yaya_api.config injects):
    api_key:    Anthropic-protocol API key
    model:      e.g. claude-sonnet-4-5-20250929
Optional:
    base_url:   custom proxy endpoint (e.g. https://lanyiapi.com)
    timeout:    per-request seconds
    max_tokens: response cap
    temperature: deterministic for scoring (0.0 default)
"""
from __future__ import annotations

from pathlib import Path

from anthropic import AsyncAnthropic

from .._retry import with_retry
from .base import BaseLLM


class AnthropicLLM(BaseLLM):
    def __init__(
        self,
        *,
        api_key: str,
        model: str,
        base_url: str | None = None,
        timeout: float = 60.0,
        max_tokens: int = 4000,
        temperature: float = 0.0,
        max_retries: int = 3,
    ):
        if not api_key:
            raise ValueError("AnthropicLLM requires api_key")
        if not model:
            raise ValueError("AnthropicLLM requires model")
        self.client = AsyncAnthropic(
            api_key=api_key,
            base_url=base_url,
            timeout=timeout,
            # Disable Anthropic SDK's internal retries; we run our own via
            # tenacity so we can classify errors uniformly.
            max_retries=0,
        )
        self.model = model
        self.max_tokens = max_tokens
        self.temperature = temperature
        self._chat = with_retry(max_attempts=max_retries, provider="anthropic")(
            self._chat_raw
        )

    async def _chat_raw(self, prompt: str) -> str:
        msg = await self.client.messages.create(
            model=self.model,
            max_tokens=self.max_tokens,
            temperature=self.temperature,
            messages=[{"role": "user", "content": prompt}],
        )
        return msg.content[0].text

    async def chat(self, prompt: str) -> str:
        return await self._chat(prompt)

    async def chat_with_audio(self, prompt: str, audio_path: str | Path) -> str:
        # Claude does not natively accept audio via Messages API.
        # Audio scoring (B route) uses GPT-4o or Gemini in Phase 3.
        raise NotImplementedError(
            "AnthropicLLM does not support audio input; use OpenAI GPT-4o or Gemini."
        )
