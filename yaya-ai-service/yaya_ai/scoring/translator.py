"""EN→CN paragraph translator (async)."""
from __future__ import annotations

import json
from dataclasses import dataclass, field


@dataclass
class TranslationResult:
    chinese: str
    pairs: list[tuple[str, str]]
    raw: dict = field(default_factory=dict, repr=False)

    def to_dict(self) -> dict:
        return {
            "chinese": self.chinese,
            "pairs": [[en, zh] for en, zh in self.pairs],
        }


class Translator:
    def __init__(self, llm):
        self.llm = llm

    async def en_to_zh(self, text: str) -> TranslationResult:
        text = text.strip()
        if not text:
            return TranslationResult(chinese="", pairs=[])
        prompt = f"""Translate the English text below into NATURAL Chinese.

English:
\"\"\"
{text}
\"\"\"

# Rules
- Conversational Chinese, not stiff / literal. Use 我、就、还、其实 like a real speaker.
- Match the IELTS speaking flavour — like a tutor explaining a student's answer.
- Keep names, place names, brands as-is (e.g. Tokyo, Netflix).
- Provide a sentence-by-sentence pair list aligned with the original.

# Output strict JSON, NO markdown fence:

{{
  "chinese": "整段流畅的中文译文（一段就好，不要分点）",
  "pairs": [
    ["English sentence 1", "中文句 1"],
    ["English sentence 2", "中文句 2"]
  ]
}}"""
        raw = await self.llm.chat(prompt)
        start = raw.find("{")
        end = raw.rfind("}") + 1
        if start < 0 or end <= start:
            raise ValueError(f"No JSON in translator output: {raw[:200]}")
        data = json.loads(raw[start:end])
        pairs: list[tuple[str, str]] = []
        for p in data.get("pairs", []):
            if isinstance(p, list) and len(p) >= 2 and p[0] and p[1]:
                pairs.append((str(p[0]).strip(), str(p[1]).strip()))
        return TranslationResult(
            chinese=str(data.get("chinese", "")).strip(),
            pairs=pairs,
            raw=data,
        )
