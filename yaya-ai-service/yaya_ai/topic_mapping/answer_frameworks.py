"""Part 2 answer scaffolds — Chinese skeleton + English boilerplate per slot.

Source: 【内容】三级：Part2回答区框架提示词.pdf  +  【PRD】三级：练习-Part2+Part3.pdf

UX (PRD): each topic type renders a Chinese skeleton always-on; every segment
has a 【📖 点击查看英文提示】 toggle that reveals fill-in-the-blank English
templates. This module ONLY holds data — the UI lives in app.py and uses HTML
<details> for collapse so no extra Gradio wiring is needed.
"""
from __future__ import annotations

from dataclasses import dataclass, field


@dataclass(frozen=True)
class Segment:
    zh_label: str                     # e.g. "起因与动机"
    zh_hint: str = ""                 # e.g. "主动/被动、导火索、当时状态"
    en_templates: tuple[str, ...] = ()  # English boilerplate lines


@dataclass(frozen=True)
class Framework:
    type_key: str       # "events" | "people" | "places" | "items_concrete" | "items_abstract"
    name_zh: str        # "事件类"
    description: str
    segments: tuple[Segment, ...] = field(default_factory=tuple)


# ── 1. 事件类 ─────────────────────────────────────────────────────────────

EVENTS = Framework(
    type_key="events",
    name_zh="事件类",
    description="Describe an event / experience / time when…",
    segments=(
        Segment(
            zh_label="总起",
            en_templates=(
                "When I saw this topic, the first experience that popped up in my mind was ___.",
            ),
        ),
        Segment(
            zh_label="起因与动机",
            zh_hint="主动/被动、导火索、当时状态",
            en_templates=(
                "To begin with, this entire experience was set in motion when ___.",
                "Prior to that moment, I found myself in a state of ___.",
            ),
        ),
        Segment(
            zh_label="时间与地点",
            zh_hint="季节、时段、天气、场所",
            en_templates=(
                "As for the setting, it unfolded in ___, sometime around ___.",
                "I can still vividly recall that the weather that day was ___.",
            ),
        ),
        Segment(
            zh_label="人物与关系",
            zh_hint="同行者、关系、各自角色",
            en_templates=(
                "In terms of company, I was accompanied by ___, who happens to be ___.",
                "Within that dynamic, I took on the role of ___.",
            ),
        ),
        Segment(
            zh_label="经过与困难",
            zh_hint="开头动作、遇到的麻烦、想放弃的瞬间",
            en_templates=(
                "Initially, I set out by ___.",
                "Before long, however, I was confronted with ___.",
                "I distinctly remember a turning point where ___.",
            ),
        ),
        Segment(
            zh_label="克服困难的方法",
            zh_hint="具体做法、心理调整、时间推移",
            en_templates=(
                "In response to that, the course of action I took was ___.",
                "The thought that kept running through my mind was ___.",
                "As time went on, ___.",
            ),
        ),
        Segment(
            zh_label="成果与后续",
            zh_hint="结果、分享对象",
            en_templates=(
                "Ultimately, ___.",
                "The very first person I confided in was ___.",
            ),
        ),
        Segment(
            zh_label="当时的感受",
            zh_hint="情绪变化、身体感受",
            en_templates=(
                "Throughout that period, I experienced a sense of ___.",
                "On a physical level, I could feel ___.",
            ),
        ),
        Segment(
            zh_label="事后的看法",
            zh_hint="现在的评价",
            en_templates=(
                "I've come to view it as ___.",
            ),
        ),
        Segment(
            zh_label="总结",
            en_templates=(
                "In retrospect, this experience is deeply rooted in my mind.",
            ),
        ),
    ),
)


# ── 2. 人物类 ─────────────────────────────────────────────────────────────

