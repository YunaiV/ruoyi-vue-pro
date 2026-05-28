"""Content operations: Correction · Upgrade · Extend (async)."""
from __future__ import annotations

import json
from dataclasses import dataclass, field


@dataclass
class ErrorSpan:
    text: str
    type: str
    severity: str
    fix: str
    why: str


@dataclass
class CorrectionResult:
    error_spans: list[ErrorSpan]
    summary: str
    raw: dict = field(default_factory=dict, repr=False)

    def to_dict(self) -> dict:
        return {
            "error_spans": [
                {
                    "text": e.text,
                    "type": e.type,
                    "severity": e.severity,
                    "fix": e.fix,
                    "why": e.why,
                }
                for e in self.error_spans
            ],
            "summary": self.summary,
        }


@dataclass
class UpgradePair:
    original: str
    upgraded: str
    reason: str


@dataclass
class UpgradeResult:
    pairs: list[UpgradePair]
    summary: str
    raw: dict = field(default_factory=dict, repr=False)

    def to_dict(self) -> dict:
        return {
            "pairs": [
                {"original": p.original, "upgraded": p.upgraded, "reason": p.reason}
                for p in self.pairs
            ],
            "summary": self.summary,
        }


@dataclass
class ExtendResult:
    original_kept: str
    extension: str
    word_count_added: int
    summary: str
    raw: dict = field(default_factory=dict, repr=False)

    def to_dict(self) -> dict:
        return {
            "original_kept": self.original_kept,
            "extension": self.extension,
            "word_count_added": self.word_count_added,
            "summary": self.summary,
        }


_BANLIST = (
    "furthermore, moreover, in addition, in conclusion, henceforth, thus, "
    "therefore, plethora, myriad, utilize, utilization, endeavour, endeavor, "
    "facilitate, ascertain, commence, terminate, paramount, quintessential, "
    "aforementioned, in light of, with regard to, notwithstanding"
)


def _strict_json(raw: str) -> dict:
    s, e = raw.find("{"), raw.rfind("}") + 1
    if s < 0 or e <= s:
        raise ValueError(f"No JSON object in LLM output: {raw[:200]}")
    return json.loads(raw[s:e])


class Correctioner:
    def __init__(self, llm):
        self.llm = llm

    async def run(self, transcript: str, topic: str = "") -> CorrectionResult:
        prompt = f"""You are an IELTS speaking teacher reviewing a student transcript for ERRORS.

Topic: {topic or '(unknown)'}
Transcript:
\"\"\"
{transcript}
\"\"\"

# Rules

- Flag clear errors only. Spoken-English habits ("kinda", "wanna", contractions,
  starting with "and"/"so", trailing "you know") are FINE — this is speech, not essay.
- For each flagged span:
    - "text": EXACT substring from the transcript above. Must appear verbatim
              (same casing, same words). Pick a span 1-6 words long.
    - "type": one of "grammar" | "word" | "awkward" | "filler"
    - "severity": "red"   = definitely wrong (e.g. wrong tense, missing subject)
                  "yellow" = suspicious / awkward but understandable
    - "fix": short replacement (1-6 words)
    - "why": one Chinese sentence explaining why
- Excessive fillers ("um", "uh", "like" repeated) → severity yellow, type filler.
- Cap at the 10 most useful flags. Quality over quantity.

# Output strict JSON, NO markdown:

{{
  "errors": [
    {{"text": "is went", "type": "grammar", "severity": "red",
      "fix": "went", "why": "is + went 时态混乱，过去时直接用 went"}},
    {{"text": "very very", "type": "awkward", "severity": "yellow",
      "fix": "really", "why": "重复 very，口语里改一个 really 更自然"}}
  ],
  "summary": "中文一句话总览（错了哪几类）"
}}"""
        raw = await self.llm.chat(prompt)
        data = _strict_json(raw)
        spans = [
            ErrorSpan(
                text=str(e.get("text", "")),
                type=str(e.get("type", "awkward")),
                severity=str(e.get("severity", "yellow")),
                fix=str(e.get("fix", "")),
                why=str(e.get("why", "")),
            )
            for e in data.get("errors", [])
            if e.get("text")
        ]
        return CorrectionResult(
            error_spans=spans,
            summary=str(data.get("summary", "")),
            raw=data,
        )


