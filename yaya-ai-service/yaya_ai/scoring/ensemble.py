"""Run multiple IELTS scorers in parallel and fuse their outputs.

Per the demo's design (and PRD §4.6.3), individual route scores are kept
side-by-side in ``per_scorer`` so the UI can show disagreement; we ALSO
emit a fused ``final`` IELTSScore for callers that want one number.

Fusion strategy (inherited from demo + Phase 2 follow-up):
    FC : audio if available (weight 0.6) + text (weight 0.4); fall back to
         whichever is present.
    LR : average of A and B (C does not score lexis).
    GRA: average of A and B (C does not score grammar).
    P  : C (real phoneme assessment) > B (audio LLM) > A (text estimate).
    overall: (FC + LR + GRA + P) / 4 — computed by us, NOT taken from any
             individual scorer's "overall" field. (Phase 2 follow-up:
             LLM-emitted overall jitters slightly even at temperature=0.)

Confidence interval (PRD §4.6.3): min/max of per-route overalls. When only
one route ran, the interval collapses to a single point.
"""
from __future__ import annotations

import asyncio
from dataclasses import dataclass, field
from pathlib import Path

from .audio_scorer import AudioOmniScorer
from .fluency_metrics import FluencyMetrics
from .ielts_scorer import IELTSScore, IELTSScorer
from .pronunciation_api import PronunciationReport


@dataclass
class EnsembleResult:
    final: IELTSScore
    per_scorer: dict[str, IELTSScore] = field(default_factory=dict)
    pronunciation: PronunciationReport | None = None
    errors: dict[str, str] = field(default_factory=dict)
    band_lower: float = 0.0
    band_upper: float = 0.0


class MultiScorer:
    """Runs any subset of A / B / C concurrently."""

    def __init__(
        self,
        text_scorer: IELTSScorer | None = None,
        audio_scorer: AudioOmniScorer | None = None,
        pron_scorer=None,  # AzurePronunciationScorer | SpeechAceScorer | None
    ):
        self.text = text_scorer
        self.audio = audio_scorer
        self.pron = pron_scorer

    async def score(
        self,
        audio_path: str | Path,
        transcript: str,
        topic: str,
        fluency: FluencyMetrics | None = None,
    ) -> EnsembleResult:
        async def run_text() -> tuple[str, IELTSScore]:
            assert self.text is not None
            return "text", await self.text.score(transcript, topic, fluency=fluency)

        async def run_audio() -> tuple[str, IELTSScore]:
            assert self.audio is not None
            return "audio", await self.audio.score(
                audio_path, topic, transcript_hint=transcript
            )

        async def run_pron() -> tuple[str, PronunciationReport]:
            assert self.pron is not None
            return "pronunciation", await self.pron.score(audio_path, transcript)

        tasks = []
        if self.text is not None:
            tasks.append(run_text())
        if self.audio is not None:
            tasks.append(run_audio())
        if self.pron is not None:
            tasks.append(run_pron())

        gathered = await asyncio.gather(*tasks, return_exceptions=True)

        per_scorer: dict[str, IELTSScore] = {}
        pron_report: PronunciationReport | None = None
        errors: dict[str, str] = {}

        # Match results back to scorer names. asyncio.gather preserves order.
        index = 0
        if self.text is not None:
            _record(gathered[index], errors, per_scorer, lambda r: per_scorer.update({r[0]: r[1]}))
            index += 1
        if self.audio is not None:
            _record(gathered[index], errors, per_scorer, lambda r: per_scorer.update({r[0]: r[1]}))
            index += 1
        if self.pron is not None:
            res = gathered[index]
            if isinstance(res, BaseException):
                errors["pronunciation"] = f"{type(res).__name__}: {res}"
            else:
                pron_report = res[1]

        final = _fuse(per_scorer, pron_report)
        band_lower, band_upper = _confidence_interval(per_scorer, pron_report)
        return EnsembleResult(
            final=final,
            per_scorer=per_scorer,
            pronunciation=pron_report,
            errors=errors,
            band_lower=band_lower,
            band_upper=band_upper,
        )