PEOPLE = Framework(
    type_key="people",
    name_zh="人物类",
    description="Describe a person…",
    segments=(
        Segment(
            zh_label="总起",
            zh_hint="点出人名、关系",
            en_templates=(
                "When I saw this topic, the first person that popped up in my mind was ___, who ___.",
            ),
        ),
        Segment(
            zh_label="外貌与第一印象",
            zh_hint="身高、发型、穿搭、初见场景",
            en_templates=(
                "When I first laid eyes on him/her, ___.",
                "I would describe him/her as roughly ___ in height, with ___ hair and ___.",
                "Our paths first crossed at ___.",
                "The very first words he/she uttered to me were ___.",
            ),
        ),
        Segment(
            zh_label="性格与行为",
            zh_hint="标志特点、典型事例、反差",
            en_templates=(
                "Fundamentally, he/she is the type of individual who ___.",
                "To give you a concrete example, ___.",
                "Actually, he/she is the most ___ person I have ever known.",
            ),
        ),
        Segment(
            zh_label="核心事件",
            zh_hint="印象深刻的具体一件事",
            en_templates=(
                "On one particular occasion, I recall ___.",
                "I still possess a crystal-clear mental image of ___.",
                "The most impressive thing about him/her is that ___.",
                "One thing that left me the deepest impression was ___. I really had a blast that day.",
            ),
        ),
        Segment(
            zh_label="深层影响",
            zh_hint="对我的改变、留下的痕迹",
            en_templates=(
                "Owing to his/her presence in my life, I began to ___.",
                "To this day, it manifests itself in the way I ___.",
            ),
        ),
        Segment(
            zh_label="关系现状",
            zh_hint="现在联系频率、亲疏程度",
            en_templates=(
                "As for the present moment, we ___.",
            ),
        ),
        Segment(
            zh_label="总结",
            en_templates=(
                "To conclude, he/she holds a special place in my heart.",
            ),
        ),
    ),
)


# ── 3. 地点类 ─────────────────────────────────────────────────────────────

PLACES = Framework(
    type_key="places",
    name_zh="地点类",
    description="Describe a place…",
    segments=(
        Segment(
            zh_label="总起",
            en_templates=(
                "When I saw this topic, the first place that popped up in my mind was ___.",
                "To tell you more about it, it's a/an ___ located in ___.",
            ),
        ),
        Segment(
            zh_label="地理位置与到达方式",
            zh_hint="在哪、怎么去、路上地标",
            en_templates=(
                "Speaking of location, it is situated in ___, a short distance from ___.",
                "I get there by ___.",
                "The typical route I take to get there is ___.",
            ),
        ),
        Segment(
            zh_label="环境描写",
            zh_hint="光线、声音、气味、布局",
            en_templates=(
                "The moment you step inside, what strikes you immediately is ___.",
                "The lighting tends to be ___, and there is a distinct scent of ___.",
            ),
        ),
        Segment(
            zh_label="活动与习惯",
            zh_hint="常做的事、常坐的位置、待多久",
            en_templates=(
                "For the most part, what I do there revolves around ___.",
                "I have a tendency to settle into ___ and remain there for roughly ___.",
            ),
        ),
        Segment(
            zh_label="情绪记忆",
            zh_hint="在这里有过的强烈情绪",
            en_templates=(
                "I have undoubtedly experienced moments of profound ___ within those walls.",
            ),
        ),
        Segment(
            zh_label="再去或不再去的理由",
            zh_hint="现在对这个地方的态度",
            en_templates=(
                "When all is said and done, the rationale behind my returning—or choosing not to—is ___.",
            ),
        ),
        Segment(
            zh_label="总结",
            en_templates=(
                "To conclude, there is no better place than this to make me feel ___.",
            ),
        ),
    ),
)


# ── 4. 物体类（具体物体）────────────────────────────────────────────────

