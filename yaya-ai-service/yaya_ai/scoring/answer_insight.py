"""Deep-analysis card for eval report (prototype verbatim prompts, async)."""
from __future__ import annotations

import json
from dataclasses import dataclass, field


@dataclass
class AnswerInsightResult:
    main_idea: str
    logic_structure: str
    highlights: list[str]
    raw: dict = field(default_factory=dict, repr=False)

    def to_dict(self) -> dict:
        return {
            "main_idea": self.main_idea,
            "logic_structure": self.logic_structure,
            "highlights": list(self.highlights),
        }


class AnswerInsight:
    def __init__(self, llm):
        self.llm = llm

    async def analyze(self, transcript: str, topic: str = "") -> AnswerInsightResult:
        prompt = f"""You are an IELTS speaking tutor reviewing a student's Part 2 response.

Topic: {topic or '(unknown)'}
Student transcript:
\"\"\"
{transcript}
\"\"\"

# What you must produce (Chinese)

1) **main_idea (主旨)** — one Chinese sentence (≤30 字) capturing the core
   point/feeling the student is conveying. Concrete, not generic.

2) **logic_structure (逻辑)** — the narrative skeleton in 1-2 Chinese sentences.
   Use Chinese signposts like "先 → 然后 → 最后" or "起 → 承 → 转 → 合".
   Describe what STORY-LEVEL moves the student makes, not the words used.

3) **highlights (亮点)** — 3-6 Chinese bullets. Each bullet identifies a real
   strength VISIBLE in the transcript (don't fabricate). Anchor each bullet
   to IELTS criteria: Fluency&Coherence / Lexical Resource / Grammatical
   Range&Accuracy / Pronunciation hint / Content & Development. Quote 1-3
   words from the transcript when relevant. If the answer is genuinely weak,
   it's OK to return only 2-3 bullets.

# Output strict JSON, NO markdown fence:

{{
  "main_idea": "中文一句话",
  "logic_structure": "中文一句或两句，描述叙事走向",
  "highlights": [
    "[LR] 用了 'blew me away' 等口语化表达，词汇灵活",
    "[FC] 时间线连贯，'first ... then ... eventually' 让叙事一气呵成",
    "[Content] 加了'I confided in my mum' 这种具体细节，避免空泛"
  ]
}}"""
        raw = await self.llm.chat(prompt)
        start = raw.find("{")
        end = raw.rfind("}") + 1
        if start < 0 or end <= start:
            raise ValueError(f"No JSON in insight output: {raw[:200]}")
        data = json.loads(raw[start:end])
        return AnswerInsightResult(
            main_idea=str(data.get("main_idea", "")).strip(),
            logic_structure=str(data.get("logic_structure", "")).strip(),
            highlights=[str(h).strip() for h in data.get("highlights", []) if h],
            raw=data,
        )
