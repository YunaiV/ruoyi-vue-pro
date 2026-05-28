"""Rule-based Part 2 category classification from English title (prototype verbatim).

Mirrors 【PRD】Part2 串题方法论.pdf grouping. Used when DB category is empty.
"""
from __future__ import annotations

# Ordered rules — first match wins.
_CATEGORY_RULES: list[tuple[str, tuple[str, ...]]] = [
    ("PLACES", (
        "occasion when many",
        "not allowed to use your mobile",
        "first talked in a foreign language",
        "exciting activity you have tried",
        "tried for the first time",
    )),
    ("ITEMS", (
        "broke something",
        "apologised to you", "apologized to you",
        "waited for something special",
        "received good service", "good service in a shop",
        "interesting on social media", "social media",
        "kept in your family", "has kept for a long time",
        "old thing", "important old thing",
    )),
    ("EVENTS", (
        "a time when", "a time you",
        "an occasion when",
        "gave advice", "encouraged someone", "use your imagination",
        "lost your way", "electricity", "went off",
        "learned a skill", "learned something new", "learned from someone",
        "good habit your friend",
        "natural talent", "positive change", "daily routine",
        "area/subject", "area of science", "subject of science",
        "perfect job", "water sport",
        "talk you gave", "told your friend", "told a friend",
        "felt proud",
    )),
    ("PEOPLE", (
        "person who", "person you would", "person you know",
        "famous person", "friend who", "friend of yours",
        "good friend", "child who", "one of your friends",
        "someone who", "creative person", "sportsperson",
        "popular person", "person makes plans",
        "great dinner", "unusual meal",
        "family member", "important decision",
    )),
    ("ITEMS", (
        "a movie", "a book", "a toy", "an item",
        "piece of technology", "a program or app", "an app on",
        "an invention",
        "something important", "something that you",
        "can't live without", "cant live without",
        "spent more than",
        "traditional story", "a story",
    )),
    ("PLACES", (
        "a place", "a building", "a country", "a city",
        "shopping mall", "a mall",
        "a room", "in your house", "hometown",
        "natural place", "place with",
        "a trip", "a journey", "foreign country",
        "car trip", "motorcycle", "bicycle",
        "wild animal", "music event",
    )),
]


def classify_topic(title_en: str) -> str:
    s = (title_en or "").lower()
    for category, keywords in _CATEGORY_RULES:
        if any(k in s for k in keywords):
            return category
    return "PLACES"
