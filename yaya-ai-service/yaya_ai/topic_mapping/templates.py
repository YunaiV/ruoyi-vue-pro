"""Part 2 structured answer templates (PLACES / PEOPLE / EVENTS / ITEMS).

Source: 【PRD】Part2 串题方法论.pdf

Each category's answer is a fixed skeleton of modules. Most of the text is
framing that stays the same across topics (greens). The `{...}` placeholders
are the only things that must change per topic (yellows if reusable from the
student's prepared story, reds if the new topic demands content the story
doesn't have).
"""
from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class Module:
    id: str           # e.g. "1", "1a", "2a", "3b"
    label: str        # e.g. "总起", "活动 1"
    text: str         # the fixed frame, with {slot} placeholders
    slots: list[str]  # ordered slot names that appear in text


@dataclass(frozen=True)
class Template:
    category: str     # PLACES / PEOPLE / EVENTS / ITEMS
    name_zh: str
    modules: list[Module]


PLACES = Template(
    category="PLACES",
    name_zh="地点类",
    modules=[
        Module("1",  "总起",
            "When I saw this topic, the first place/experience that popped up in my mind was {place}.",
            ["place"]),
        Module("1a", "位置",
            "To tell you more about it, it's a/an {type} located in {location}.",
            ["type", "location"]),
        Module("1b", "事件/背景",
            "Actually, {background}.",
            ["background"]),
        Module("2",  "WHAT 过渡",
            "Speaking of what {topic_hook}, well, there are mainly 2 things I wanna share with you.",
            ["topic_hook"]),
        Module("2a", "活动 1",
            "Talking about {attraction_a}, it's second to none. "
            "In fact, it's like a magnet attracting millions of people flooding there for {reason_a}. "
            "As for us, {activity_a}.",
            ["attraction_a", "reason_a", "activity_a"]),
        Module("2b", "活动 2",
            "In terms of {attraction_b}, it's also beyond compare. "
            "As a matter of fact, it's like a paradise for people to {reason_b}, including us. "
            "So when we were there, {activity_b}.",
            ["attraction_b", "reason_b", "activity_b"]),
        Module("3",  "WHY/HOW 过渡",
            "When it comes to why/how {topic_hook}, well,",
            ["topic_hook"]),
        Module("3a", "原因/感受",
            "One is that {reason_1} made me feel recharged, "
            "and the other is that {reason_2} enabled me to take my mind off my tedious daily routine.",
            ["reason_1", "reason_2"]),
        Module("3b", "总结",
            "To conclude, there is no better place/experience than this to make me feel pleasant.",
            []),
    ],
)


PEOPLE = Template(
    category="PEOPLE",
    name_zh="人物类",
    modules=[
        Module("1",  "总起",
            "When I saw this topic, the first person that popped up in my mind was {person}, who {relation}.",
            ["person", "relation"]),
        Module("1a", "外貌",
            "To tell you more about him/her, he's/she's {age_build}, "
            "who's {height}, with {feature_1} and {feature_2}.",
            ["age_build", "height", "feature_1", "feature_2"]),
        Module("1b", "性格",
            "Actually, he/she's the most {core_trait} person I have ever known.",
            ["core_trait"]),
        Module("2",  "WHAT 过渡",
            "Speaking of what {topic_hook}, well, there are mainly 2 things I wanna share with you.",
            ["topic_hook"]),
        Module("2a", "事件 1",
            "The most impressive thing about him/her is that {fact_a}.",
            ["fact_a"]),
        Module("2b", "事件 2",
            "One thing that left me the deepest impression was {fact_b}. "
            "I really had a blast that day {detail_b}.",
            ["fact_b", "detail_b"]),
        Module("3",  "WHY/HOW 过渡",
            "When it comes to why/how {topic_hook}, well,",
            ["topic_hook"]),
        Module("3a", "原因/感受",
            "One is that he/she is really {quality_1}, "
            "and the other is that he/she {quality_2}, which really inspired me.",
            ["quality_1", "quality_2"]),
        Module("3b", "总结",
            "To conclude, he/she holds a special place in my heart.",
            []),
    ],
)


