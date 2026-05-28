"""IELTS speaking scorer (A route — text-based) — async.

Given a transcript (+ optional fluency metrics + optional RAG knowledge base),
returns a 4-dim IELTS score with per-dim feedback. The Pronunciation dimension
is flagged as a text-based estimate; real pronunciation lives in scorer C
(Phase 3, Azure / SpeechAce).
"""
from __future__ import annotations

import json
from dataclasses import dataclass, field
from typing import TYPE_CHECKING

from ..llm.base import BaseLLM
from .fluency_metrics import FluencyMetrics

if TYPE_CHECKING:
    from ..rag.knowledge_base import KnowledgeBase  # Phase 4 only


# ── Few-shot calibration anchors ─────────────────────────────────────────

FEW_SHOT_EXAMPLES = [
    {
        "topic": "Describe a time you helped someone",
        "transcript": (
            "Um, I think, last year I helped my friend when she was sick. "
            "I brought her some food and we talked. It was good. "
            "She was very happy. I like helping people."
        ),
        "scores": {"FC": 5.0, "LR": 4.5, "GRA": 5.0, "P": 6.0},
        "rationale": (
            "FC: frequent hesitations ('um', 'I think'), short sentences with little development. "
            "LR: repetitive simple vocabulary ('good', 'very happy'), no advanced expressions. "
            "GRA: mostly simple clauses, no complex structures. "
            "P: generally clear but monotone."
        ),
    },
    {
        "topic": "Describe a time you helped someone",
        "transcript": (
            "There's actually a really vivid memory that comes to mind. "
            "My close friend was going through a particularly rough patch last winter — "
            "she'd been struggling with exam pressure and feeling quite isolated. "
            "What I did was set aside a whole afternoon and just showed up at her door, "
            "you know, without making a big deal of it. "
            "We ended up having this long, heartfelt conversation, and I could see her gradually "
            "opening up. Looking back on it now, I think what made the biggest difference wasn't "
            "the practical help — it was just being present."
        ),
        "scores": {"FC": 7.5, "LR": 7.0, "GRA": 7.0, "P": 7.0},
        "rationale": (
            "FC: smooth delivery, good discourse markers, well-developed narrative. "
            "LR: varied vocab ('vivid', 'rough patch', 'heartfelt'), natural idioms. "
            "GRA: mix of complex structures, conditionals, participle clauses. "
            "P: natural intonation and rhythm."
        ),
    },
]


@dataclass
class IELTSScore:
    FC: float
    LR: float
    GRA: float
    P: float
    overall: float
    fc_feedback: str
    lr_feedback: str
    gra_feedback: str
    p_feedback: str
    summary: str
    source: str = "text"
    p_is_estimate: bool = False
    raw: dict = field(default_factory=dict, repr=False)

    def __str__(self) -> str:
        p_tag = " (estimate)" if self.p_is_estimate else ""
        return (
            f"[{self.source}] Overall: {self.overall:.1f}\n"
            f"  FC  {self.FC:.1f}: {self.fc_feedback}\n"
            f"  LR  {self.LR:.1f}: {self.lr_feedback}\n"
            f"  GRA {self.GRA:.1f}: {self.gra_feedback}\n"
            f"  P   {self.P:.1f}{p_tag}: {self.p_feedback}\n"
            f"Summary: {self.summary}"
        )


class IELTSScorer:
    """Text-based IELTS scorer (A route)."""

    def __init__(self, llm: BaseLLM, kb: "KnowledgeBase | None" = None):
        self.llm = llm
        self.kb = kb

    async def score(
        self,
        transcript: str,
        topic: str,
        fluency: FluencyMetrics | None = None,
    ) -> IELTSScore:
        chunk_context = ""
        if self.kb is not None:
            try:
                relevant = self.kb.search_chunks(topic + " " + transcript[:200], top_k=6)
                chunk_context = self.kb.format_chunks_for_prompt(relevant)
            except Exception:
                chunk_context = ""

        prompt = _build_scoring_prompt(transcript, topic, chunk_context, fluency)
        raw = await self.llm.chat(prompt)
        score = _parse_score(raw)
        score.source = "text"
        score.p_is_estimate = True
        return score


