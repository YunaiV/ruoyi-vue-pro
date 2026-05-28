"""Content evaluator + spoken-English rewriter (async, prototype prompts)."""
from __future__ import annotations

import json
from dataclasses import dataclass, field


@dataclass
class RewriteSegment:
    type: str
    text: str = ""
    from_text: str = ""
    to_text: str = ""
    reason: str = ""


@dataclass
class ContentRewrite:
    target_band: float
    current_band: float
    current_band_reason: str
    evaluation: dict
    segments: list[RewriteSegment]
    key_changes: list[str]
    rewritten_plain: str
    raw: dict = field(default_factory=dict, repr=False)

    def to_dict(self) -> dict:
        return {
            "target_band": self.target_band,
            "current_band": self.current_band,
            "current_band_reason": self.current_band_reason,
            "evaluation": self.evaluation,
            "segments": [
                {
                    "type": s.type,
                    "text": s.text,
                    "from_text": s.from_text,
                    "to_text": s.to_text,
                    "reason": s.reason,
                }
                for s in self.segments
            ],
            "key_changes": list(self.key_changes),
            "rewritten_plain": self.rewritten_plain,
        }


_ACADEMIC_BANLIST = (
    "furthermore, moreover, in addition, in conclusion, henceforth, thus, "
    "therefore, plethora, myriad, utilize, utilization, endeavour, endeavor, "
    "facilitate, ascertain, commence, terminate, paramount, quintessential, "
    "aforementioned, in light of, with regard to, notwithstanding"
)


class ContentRewriter:
    def __init__(self, llm):
        self.llm = llm

    async def rewrite(
        self,
        transcript: str,
        topic: str,
        target_band: float = 7.0,
    ) -> ContentRewrite:
        prompt = _build_prompt(transcript, topic, target_band)
        raw = await self.llm.chat(prompt)
        return _parse(raw, target_band)


def _build_prompt(transcript: str, topic: str, target_band: float) -> str:
    return f"""You are an IELTS speaking coach. The student gave the response below for a Part 2 task.

Task topic: {topic}
Target band: {target_band:.1f}

Student transcript:
\"\"\"
{transcript}
\"\"\"

# What you must do

1) **Score the ORIGINAL content** first (content-only band, half-band increments
   from 4.0 to 9.0 — judge relevance / depth / specificity / structure / vocab
   richness; do NOT factor in pronunciation, you can't hear it).
   Provide `current_band` (a number) and `current_band_reason` (one Chinese sentence).

2) **Evaluate the CONTENT** in detail (pronunciation/grammar scored separately):
   - relevance: does it actually address the cue card?
   - depth: are points developed with reasons / examples / feelings?
   - specificity: concrete details vs. vague generalities?
   - structure: opener → body → wrap-up logic?
   - overall_gap: in 1-2 Chinese sentences, what's missing to reach band {target_band:.1f}?

3) **Rewrite to hit band {target_band:.1f}** while:
   - Keeping the SAME core story the student told. Do NOT invent a different topic
     or replace the student's experience with a fabricated one.
   - **Sounding like real spoken English** — what a fluent speaker would actually say
     out loud in a 2-minute exam answer. Use contractions ("I've", "it's", "didn't"),
     natural fillers/connectors when appropriate ("so", "and then", "I mean", "kind of",
     "to be honest", "you know"), and discourse markers a real speaker uses.
   - **DO NOT use academic / essay vocabulary.** Hard banlist (do NOT use any of these):
     {_ACADEMIC_BANLIST}.
   - For target band {target_band:.1f}: aim for natural fluency with SOME less-common
     words and idiomatic phrases ("a bit of a", "it kind of dawned on me", "blew me away",
     "ended up", "out of the blue"), but everyday spoken — never fancy/written.
   - Length around 230-290 words (a 2-minute monologue).

4) **Express the rewrite as ordered segments** so the UI can colour-code the diff.
   Segment types:
   - "keep"    — wording from the student that already works, kept verbatim
   - "add"     — newly inserted phrase (filling a content gap; include a short reason)
   - "upgrade" — replacing weaker wording with a more natural / vivid alternative.
                 Provide BOTH "from" (student's original) and "to" (your replacement).
                 The replacement MUST still sound spoken, never academic.
   - "delete"  — wording removed (filler, redundancy, off-topic). Include a short reason.

   Order segments left-to-right as they appear in the FINAL rewrite (delete segments
   slot in where the deletion was, so the user sees what got cut in context).

5) Also produce **rewritten_plain**: the final rewritten answer as a single
   continuous string (the segments above with "delete" omitted and "upgrade"
   resolved to its "to" text). This is what the student will read aloud.

# Output strictly this JSON, NO prose, NO markdown fence:

{{
  "current_band": 5.5,
  "current_band_reason": "故事框架完整但细节单薄、词汇重复",
  "evaluation": {{
    "relevance": "...",
    "depth": "...",
    "specificity": "...",
    "structure": "...",
    "overall_gap": "..."
  }},
  "segments": [
    {{"type": "keep",    "text": "Last year I went to"}},
    {{"type": "delete",  "text": "um, like",                         "reason": "填充词"}},
    {{"type": "keep",    "text": "Tokyo for the first time."}},
    {{"type": "upgrade", "from": "It was really good",               "to": "It honestly blew me away", "reason": "更生动，仍是口语"}},
    {{"type": "add",     "text": "I mean, I'd built it up in my head for years.", "reason": "补充情感铺垫"}}
  ],
  "key_changes": [
    "保留了去东京的故事框架",
    "把 'really good' 升级成 'honestly blew me away'，更有画面感且口语",
    "删掉填充词 um/like，补一句铺垫情绪"
  ],
  "rewritten_plain": "Last year I went to Tokyo for the first time. It honestly blew me away. ..."
}}"""


def _parse(raw: str, target_band: float) -> ContentRewrite:
    start = raw.find("{")
    end = raw.rfind("}") + 1
    if start < 0 or end <= start:
        raise ValueError(f"No JSON object in LLM output: {raw[:200]}")
    data = json.loads(raw[start:end])

    segs: list[RewriteSegment] = []
    for s in data.get("segments", []):
        t = s.get("type", "keep")
        if t == "upgrade":
            segs.append(
                RewriteSegment(
                    type="upgrade",
                    from_text=str(s.get("from", "")),
                    to_text=str(s.get("to", "")),
                    reason=str(s.get("reason", "")),
                )
            )
        else:
            segs.append(
                RewriteSegment(
                    type=t,
                    text=str(s.get("text", "")),
                    reason=str(s.get("reason", "")),
                )
            )

    plain = str(data.get("rewritten_plain", "")).strip()
    if not plain:
        parts: list[str] = []
        for s in segs:
            if s.type in ("keep", "add"):
                parts.append(s.text)
            elif s.type == "upgrade":
                parts.append(s.to_text)
        plain = " ".join(p.strip() for p in parts if p.strip())

    try:
        cur = float(data.get("current_band", 0.0))
    except (TypeError, ValueError):
        cur = 0.0

    return ContentRewrite(
        target_band=target_band,
        current_band=cur,
        current_band_reason=str(data.get("current_band_reason", "")),
        evaluation=data.get("evaluation", {}),
        segments=segs,
        key_changes=list(data.get("key_changes", [])),
        rewritten_plain=plain,
        raw=data,
    )
