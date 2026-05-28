"""Shared dataclasses for Part 2 story mapping and template assembly."""
from __future__ import annotations

from dataclasses import dataclass, field


@dataclass
class Part2Topic:
    """Runtime view of a Part 2 topic — loaded from DB, not markdown."""

    uuid: str
    number: int
    title_zh: str
    title_en: str
    cue_points: list[str]
    closing: str
    category: str = "PLACES"  # PLACES / PEOPLE / EVENTS / ITEMS

    def __str__(self) -> str:
        points = "\n  - ".join(self.cue_points)
        return (
            f"[{self.number}] {self.title_zh}\n"
            f"  {self.title_en}\n"
            f"  You should say:\n  - {points}\n"
            f"  {self.closing}"
        )

    def to_prompt_str(self) -> str:
        points = " / ".join(self.cue_points[:3])
        closing = f" / {self.closing}" if self.closing else ""
        return f"[ID:{self.number}] {self.title_en}{closing}"


@dataclass
class StoryElements:
    """Key reusable elements extracted from the transcript."""

    person: str
    traits: list[str] = field(default_factory=list)
    behaviors: list[str] = field(default_factory=list)
    emotions: list[str] = field(default_factory=list)
    setting: str = ""
    raw_summary: str = ""

    def __str__(self) -> str:
        lines = [
            f"  人物:  {self.person}",
            f"  特质:  {', '.join(self.traits)}",
            f"  行为:  {', '.join(self.behaviors)}",
            f"  情感:  {', '.join(self.emotions)}",
            f"  背景:  {self.setting}",
        ]
        return "\n".join(lines)


@dataclass
class TopicCoverage:
    topic: Part2Topic
    score: int
    stars: str
    keep: list[str] = field(default_factory=list)
    modify: list[str] = field(default_factory=list)

    def display(self) -> str:
        pct = f"{self.score}%"
        lines = [
            f"{self.stars} [{pct:>4}]  [{self.topic.number:02d}] {self.topic.title_zh}"
        ]
        lines.append(f"           {self.topic.title_en}")
        if self.keep:
            lines.append(f"           ✅ 直接用: {' / '.join(self.keep)}")
        if self.modify:
            lines.append(f"           ✏️  需改:   {' / '.join(self.modify)}")
        return "\n".join(lines)


@dataclass
class ModuleFit:
    id: str
    label: str
    text: str
    state: str  # green | yellow | red
    note: str = ""


@dataclass
class TemplateFit:
    category: str
    category_zh: str
    modules: list[ModuleFit]
    summary: str = ""


@dataclass
class SlotFeedback:
    module_id: str
    state: str  # ok | warn | issue
    msg: str


@dataclass
class AssembledAnswer:
    full_text: str
    word_count: int
    feedbacks: list[SlotFeedback] = field(default_factory=list)


@dataclass
class StoryMapResult:
    source_id: str
    original_topic: str
    elements: StoryElements
    coverages: list[TopicCoverage] = field(default_factory=list)

    def __str__(self) -> str:
        lines = [
            f"{'=' * 65}",
            f"Source: {self.source_id}",
            f"原题: {self.original_topic}",
            f"{'=' * 65}",
            "",
            "【故事关键要素】",
            str(self.elements),
            "",
            f"【覆盖分析】共找到 {len(self.coverages)} 个可套用题目",
            "",
        ]
        for cov in self.coverages:
            lines.append(cov.display())
            lines.append("")
        return "\n".join(lines)
