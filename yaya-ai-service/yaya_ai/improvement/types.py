"""Shared types for chunk-based improvement."""
from __future__ import annotations

from dataclasses import dataclass, field


@dataclass
class ChunkCandidate:
    """One chunk surfaced from the RAG store as a candidate for substitution."""
    id: str
    text: str
    translation: str = ""
    categories: list[str] = field(default_factory=list)
    granularity: str = ""
    similarity: float = 0.0


@dataclass
class ChunkSuggestion:
    """One concrete suggestion the LLM produced and that we have validated
    references a real chunk in our DB."""
    chunk_id: str
    original_phrase: str
    suggested_chunk: str
    chunk_translation: str
    chunk_categories: list[str]
    reason: str
    sentence_context: str = ""

    def to_dict(self) -> dict:
        return {
            "chunk_id": self.chunk_id,
            "original_phrase": self.original_phrase,
            "suggested_chunk": self.suggested_chunk,
            "chunk_translation": self.chunk_translation,
            "chunk_categories": self.chunk_categories,
            "reason": self.reason,
            "sentence_context": self.sentence_context,
        }


@dataclass
class ImprovementReport:
    suggestions: list[ChunkSuggestion] = field(default_factory=list)
    rejected_count: int = 0           # how many LLM picks failed validation
    candidate_count: int = 0          # total chunks shown to the LLM
    sentences_searched: int = 0
    raw_response: str = ""

    def to_dict(self) -> dict:
        return {
            "suggestions": [s.to_dict() for s in self.suggestions],
            "rejected_count": self.rejected_count,
            "candidate_count": self.candidate_count,
            "sentences_searched": self.sentences_searched,
        }
