"""Story Mapper — one story, coverage across ALL Part 2 topics (async, DB-backed).

Flow (prototype verbatim):
  1. Extract story elements from transcript
  2. Score all Part 2 topics (0–100%)
  3. Per-topic template fit (green/yellow/red modules)
  4. Assemble user-edited modules into a full answer
"""
from __future__ import annotations

import json
from typing import Any, Callable

from ..logging import get_logger
from .templates import Template, get_template
from .types import (
    AssembledAnswer,
    ModuleFit,
    Part2Topic,
    SlotFeedback,
    StoryElements,
    StoryMapResult,
    TemplateFit,
    TopicCoverage,
)

log = get_logger(__name__)

OnStep = Callable[[str], None] | None


class StoryMapper:
    def __init__(self, llm, topics: list[Part2Topic]):
        self.llm = llm
        self.topics = topics
        self._fit_cache: dict[tuple[str, str], TemplateFit] = {}

    async def map(
        self,
        transcript: str,
        source_id: str,
        original_topic: str,
        on_step: OnStep = None,
        elements: StoryElements | None = None,
    ) -> StoryMapResult:
        if elements is None:
            if on_step:
                on_step("extract_elements")
            log.info("story_map_extract_elements", source_id=source_id)
            elements = await self.extract_elements(transcript, original_topic)

        if on_step:
            on_step("score_topics")
        log.info("story_map_score_topics", source_id=source_id, topic_count=len(self.topics))
        coverages = await self._score_all_topics(transcript, elements)
        coverages = sorted(
            [c for c in coverages if c.score > 0],
            key=lambda c: c.score,
            reverse=True,
        )
        return StoryMapResult(
            source_id=source_id,
            original_topic=original_topic,
            elements=elements,
            coverages=coverages,
        )

    async def extract_elements(self, transcript: str, original_topic: str) -> StoryElements:
        return await self._extract_elements(transcript, original_topic)

    async def _extract_elements(self, transcript: str, original_topic: str) -> StoryElements:
        prompt = f"""Extract the key reusable story elements from this IELTS Part 2 answer.

Topic: {original_topic}
Transcript: {transcript}

Respond ONLY with valid JSON:
{{
  "person": "brief description of subject (Chinese OK, e.g. 表弟的儿子，安静内向，8岁)",
  "traits": ["trait1", "trait2", "trait3"],
  "behaviors": ["what they do / what happened (2–4 items)"],
  "emotions": ["feelings, impact, meaning (2–3 items)"],
  "setting": "when/where/context (one line)",
  "raw_summary": "one sentence story summary in Chinese"
}}"""
        raw = await self.llm.chat(prompt)
        s = raw[raw.find("{") : raw.rfind("}") + 1]
        d = json.loads(s)
        return StoryElements(
            person=d.get("person", ""),
            traits=d.get("traits", []),
            behaviors=d.get("behaviors", []),
            emotions=d.get("emotions", []),
            setting=d.get("setting", ""),
            raw_summary=d.get("raw_summary", ""),
        )

    async def _score_all_topics(
        self, transcript: str, elements: StoryElements
    ) -> list[TopicCoverage]:
        topics_text = "\n".join(t.to_prompt_str() for t in self.topics)
        n = len(self.topics)

        prompt = f"""You are an expert IELTS speaking coach. A student has ONE prepared answer.
Your task: evaluate how well this story can be REUSED for each of the {n} Part 2 topics listed below.

## What "套题" (story reuse) means in IELTS:
- The student keeps the SAME core story (same person, same events, same emotions)
- They only change: the opening sentence framing, 1-2 key nouns/roles, and the closing sentence
- The story must GENUINELY answer the new topic — not just vaguely relate to it
- A topic about "a person who..." requires the story's MAIN CHARACTER to fit that role
- A topic about "a time when..." requires the story to contain THAT TYPE OF EVENT
- DO NOT give high scores just because themes loosely overlap

## Fictional / literary stories (e.g. 《促织》, anime, novels, films)
- Treat them as the student's CHOSEN story material. Score them against topics where the
  story's protagonist, conflict, sacrifice, transformation, lesson, or emotional arc maps
  directly onto the new question.
- For these stories, "describe a person / family / sacrifice / book that influenced you /
  a story you read / a piece of art / a cultural tradition" topics should usually score
  high if the story details match.
- Do NOT give high scores to topics about everyday personal experiences (e.g. "a time you
  were late", "a habit you developed") when the underlying story is purely literary —
  scoring above 40 for those is wrong unless the student personalises the framing.

## Scoring guide (use multiples of 10 only):
  100: perfect fit, change only opening/closing sentence framing
  80:  strong fit, change 1 key role/noun (e.g. "cousin's son" → "friend")
  60:  good fit, change relationship + add 1-2 sentences of new detail
  40:  partial fit, the character traits or emotional arc are reusable but need new specific examples
  20:  weak fit, only the theme/emotion is vaguely similar — new story mostly needed
  0:   no real connection — DO NOT include

## Story being analysed:
Person: {elements.person}
Traits: {', '.join(elements.traits)}
Key behaviors: {', '.join(elements.behaviors)}
Emotions/impact: {', '.join(elements.emotions)}
Context: {elements.setting}

Full transcript:
{transcript}

## All {n} Part 2 topics (use the ID number in your response):
{topics_text}

For each topic with score ≥ 20, provide:
- topic_id: the [ID:N] number from the list above
- score: 20/40/60/80/100
- keep: 2-3 specific story elements that work directly (be concrete, Chinese OK)
- modify: 1-3 specific changes needed (be concrete, e.g. "把'表弟儿子'改成'朋友'" not "change person")

Respond ONLY with valid JSON — no markdown, no explanation:
{{
  "results": [
    {{
      "topic_id": 8,
      "score": 100,
      "keep": ["安静专注的性格描写", "每天画画的细节", "完成作品的成就感"],
      "modify": ["开头改为'I'd like to talk about a friend...'"]
    }}
  ]
}}"""

        raw = await self.llm.chat(prompt)
        s = raw[raw.find("{") : raw.rfind("}") + 1]
        data = json.loads(s)

        topic_map = {t.number: t for t in self.topics}
        coverages: list[TopicCoverage] = []
        for r in data.get("results", []):
            num = r.get("topic_id") or r.get("topic_number")
            if num not in topic_map:
                continue
            score = int(r.get("score", 0))
            if score < 20:
                continue
            coverages.append(
                TopicCoverage(
                    topic=topic_map[num],
                    score=score,
                    stars=_stars(score),
                    keep=r.get("keep", []),
                    modify=r.get("modify", []),
                )
            )
        return coverages

    async def explain_coverage(
        self,
        transcript: str,
        elements: StoryElements,
        topic: Part2Topic,
        source_id: str = "",
    ) -> TemplateFit:
        key = (source_id, topic.uuid)
        if key in self._fit_cache:
            return self._fit_cache[key]

        template = get_template(topic.category)
        fit = await self._build_fit(transcript, elements, topic, template)
        self._fit_cache[key] = fit
        return fit

    async def _build_fit(
        self,
        transcript: str,
        elements: StoryElements,
        topic: Part2Topic,
        template: Template,
    ) -> TemplateFit:
        modules_spec = "\n".join(
            f"- id={m.id} | label={m.label} | slots={m.slots or '[]'}\n  frame: {m.text}"
            for m in template.modules
        )
        cue = " / ".join(topic.cue_points) if topic.cue_points else "(no cue points)"

        prompt = f"""You are an IELTS Part 2 coach helping a student adapt ONE prepared story to a NEW topic.

The answer template for this topic category is FIXED — it is a skeleton of {len(template.modules)} modules.
Framing phrases (e.g. "When I saw this topic...", "Speaking of what...", "One is that...") never change.
Only the {{slot}} placeholders inside each module need content from the student's story.

Your job: for each module, decide whether the student's prepared story gives us the content to fill its
slots, and output a short rendered line showing what the module would look like filled in.

## Category
{template.category} ({template.name_zh})

## Modules (the skeleton)
{modules_spec}

## The NEW topic to adapt to
Title (EN): {topic.title_en}
Title (ZH): {topic.title_zh}
Cue points: {cue}

## The student's prepared story
Person/subject: {elements.person}
Traits: {', '.join(elements.traits)}
Key behaviours/events: {', '.join(elements.behaviors)}
Emotions/impact: {', '.join(elements.emotions)}
Context: {elements.setting}

Full transcript:
\"\"\"
{transcript}
\"\"\"

## Rules for each module
State is ONE of:
  - "green":  the module has no slots (pure framing) OR the story provides slot content that fits the
              new topic essentially as-is. No real rewriting needed.
  - "yellow": the slots can be filled by swapping 1–2 keywords/phrases from the student's story (e.g.
              "Beijing" → "a local park", "cousin's son" → "my friend"). Story content is reusable.
  - "red":    the new topic's cue demands content the story does NOT cover, or the story's slot content
              genuinely contradicts the new topic. The student must add fresh material here.

For each module output:
  - id: the module id string
  - state: green | yellow | red
  - text: the FILLED module text — a short English sentence showing how this module would read under the
          new topic. Fill slots with concrete suggestions. For red modules, write a placeholder like
          "[need to add: ...]" where the missing content belongs, in English.
  - note: a short Chinese hint (≤20 chars) explaining the choice, e.g. "直接套用" / "北京→本地公园" /
          "原素材无 why-interesting". Empty string for pure-framing green modules is fine.

Also give a `summary` field: ONE short Chinese sentence summarising how well this story adapts overall.

Respond with ONLY valid JSON, no markdown fence, no commentary:
{{
  "summary": "...",
  "modules": [
    {{"id": "1", "state": "green", "text": "...", "note": "..."}},
    ...
  ]
}}"""

        raw = await self.llm.chat(prompt)
        s = raw[raw.find("{") : raw.rfind("}") + 1]
        data = json.loads(s)

        by_id = {m["id"]: m for m in data.get("modules", [])}
        mods = []
        for m in template.modules:
            hit = by_id.get(m.id, {})
            mods.append(
                ModuleFit(
                    id=m.id,
                    label=m.label,
                    text=hit.get("text", m.text),
                    state=hit.get("state", "red"),
                    note=hit.get("note", ""),
                )
            )

        return TemplateFit(
            category=template.category,
            category_zh=template.name_zh,
            modules=mods,
            summary=data.get("summary", ""),
        )

    async def assemble_and_check(
        self,
        fit: TemplateFit,
        user_edits: dict[str, str],
        topic: Part2Topic,
        elements: StoryElements,
    ) -> AssembledAnswer:
        merged: list[dict[str, Any]] = []
        for m in fit.modules:
            user_text = (user_edits.get(m.id) or "").strip()
            merged.append(
                {
                    "id": m.id,
                    "label": m.label,
                    "state": m.state,
                    "text": user_text or m.text,
                    "was_edited": bool(user_text),
                }
            )

        modules_block = "\n".join(
            f"- [{m['id']}] ({m['label']}) state={m['state']} edited={m['was_edited']}\n"
            f"    content: {m['text']}"
            for m in merged
        )
        cue = " / ".join(topic.cue_points) if topic.cue_points else "(no cue points)"

        prompt = f"""You are an IELTS Part 2 coach. A student has filled in the blanks of a fixed answer
template and wants your help to (a) stitch everything into one continuous ~220 word answer,
and (b) grade each module they edited.

## New topic
Title (EN): {topic.title_en}
Cue points: {cue}

## Student's background story (context only)
{elements.raw_summary}

## The modules (with the student's current content)
{modules_block}

## Your tasks

1. Assemble ALL modules in order into ONE continuous English Part 2 answer.
   - Keep each module's fixed framing phrases intact (e.g. "When I saw this topic...",
     "Speaking of what...", "One is that..., and the other is that...", "To conclude...").
   - Smooth transitions and pronoun agreement between modules, but do NOT rewrite modules
     whose state=green.
   - Light grammar fixes on edited modules are OK — do not rewrite their substance.
   - Target length: 200-250 words.

2. Give ONE feedback entry per module:
   - state=ok: looks good, only trivial or zero fixes.
   - state=warn: minor grammar / wording issue, content still answers the cue.
   - state=issue: content does NOT address the new topic's cue point, is off-topic, or makes
     the answer incoherent.
   - For modules the student did not edit (was_edited=false), you may use state=ok with empty msg.
   - msg: short Chinese (≤25 chars). Examples: "直接套用" / "'peoples' → 'people'" /
     "没扣题: 新题要求 why interesting，这里讲了 how recharged"

Respond with ONLY valid JSON — no markdown fence, no commentary:
{{
  "full_text": "...the complete continuous answer...",
  "word_count": 230,
  "feedbacks": [
    {{"module_id": "1", "state": "ok", "msg": "固定句"}},
    {{"module_id": "2b", "state": "warn", "msg": "'peoples' → 'people'"}},
    ...
  ]
}}"""

        raw = await self.llm.chat(prompt)
        s = raw[raw.find("{") : raw.rfind("}") + 1]
        data = json.loads(s)

        feedbacks = [
            SlotFeedback(
                module_id=f.get("module_id", ""),
                state=f.get("state", "ok"),
                msg=f.get("msg", ""),
            )
            for f in data.get("feedbacks", [])
        ]
        text = data.get("full_text", "")
        wc = data.get("word_count") or len(text.split())
        return AssembledAnswer(full_text=text, word_count=int(wc), feedbacks=feedbacks)


def _stars(score: int) -> str:
    full = score // 20
    half = (score % 20) >= 10
    empty = 5 - full - (1 if half else 0)
    return "★" * full + ("½" if half else "") + "☆" * empty
