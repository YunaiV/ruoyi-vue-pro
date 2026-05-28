"""Chunk-based improvement — async, RAG-backed, hallucination-proof.

The flow is:
  1. Split the transcript into sentences.
  2. For each substantive sentence, ask a ``ChunkSearcher`` for top-K
     candidates from the chunks DB (typically pgvector ANN).
  3. Hand the LLM the transcript + a flat list of unique candidates with
     their chunk_ids.
  4. The LLM picks at most ``max_suggestions`` replacements, ALWAYS quoting
     the chunk_id so we can verify the suggestion is grounded.
  5. We post-validate each suggestion: keep only those whose chunk_id we
     actually showed the LLM. Anything else is hallucination → drop.

Per D-107 the prompt also tells the LLM to prioritize Chinese fragments
in the transcript: those are the highest-value upgrade points (we already
told the speaker code-switching is fine; the next step is suggesting an
English chunk that would have closed that gap).
"""
from __future__ import annotations

import json
import re
from typing import Protocol

from ..llm.base import BaseLLM
from .types import ChunkCandidate, ChunkSuggestion, ImprovementReport


class ChunkSearcher(Protocol):
    """Adapt any backing store (pgvector, in-memory, etc.) to a uniform API."""

    async def search(
        self, query: str, *, top_k: int = 4
    ) -> list[ChunkCandidate]:  # pragma: no cover — Protocol
        ...


_SENT_SPLIT_RE = re.compile(r"(?<=[.!?,，。!?])\s+|(?<=[.!?])$")
_MIN_SENTENCE_WORDS = 4
_DEFAULT_PER_SENTENCE_K = 4


class ChunkImprover:
    """Generate concrete chunk-level upgrade suggestions for a transcript."""

    def __init__(
        self,
        *,
        searcher: ChunkSearcher,
        llm: BaseLLM,
        per_sentence_k: int = _DEFAULT_PER_SENTENCE_K,
    ):
        self.searcher = searcher
        self.llm = llm
        self.per_sentence_k = per_sentence_k

    async def improve(
        self,
        transcript: str,
        topic: str,
        max_suggestions: int = 5,
    ) -> ImprovementReport:
        sentences = _split_sentences(transcript)
        # Search per substantive sentence; deduplicate candidates by chunk_id.
        seen: dict[str, ChunkCandidate] = {}
        sentence_to_candidates: list[tuple[str, list[ChunkCandidate]]] = []
        searched = 0
        for sent in sentences:
            words = sent.split()
            if len(words) < _MIN_SENTENCE_WORDS:
                continue
            searched += 1
            try:
                hits = await self.searcher.search(sent, top_k=self.per_sentence_k)
            except Exception:
                hits = []
            for h in hits:
                seen.setdefault(h.id, h)
            sentence_to_candidates.append((sent, hits))

        candidates = list(seen.values())
        if not candidates:
            return ImprovementReport(
                suggestions=[],
                rejected_count=0,
                candidate_count=0,
                sentences_searched=searched,
            )

        prompt = _build_prompt(
            transcript=transcript,
            topic=topic,
            sentence_pairs=sentence_to_candidates,
            max_suggestions=max_suggestions,
        )
        raw = await self.llm.chat(prompt)
        picks = _safe_parse_picks(raw)

        kept: list[ChunkSuggestion] = []
        rejected = 0
        seen_pick_ids: set[str] = set()
        for p in picks:
            cid = (p.get("chunk_id") or "").strip()
            if not cid or cid not in seen:
                rejected += 1
                continue
            if cid in seen_pick_ids:
                continue  # dedup if LLM repeats
            seen_pick_ids.add(cid)
            cand = seen[cid]
            kept.append(
                ChunkSuggestion(
                    chunk_id=cid,
                    original_phrase=(p.get("original_phrase") or "").strip(),
                    suggested_chunk=cand.text,
                    chunk_translation=cand.translation,
                    chunk_categories=cand.categories,
                    reason=(p.get("reason") or "").strip(),
                    sentence_context=(p.get("sentence_context") or "").strip(),
                )
            )
            if len(kept) >= max_suggestions:
                break

        return ImprovementReport(
            suggestions=kept,
            rejected_count=rejected,
            candidate_count=len(candidates),
            sentences_searched=searched,
            raw_response=raw,
        )


# ── Prompt + parsing helpers ───────────────────────────────────────────


def _build_prompt(
    *,
    transcript: str,
    topic: str,
    sentence_pairs: list[tuple[str, list[ChunkCandidate]]],
    max_suggestions: int,
) -> str:
    cand_block_lines: list[str] = []
    for i, (sent, hits) in enumerate(sentence_pairs, 1):
        cand_block_lines.append(f"Sentence {i}: \"{sent}\"")
        if not hits:
            cand_block_lines.append("  (no candidates retrieved)")
            continue
        for h in hits:
            cats = "/".join(h.categories) if h.categories else "—"
            cand_block_lines.append(
                f'  - id={h.id}  text="{h.text}"  zh="{h.translation}"  '
                f'category={cats}  similarity={h.similarity:.2f}'
            )
    candidates_block = "\n".join(cand_block_lines)

    return f"""You are an IELTS speaking coach. Your job is to suggest specific, surgical
upgrades to the learner's answer by replacing weak phrases with concrete
high-band chunks from a curated knowledge base.

## Hard rules

1. You MUST only choose chunks from the candidates listed below. Each
   candidate has an `id=...` — your output MUST include that exact id for
   every suggestion. If a chunk is not in this list, do NOT suggest it.
2. Pick at most {max_suggestions} suggestions, ranked by impact.
3. Quote the learner's exact wording in `original_phrase` (verbatim from
   the transcript).
4. Only suggest a replacement if it genuinely fits the sentence naturally;
   do not force it.
5. If the learner used a Chinese fragment (code-switching), and one of the
   candidate chunks would close that exact gap, prefer that suggestion —
   that is the highest-value upgrade.

## Topic
{topic}

## Full transcript
{transcript}

## Candidate chunks (per sentence)

{candidates_block}

## Output

Respond ONLY with valid JSON (no markdown fence, no prose):
{{
  "suggestions": [
    {{
      "chunk_id": "exact id from the candidates above",
      "original_phrase": "exact phrase quoted from the transcript",
      "reason": "one sentence explaining why this replacement raises the band",
      "sentence_context": "the full sentence the original_phrase appears in"
    }}
  ]
}}"""


def _split_sentences(text: str) -> list[str]:
    """Sentence splitter friendly to spoken English with code-switched zh."""
    if not text:
        return []
    parts = _SENT_SPLIT_RE.split(text)
    return [p.strip() for p in parts if p and p.strip()]


def _safe_parse_picks(raw: str) -> list[dict]:
    """Extract the suggestions list from any reasonable LLM response."""
    if not raw:
        return []
    start = raw.find("{")
    end = raw.rfind("}") + 1
    if start < 0 or end <= start:
        return []
    try:
        data = json.loads(raw[start:end])
    except json.JSONDecodeError:
        return []
    items = data.get("suggestions") if isinstance(data, dict) else None
    return items if isinstance(items, list) else []
