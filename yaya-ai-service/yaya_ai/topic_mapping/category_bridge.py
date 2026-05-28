"""Map DB practice topic categories to prototype template buckets."""
from __future__ import annotations

from .classify_topic import classify_topic

_DB_TO_TEMPLATE: dict[str, str] = {
    "事件": "EVENTS",
    "人物": "PEOPLE",
    "地点": "PLACES",
    "具体物体": "ITEMS",
    "抽象物体": "ITEMS",
}


def to_template_bucket(db_category: str, *, title_en: str = "") -> str:
    """Return PLACES | PEOPLE | EVENTS | ITEMS for template selection."""
    normalized = (db_category or "").strip()
    if normalized in _DB_TO_TEMPLATE:
        return _DB_TO_TEMPLATE[normalized]
    # legacy CSV may still carry 类 suffix
    if normalized.endswith("类"):
        base = normalized.removesuffix("类")
        if base in _DB_TO_TEMPLATE:
            return _DB_TO_TEMPLATE[base]
    if title_en:
        return classify_topic(title_en)
    return "PLACES"