ITEMS_CONCRETE = Framework(
    type_key="items_concrete",
    name_zh="物体类（具体物体）",
    description="Describe a physical object you own / treasure…",
    segments=(
        Segment(
            zh_label="总起",
            en_templates=(
                "When I saw this topic, the first ___ that popped up in my mind was ___.",
                "To tell you more about it, ___.",
                "In terms of why/how ___, actually, ___.",
            ),
        ),
        Segment(
            zh_label="物理属性",
            zh_hint="大小、颜色、材质、痕迹",
            en_templates=(
                "In terms of its physical presence, it measures roughly the size of ___, "
                "bears a ___ hue, and feels ___ to the touch.",
            ),
        ),
        Segment(
            zh_label="获得过程",
            zh_hint="来源、场合、谁给的、当时心情",
            en_templates=(
                "I came into possession of it from ___, way back when ___.",
                "I can still recall the sensation I had at that very instant — it was ___.",
            ),
        ),
        Segment(
            zh_label="使用场景",
            zh_hint="用在哪里、频率、使用时的细节",
            en_templates=(
                "Its primary function in my daily life is ___, and I find myself reaching for it "
                "perhaps ___ times per day/week.",
                "Every single time I make use of it, I catch myself ___.",
            ),
        ),
        Segment(
            zh_label="情感价值",
            zh_hint="联想到的人或事、为什么重要",
            en_templates=(
                "Quite frankly, it serves as a tangible reminder of ___.",
                "The underlying reason it carries weight with me is ___.",
            ),
        ),
        Segment(
            zh_label="与之相关的回忆",
            zh_hint="某个具体的记忆片段",
            en_templates=(
                "One specific recollection that springs to mind in connection with it is ___.",
            ),
        ),
        Segment(
            zh_label="总结",
            en_templates=(
                "To conclude, this ___ holds a special place in my heart.",
            ),
        ),
    ),
)


# ── 5. 物体类（抽象物体）────────────────────────────────────────────────

ITEMS_ABSTRACT = Framework(
    type_key="items_abstract",
    name_zh="物体类（抽象物体）",
    description="Describe a book / film / song / idea…",
    segments=(
        Segment(
            zh_label="总起",
            en_templates=(
                "When I saw this topic, the first ___ that popped up in my mind was ___.",
            ),
        ),
        Segment(
            zh_label="基本面貌",
            zh_hint="类型、名字、创作者、大致风格",
            en_templates=(
                "To put it simply, it is a ___ entitled ___, "
                "which was brought into existence by ___.",
            ),
        ),
        Segment(
            zh_label="接触途径",
            zh_hint="第一次接触的时间、场合、推荐人",
            en_templates=(
                "My initial encounter with it occurred when ___.",
                "If memory serves me right, it was ___ who first put it on my radar.",
            ),
        ),
        Segment(
            zh_label="内容或特点",
            zh_hint="情节、旋律、核心观点、最打动你的部分",
            en_templates=(
                "The element that truly distinguishes it is ___.",
                "The segment that resonated with me on the deepest level was ___.",
            ),
        ),
        Segment(
            zh_label="与之相关的经历",
            zh_hint="接触它的那段时间发生了什么",
            en_templates=(
                "Coincidentally, that chapter of my life was also marked by ___.",
            ),
        ),
        Segment(
            zh_label="个人感受或意义",
            zh_hint="为什么喜欢、对现在的你意味着什么",
            en_templates=(
                "On a personal note, it has come to symbolize ___.",
                "And that, in essence, is precisely why it has endured in my consciousness.",
            ),
        ),
        Segment(
            zh_label="总结",
            en_templates=(
                "To conclude, this ___ holds a special place in my heart.",
            ),
        ),
    ),
)


ALL_FRAMEWORKS: tuple[Framework, ...] = (
    EVENTS, PEOPLE, PLACES, ITEMS_CONCRETE, ITEMS_ABSTRACT,
)


def by_type_key(key: str) -> Framework | None:
    for fw in ALL_FRAMEWORKS:
        if fw.type_key == key:
            return fw
    return None