def _build_scoring_prompt(
    transcript: str,
    topic: str,
    chunk_context: str,
    fluency: FluencyMetrics | None,
) -> str:
    few_shot_text = ""
    for ex in FEW_SHOT_EXAMPLES:
        few_shot_text += f"""
Example topic: {ex['topic']}
Transcript: {ex['transcript']}
Scores: FC={ex['scores']['FC']}, LR={ex['scores']['LR']}, GRA={ex['scores']['GRA']}, P={ex['scores']['P']}
Rationale: {ex['rationale']}
---"""

    fluency_block = ""
    if fluency is not None:
        fluency_block = (
            "\n## Objective acoustic fluency metrics (from Whisper word timestamps)\n"
            "**Use these numbers directly when scoring FC — do not guess from transcript alone.**\n"
            "Rough anchors: 140-170 WPM natural; <120 halting; >200 rushed. "
            "Pause>2s concerning. Filler density <3/100w fluent; >8/100w halting.\n"
            f"{fluency.as_prompt_block()}\n"
        )

    kb_block = (
        f"\n## Relevant high-score expressions (from knowledge base)\n{chunk_context}\n"
        if chunk_context
        else ""
    )

    return f"""You are an experienced IELTS speaking examiner. Score the following response on the official 4-dim rubric.

## IELTS Band Descriptors (half-band increments: 4.0, 4.5, 5.0, 5.5, ..., 9.0)

- FC  Fluency & Coherence: pace, hesitation, discourse markers, topic development, coherence.
- LR  Lexical Resource: vocab range, idiomatic language, paraphrase ability, precision.
- GRA Grammatical Range & Accuracy: variety of structures, complex sentences, error frequency.
- P   Pronunciation: phoneme accuracy, intonation, connected speech. (Note: you are scoring
      from a transcript — you CANNOT hear the audio. Base P on transcript cues only
      (filler words, length, apparent rhythm from punctuation); do not over-claim certainty.
      Prefer conservative mid-band (5.5-7.0) unless the transcript strongly suggests otherwise.)

## Multilingual transcript handling (IMPORTANT)

The transcript may contain Chinese (Mandarin) fragments inserted by a speaker
who briefly switched languages when stuck. **This is a deliberate coping
strategy in our product, not a defect.** Score guidance:

- Do NOT auto-fail or heavily penalize a response just because Chinese
  appears. Chinese fragments are an honest disclosure of where the speaker
  ran out of English vocabulary.
- Score the English portions on their own merit using the rubric above.
- For LR specifically: note in `lr_feedback` which Chinese fragment(s)
  could be upgraded to English, and suggest a high-score English
  alternative for each (e.g. 中文"印象深刻" → "really stuck with me /
  left a lasting impression"). Mention 2-3 such upgrades when applicable.
- If the ENTIRE transcript is Chinese with no English attempt at all, that
  is a different case — treat it as an unscoreable / very-low-band response
  and explain in `summary` that the speaker did not attempt English.
- If most of the transcript appears to be a Chinese MISTRANSCRIPTION of
  English audio (a common Whisper failure: short or accented English audio
  gets translated to Chinese), say so in `summary` and decline to assign a
  reliable band — set all dims to 0.0 and explain the transcript appears
  to be a transcription artifact rather than the speaker's actual answer.
{fluency_block}{kb_block}
## Few-shot calibration examples
{few_shot_text}

## Now score this response
Topic: {topic}
Transcript: {transcript}

Respond ONLY with valid JSON (no markdown fence, no prose):
{{
  "FC": 6.0,
  "LR": 5.5,
  "GRA": 6.0,
  "P": 6.0,
  "overall": 5.5,
  "fc_feedback": "concrete 1-2 sentence feedback with transcript-grounded examples",
  "lr_feedback": "...",
  "gra_feedback": "...",
  "p_feedback": "...",
  "summary": "one sentence overall gist in Chinese or English"
}}"""


def _parse_score(raw: str) -> IELTSScore:
    start = raw.find("{")
    end = raw.rfind("}") + 1
    data = json.loads(raw[start:end])
    dims = [data["FC"], data["LR"], data["GRA"], data["P"]]
    overall = data.get("overall") or round(sum(dims) / 4 * 2) / 2
    return IELTSScore(
        FC=data["FC"], LR=data["LR"], GRA=data["GRA"], P=data["P"],
        overall=overall,
        fc_feedback=data.get("fc_feedback", ""),
        lr_feedback=data.get("lr_feedback", ""),
        gra_feedback=data.get("gra_feedback", ""),
        p_feedback=data.get("p_feedback", ""),
        summary=data.get("summary", ""),
        raw=data,
    )