class Upgrader:
    def __init__(self, llm):
        self.llm = llm

    async def run(self, transcript: str, topic: str = "") -> UpgradeResult:
        prompt = f"""You are an IELTS speaking coach. Suggest VOCABULARY UPGRADES for this transcript.

Topic: {topic or '(unknown)'}
Transcript:
\"\"\"
{transcript}
\"\"\"

# Rules

- Replace weak / repetitive wording with more vivid spoken alternatives.
- Each pair MUST be 1-6 words on each side, swappable in-place.
- Replacement MUST sound like real speech — what a fluent speaker would say
  out loud. Idiomatic phrases ("blew me away", "out of the blue", "ended up",
  "kind of dawned on me", "to be honest") are great.
- DO NOT use academic / essay vocab. Banlist (forbidden):
  {_BANLIST}.
- Do NOT change content, structure, or order — vocabulary only.
- 8-15 pairs. Skip if there's nothing weak to upgrade.
- "original" must appear in the transcript verbatim (so the UI can locate it).

# Output strict JSON, NO markdown:

{{
  "pairs": [
    {{"original": "really good", "upgraded": "absolutely brilliant", "reason": "更生动，仍是日常口语"}},
    {{"original": "I went there", "upgraded": "I ended up going there", "reason": "ended up 是高频口语"}}
  ],
  "summary": "中文一句话总览"
}}"""
        raw = await self.llm.chat(prompt)
        data = _strict_json(raw)
        pairs = [
            UpgradePair(
                original=str(p.get("original", "")),
                upgraded=str(p.get("upgraded", "")),
                reason=str(p.get("reason", "")),
            )
            for p in data.get("pairs", [])
            if p.get("original") and p.get("upgraded")
        ]
        return UpgradeResult(
            pairs=pairs,
            summary=str(data.get("summary", "")),
            raw=data,
        )


class Extender:
    def __init__(self, llm):
        self.llm = llm

    async def run(self, transcript: str, topic: str = "") -> ExtendResult:
        prompt = f"""You are an IELTS speaking coach. EXTEND this Part 2 response with more content.

Topic: {topic or '(unknown)'}
Original transcript (this MUST be kept verbatim — do not modify it):
\"\"\"
{transcript}
\"\"\"

# Rules

- KEEP the student's original answer 100% intact. You are only producing what
  comes AFTER it. Do not paraphrase or "clean up" the original.
- Produce 60-90 NEW words that flow naturally from the last sentence.
- Spoken English. Contractions, natural connectors ("and another thing",
  "looking back", "to be honest", "actually", "the other thing was").
- DO NOT use academic vocab. Banlist (forbidden):
  {_BANLIST}.
- The extension must feel like the SAME speaker continuing — same voice,
  same level of formality, no shift to fancy diction.
- Add concrete things: a specific detail, a feeling, a comparison, a
  reflection. NOT a generic philosophical wrap-up like "in conclusion,
  this experience taught me...".

# Output strict JSON, NO markdown:

{{
  "extension": "And honestly, looking back on it, I still remember ... (60-90 words)",
  "word_count_added": 78,
  "summary": "中文一句话：补充了 X、Y"
}}"""
        raw = await self.llm.chat(prompt)
        data = _strict_json(raw)
        ext = str(data.get("extension", "")).strip()
        try:
            wc = int(data.get("word_count_added") or 0)
        except (TypeError, ValueError):
            wc = 0
        if wc <= 0:
            wc = len(ext.split())
        return ExtendResult(
            original_kept=transcript.strip(),
            extension=ext,
            word_count_added=wc,
            summary=str(data.get("summary", "")),
            raw=data,
        )
