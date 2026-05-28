"""Compute objective fluency metrics from Whisper word timestamps.

Fed into the FC prompt of IELTSScorer so the LLM's Fluency & Coherence score is
grounded in real acoustic signals rather than inferred from a cleaned-up
transcript.

Phase 1 note: OpenAIWhisperSTT does not currently populate WordTiming, so
callers pass ``fluency=None`` to the scorer. fluency_metrics is migrated now
so the scorer's import works and so a future STT (faster-whisper / Deepgram)
can plug in without code changes.
"""
from __future__ import annotations

import re
from dataclasses import dataclass

from ..stt.base import WordTiming


FILLERS = {
    "um", "uh", "er", "erm", "ehm", "hmm", "mm",
    "like", "actually", "basically", "literally",
    "you", "know",
    "kinda", "kind",
    "sort",
    "i", "mean",
    "well",
    "right",
}
FILLER_BIGRAMS = {
    ("you", "know"), ("i", "mean"), ("kind", "of"), ("sort", "of"),
    ("you", "see"),
}
HESITATION_SOUNDS = {"um", "uh", "er", "erm", "ehm", "hmm", "mm"}

PAUSE_THRESHOLD = 0.6


@dataclass
class FluencyMetrics:
    total_words: int
    speech_seconds: float
    active_seconds: float
    speech_rate_wpm: float
    active_speech_rate_wpm: float
    pause_count: int
    pause_total_seconds: float
    longest_pause: float
    filler_count: int
    filler_density: float
    hesitation_count: int
    mean_word_confidence: float

    def as_prompt_block(self) -> str:
        return (
            f"  total_words={self.total_words}  "
            f"speech_seconds={self.speech_seconds:.1f}s\n"
            f"  speech_rate={self.speech_rate_wpm:.0f} WPM  "
            f"(active {self.active_speech_rate_wpm:.0f} WPM)\n"
            f"  pauses≥{PAUSE_THRESHOLD}s: count={self.pause_count}  "
            f"total={self.pause_total_seconds:.1f}s  "
            f"longest={self.longest_pause:.1f}s\n"
            f"  filler_count={self.filler_count} "
            f"({self.filler_density:.1f}/100w)  "
            f"hesitations(um/uh)={self.hesitation_count}\n"
            f"  whisper_mean_conf={self.mean_word_confidence:.2f}"
        )


def _normalise(w: str) -> str:
    return re.sub(r"[^a-z]", "", w.lower())


def compute_fluency_metrics(
    words: list[WordTiming],
    audio_duration: float | None = None,
) -> FluencyMetrics:
    if not words:
        return FluencyMetrics(
            total_words=0, speech_seconds=audio_duration or 0.0,
            active_seconds=0.0, speech_rate_wpm=0.0, active_speech_rate_wpm=0.0,
            pause_count=0, pause_total_seconds=0.0, longest_pause=0.0,
            filler_count=0, filler_density=0.0, hesitation_count=0,
            mean_word_confidence=0.0,
        )

    span_start = words[0].start
    span_end = words[-1].end
    speech_seconds = float(audio_duration) if audio_duration else max(span_end - span_start, 0.01)

    active_seconds = sum(max(w.end - w.start, 0.0) for w in words)

    pauses = []
    for prev, curr in zip(words, words[1:]):
        gap = curr.start - prev.end
        if gap >= PAUSE_THRESHOLD:
            pauses.append(gap)
    pause_count = len(pauses)
    pause_total = sum(pauses)
    longest = max(pauses) if pauses else 0.0

    total = len(words)
    rate_overall = (total / speech_seconds) * 60.0 if speech_seconds > 0 else 0.0
    rate_active = (total / active_seconds) * 60.0 if active_seconds > 0 else 0.0

    norms = [_normalise(w.word) for w in words]
    hesitation = sum(1 for n in norms if n in HESITATION_SOUNDS)

    filler = 0
    i = 0
    while i < len(norms):
        n = norms[i]
        if i + 1 < len(norms) and (n, norms[i + 1]) in FILLER_BIGRAMS:
            filler += 1
            i += 2
            continue
        if n in HESITATION_SOUNDS:
            filler += 1
        elif n in {"actually", "basically", "literally", "well", "like"}:
            filler += 1
        i += 1

    density = (filler / total) * 100.0 if total else 0.0
    mean_conf = sum(w.probability for w in words) / total if total else 0.0

    return FluencyMetrics(
        total_words=total,
        speech_seconds=speech_seconds,
        active_seconds=active_seconds,
        speech_rate_wpm=rate_overall,
        active_speech_rate_wpm=rate_active,
        pause_count=pause_count,
        pause_total_seconds=pause_total,
        longest_pause=longest,
        filler_count=filler,
        filler_density=density,
        hesitation_count=hesitation,
        mean_word_confidence=mean_conf,
    )
