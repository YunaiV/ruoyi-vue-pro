"""OpenAI GPT-4o-audio multimodal client.

Used by the B-route ``AudioOmniScorer``. The model accepts audio inline (as
base64) and emits text. Per D-102, the real OpenAI API key drives this path
(distinct from Anthropic via lanyiapi which serves the text scorer).
"""
from __future__ import annotations

from pathlib import Path

from openai import AsyncOpenAI

from .._audio import encode_audio_inline, ensure_format
from .._retry import with_retry
from .base import BaseLLM


class OpenAIAudioLLM(BaseLLM):
    """OpenAI gpt-4o-audio-preview wrapper.

    Args:
        api_key:    real OpenAI key (no env reads).
        model:      defaults to ``gpt-4o-audio-preview``.
        timeout:    per-request seconds.
        max_retries: tenacity attempts.
    """

    def __init__(
        self,
        *,
        api_key: str,
        model: str = "gpt-4o-audio-preview",
        timeout: float = 120.0,
        max_retries: int = 3,
    ):
        if not api_key:
            raise ValueError("OpenAIAudioLLM requires api_key")
        self.client = AsyncOpenAI(api_key=api_key, timeout=timeout, max_retries=0)
        self.model = model
        self._chat_audio = with_retry(
            max_attempts=max_retries, provider="openai-audio"
        )(self._chat_with_audio_raw)

    async def chat(self, prompt: str) -> str:
        """gpt-4o-audio-preview also handles plain text prompts."""
        msg = await self.client.chat.completions.create(
            model=self.model,
            messages=[{"role": "user", "content": prompt}],
            modalities=["text"],
        )
        return msg.choices[0].message.content or ""

    async def _chat_with_audio_raw(
        self,
        prompt: str,
        audio_path: str | Path,
    ) -> str:
        # gpt-4o-audio-preview accepts mp3 / wav inline; convert m4a/etc.
        with ensure_format(audio_path, target_ext=".mp3") as ready_path:
            data_b64, fmt = encode_audio_inline(ready_path)

        completion = await self.client.chat.completions.create(
            model=self.model,
            modalities=["text"],
            messages=[
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": prompt},
                        {
                            "type": "input_audio",
                            "input_audio": {"data": data_b64, "format": fmt},
                        },
                    ],
                }
            ],
        )
        return completion.choices[0].message.content or ""

    async def chat_with_audio(self, prompt: str, audio_path: str | Path) -> str:
        return await self._chat_audio(prompt, audio_path)