EVENTS = Template(
    category="EVENTS",
    name_zh="事件类",
    modules=[
        Module("1",  "总起",
            "When I saw this topic, the first experience that popped up in my mind was {experience}.",
            ["experience"]),
        Module("1a", "背景",
            "To tell you more about it, {background}.",
            ["background"]),
        Module("2",  "WHAT 过渡",
            "Speaking of what {topic_hook}, well, there are a few things I wanna share with you.",
            ["topic_hook"]),
        Module("2a", "事件 1（低谷/起步）",
            "On the first few days, I found it difficult to {difficulty}. "
            "As a result, I {consequence} and I felt a bit down in the dumps.",
            ["difficulty", "consequence"]),
        Module("2b", "事件 2（转折）",
            "However, as they say, \"There's light at the end of the tunnel.\" "
            "A few days later, {turning_point}.",
            ["turning_point"]),
        Module("2c", "事件 3（成果）",
            "Later on, I began to make progress and {progress}. "
            "Finally, I was over the moon to find out that I {outcome}.",
            ["progress", "outcome"]),
        Module("3",  "WHY/HOW 过渡",
            "When it comes to why/how {topic_hook}, well,",
            ["topic_hook"]),
        Module("3a", "原因/感受",
            "I have mixed feelings about it as it was both a challenging but rewarding process. "
            "I mean, it was such a challenge because {challenge_reason}, "
            "while it was truly rewarding to {reward_reason}.",
            ["challenge_reason", "reward_reason"]),
        Module("3b", "总结",
            "To conclude, this experience is deeply rooted in my mind.",
            []),
    ],
)


ITEMS = Template(
    category="ITEMS",
    name_zh="物品类",
    modules=[
        Module("1",  "总起",
            "When I saw this topic, the first {item_type} that popped up in my mind was {item}.",
            ["item_type", "item"]),
        Module("1a", "介绍",
            "To tell you more about it, {basic_info}.",
            ["basic_info"]),
        Module("1b", "背景",
            "In terms of why/how {acquisition_hook}, actually, {background}.",
            ["acquisition_hook", "background"]),
        Module("2",  "WHAT 过渡",
            "Speaking of what {topic_hook}, well, there are a few things I wanna share with you.",
            ["topic_hook"]),
        Module("2a", "描述 1（外观/客观特征）",
            "Firstly, it {appearance}. In fact, {appearance_detail}.",
            ["appearance", "appearance_detail"]),
        Module("2b", "描述 2（使用体验/乐趣）",
            "Besides, I {usage} and I always have a whale of a time. "
            "To tell you the truth, {usage_detail}.",
            ["usage", "usage_detail"]),
        Module("2c", "描述 3（附加价值/解压）",
            "Apart from those, I also {extra_use}. "
            "Although my agenda is packed and {busy_context}, I can recharge and unwind {unwind_way}.",
            ["extra_use", "busy_context", "unwind_way"]),
        Module("3",  "WHY/HOW 过渡",
            "When it comes to why/how {topic_hook}, well,",
            ["topic_hook"]),
        Module("3a", "原因/感受",
            "It's not only because the {item_short} itself is so useful in every aspect of {life_aspect}, "
            "but also because it holds sentimental value and {sentimental_reason}.",
            ["item_short", "life_aspect", "sentimental_reason"]),
        Module("3b", "总结",
            "To conclude, this {item_short} holds a special place in my heart.",
            ["item_short"]),
    ],
)


TEMPLATES: dict[str, Template] = {
    "PLACES": PLACES,
    "PEOPLE": PEOPLE,
    "EVENTS": EVENTS,
    "ITEMS":  ITEMS,
}


def get_template(category: str) -> Template:
    return TEMPLATES.get(category.upper(), PLACES)
