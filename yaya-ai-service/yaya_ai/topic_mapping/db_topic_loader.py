"""Load Part2Topic rows from DB-shaped dicts (no SQLAlchemy dependency in yaya-ai)."""
from __future__ import annotations

import re
from typing import Any

from .category_bridge import to_template_bucket
from .types import Part2Topic

_EXPLAIN_RE = re.compile(r"\band explain\b", re.IGNORECASE)


def _first_cue_question(questions: list[dict[str, Any]]) -> dict[str, Any] | None:
    for q in questions or []:
        if (q.get("question_role") or "question") == "cue_prompt":
            return q
    return questions[0] if questions else None


def _split_closing(prompt_en: str) -> tuple[str, str]:
    """Return (body, closing) splitting on 'And explain'."""
    text = (prompt_en or "").strip()
    if not text:
        return "", ""
    m = _EXPLAIN_RE.search(text)
    if not m:
        return text, ""
    body = text[: m.start()].strip().rstrip(".")
    closing = text[m.start() :].strip()
    return body, closing


def part2_topic_from_db_row(row: dict[str, Any]) -> Part2Topic:
    """Build Part2Topic from a practice topic dict (ORM or API serializer shape)."""
    questions = row.get("questions") or []
    q = _first_cue_question(questions)

    prompt_en = (
        (q.get("prompt_en") if q else None)
        or row.get("prompt_en")
        or row.get("title_en")
        or ""
    ).strip()
    title_en = (row.get("title_en") or prompt_en).strip()
    body, closing = _split_closing(prompt_en)
    if not closing and q:
        bullets = q.get("cue_bullets") or []
        if bullets:
            closing = f"And explain {bullets[-1].lower()}"

    cue_points = list((q.get("cue_bullets") if q else None) or [])
    db_category = str(row.get("category") or "")
    template_bucket = to_template_bucket(db_category, title_en=title_en or prompt_en)

    return Part2Topic(
        uuid=str(row.get("id") or ""),
        number=int(row.get("number") or row.get("display_order") or 0),
        title_zh=str(row.get("title_zh") or "").strip(),
        title_en=title_en or body or str(row.get("title_zh") or ""),
        cue_points=[str(b).strip() for b in cue_points if str(b).strip()],
        closing=closing,
        category=template_bucket,
    )


def load_part2_topics_from_rows(rows: list[dict[str, Any]]) -> list[Part2Topic]:
    topics = [part2_topic_from_db_row(r) for r in rows]
    topics.sort(key=lambda t: (t.number, t.uuid))
    return topics