def _record(result, errors, per_scorer, on_ok):
    """Sort one gather result into errors or successes (text/audio routes)."""
    if isinstance(result, BaseException):
        # Map to a scorer name from the result type — but here we know it's
        # one of the IELTSScore-returning tasks. The name was lost on failure.
        # Caller infers name from missing key.
        errors[_infer_name(result, per_scorer)] = f"{type(result).__name__}: {result}"
    else:
        on_ok(result)


def _infer_name(exc: BaseException, per_scorer: dict) -> str:
    # Best-effort: pick first not-yet-recorded slot. Caller wraps all 3 routes
    # so collisions are unlikely; this is just for log labelling.
    for name in ("text", "audio"):
        if name not in per_scorer:
            return name
    return "unknown"


def _fuse(
    per_scorer: dict[str, IELTSScore],
    pron: PronunciationReport | None,
) -> IELTSScore:
    text = per_scorer.get("text")
    audio = per_scorer.get("audio")

    # FC: audio 0.6 + text 0.4 if both, else whichever's present.
    fc = 0.0
    if audio and text:
        fc = audio.FC * 0.6 + text.FC * 0.4
    elif audio:
        fc = audio.FC
    elif text:
        fc = text.FC
    fc = round(fc * 2) / 2 if fc else 0.0

    lr = _avg([s.LR for s in per_scorer.values() if s])
    gra = _avg([s.GRA for s in per_scorer.values() if s])

    # P: pron > audio > text
    p_is_estimate = True
    if pron is not None:
        p = pron.ielts_band
        p_is_estimate = False
        p_source = "pronunciation_api"
    elif audio is not None:
        p = audio.P
        p_is_estimate = False
        p_source = "audio"
    elif text is not None:
        p = text.P
        p_source = "text"
    else:
        p = 0.0
        p_source = "none"

    # Backend computes overall to dodge LLM-side jitter (Phase 2 follow-up)
    overall = round(((fc + lr + gra + p) / 4) * 2) / 2

    def joiner(key: str) -> str:
        parts = []
        for name, s in per_scorer.items():
            v = getattr(s, key, "").strip()
            if v:
                parts.append(f"[{name}] {v}")
        return "\n".join(parts)

    p_feedback_lines: list[str] = []
    if pron is not None:
        issues = pron.top_issue_words(5)
        if issues:
            issue_str = ", ".join(f"{w.word}({w.accuracy:.0f})" for w in issues)
            p_feedback_lines.append(
                f"[{pron.provider}] accuracy={pron.accuracy:.0f} "
                f"fluency={pron.fluency:.0f}  issues: {issue_str}"
            )
        else:
            p_feedback_lines.append(
                f"[{pron.provider}] accuracy={pron.accuracy:.0f} "
                f"fluency={pron.fluency:.0f}"
            )
    if audio is not None:
        p_feedback_lines.append(f"[audio] {audio.p_feedback}")
    if text is not None and not (pron or audio):
        p_feedback_lines.append(f"[text] {text.p_feedback}")

    summaries = [s.summary for s in per_scorer.values() if s and s.summary]
    final_summary = " · ".join(summaries[:2]) or (text.summary if text else "")

    return IELTSScore(
        FC=fc,
        LR=lr,
        GRA=gra,
        P=p,
        overall=overall,
        fc_feedback=joiner("fc_feedback"),
        lr_feedback=joiner("lr_feedback"),
        gra_feedback=joiner("gra_feedback"),
        p_feedback="\n".join(p_feedback_lines),
        summary=final_summary,
        source=f"ensemble(P={p_source})",
        p_is_estimate=p_is_estimate,
        raw={
            "per_scorer": {k: v.raw for k, v in per_scorer.items()},
            "pronunciation": pron.raw if pron else None,
        },
    )


def _confidence_interval(
    per_scorer: dict[str, IELTSScore],
    pron: PronunciationReport | None,
) -> tuple[float, float]:
    """Min/max of per-route overall scores (PRD §4.6.3 simple version)."""
    overalls: list[float] = [s.overall for s in per_scorer.values() if s]
    if pron is not None:
        overalls.append(pron.ielts_band)
    if not overalls:
        return 0.0, 0.0
    return round(min(overalls) * 2) / 2, round(max(overalls) * 2) / 2


def _avg(vals: list[float]) -> float:
    return round(sum(vals) / len(vals) * 2) / 2 if vals else 0.0
