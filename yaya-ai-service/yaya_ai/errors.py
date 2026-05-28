"""Error layer for yaya_ai.

All callers (e.g. ``apps/api/services/scoring_service``) catch these instead
of provider-specific exceptions, so the orchestration layer doesn't need to
know which SDK raised what.
"""
from __future__ import annotations


class AIServiceError(Exception):
    """Base class for any failure invoking an external AI service.

    Carries an optional ``provider`` (e.g. ``"anthropic"``) and ``status_code``
    so callers can log or branch without parsing strings.
    """

    def __init__(
        self,
        message: str,
        *,
        provider: str = "",
        status_code: int | None = None,
    ):
        super().__init__(message)
        self.provider = provider
        self.status_code = status_code


class AIServiceTimeout(AIServiceError):
    """The request did not complete within the configured timeout."""


class AIServiceRateLimit(AIServiceError):
    """The provider returned a 429 / rate-limit error after retries."""
