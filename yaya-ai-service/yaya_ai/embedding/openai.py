"""OpenAI embeddings (text-embedding-3-small / -large).

Default = ``text-embedding-3-small`` (1536 dim, $0.00002 / 1k tokens) — matches
the pgvector(1536) column on the ``chunks`` table.

Async + tenacity retry via the shared ``with_retry`` wrapper.
"""
from __future__ import annotations

from openai import AsyncOpenAI

from .._retry import with_retry


class OpenAIEmbeddingClient:
    def __init__(
        self,
        *,
        api_key: str,
        model: str = "text-embedding-3-small",
        timeout: float = 60.0,
        max_retries: int = 3,
    ):
        if not api_key:
            raise ValueError("OpenAIEmbeddingClient requires api_key")
        self.client = AsyncOpenAI(api_key=api_key, timeout=timeout, max_retries=0)
        self.model = model
        self._embed = with_retry(
            max_attempts=max_retries, provider="openai-embedding"
        )(self._embed_raw)

    async def _embed_raw(self, texts: list[str]) -> list[list[float]]:
        if not texts:
            return []
        resp = await self.client.embeddings.create(model=self.model, input=texts)
        # OpenAI returns items in input order
        return [d.embedding for d in resp.data]

    async def embed(self, texts: list[str]) -> list[list[float]]:
        return await self._embed(texts)
