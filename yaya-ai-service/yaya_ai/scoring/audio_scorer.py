"""IELTS speaking scorer (B route) — multimodal, LLM listens to raw audio.

Per D-102, this uses OpenAI ``gpt-4o-audio-preview`` (real OpenAI). The
text route (A) uses Claude via lanyiapi; this audio route uses real OpenAI
because Claude does not natively accept audio input via the Messages API.

Per D-107 the prompt explicitly handles multilingual audio (speakers may
switch to Chinese fragments when stuck — code-switching, not failure).
"""
from __future__ import annotations

import json
from pathlib import Path

from ..llm.base import BaseLLM
from .ielts_scorer import IELTSScore


class AudioOmniScorer:
    """Send raw audio + rubric prompt to a multimodal LLM, parse 4-dim score."""

    def __init__(self, llm: BaseLLM):
        self.llm = llm

    async def score(
        self,
        audio_path: str | Path,
        topic: str,
        transcript_hint: str = "",
    ) -> IELTSScore:
        prompt = _build_audio_prompt(topic, transcript_hint)
        raw = await self.llm.chat_with_audio(prompt, audio_path)
        score = _parse_score_json(raw)
        score.source = "audio"
        score.p_is_estimate = False
        return score


def _build_audio_prompt(topic: str, transcript_hint: str) -> str:
    transcript_block = (
        f"\n## Transcript (reference only — trust the audio over this)\n{transcript_hint}\n"
        if transcript_hint
        else ""
    )
    return f"""You are an experienced IELTS speaking examiner. Listen to the audio file
and score the candidate on the official 4-dim rubric. You CAN hear the audio
directly — base your Pronunciation score on what you actually hear (individual
sounds, stress, rhythm, connected speech), not on guesses from the transcript.

## IELTS Band Descriptors (half-band increments: 4.0, 4.5, ..., 9.0)
- FC  Fluency & Coherence: pace, hesitation, pauses, discourse markers, topic development.
- LR  Lexical Resource: vocab range, idiomatic language, precision, paraphrase ability.
- GRA Grammatical Range & Accuracy: complex structures, error frequency, variety.
- P   Pronunciation: phoneme accuracy, word stress, sentence stress, intonation,
      connected speech, L1 interference, overall intelligibility.

## Multilingual handling (IMPORTANT)

The speaker may switch briefly to Chinese (Mandarin) when they get stuck on
an English word — this is a deliberate coping strategy in our product, not a
defect. Do NOT auto-fail or heavily penalize a response just because Chinese
appears. Score the English portions on their own merit. For LR, in your
``lr_feedback`` note 1-2 specific Chinese fragments that could be upgraded
to high-band English (e.g. 中文"印象深刻" → "really stuck with me").
If the entire response is Chinese with no English attempt, treat it as
unscoreable and explain in ``summary``.

## Topic
{topic}
{transcript_block}
## Scoring guidance
- Be calibrated, not generous. Most Chinese learners fall in the 5.0-6.5 range.
- Quote specific moments in your feedback when possible: e.g. "around 0:23 you
  say 'peoples'..." or "the /θ/ in 'think' is consistently realised as /s/".
- For P, list 2-3 concrete pronunciation issues you heard (phoneme-level if
  possible).
- For FC, estimate words-per-minute and count noticeable hesitations/pauses.

Respond ONLY with valid JSON (no markdown fence):
{{
  "FC": 6.0,
  "LR": 5.5,
  "GRA": 6.0,
  "P": 6.0,
  "overall": 6.0,
  "fc_feedback": "specific 1-2 sentences with timestamps when possible",
  "lr_feedback": "...",
  "gra_feedback": "...",
  "p_feedback": "2-3 concrete pronunciation observations",
  "summary": "one-sentence overall gist"
}}"""


def _parse_score_json(raw: str) -> IELTSScore:
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
