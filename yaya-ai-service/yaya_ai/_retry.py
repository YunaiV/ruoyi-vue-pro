"""Retry / timeout wrapper for AI service calls.

Wraps async functions with tenacity's exponential backoff, and converts
provider-specific exceptions into the unified ``AIServiceError`` hierarchy.

Usage::

    @with_retry(max_attempts=3)
    async def my_call(...): ...

The wrapped function will be retried up to ``max_attempts`` times on
transient errors (timeout, rate limit, connection error). Final failure
re-raises as ``AIServiceTimeout`` / ``AIServiceRateLimit`` / ``AIServiceError``.
"""
from __future__ import annotations

import asyncio
from collections.abc import Awaitable, Callable
from functools import wraps
from typing import Any, TypeVar

from tenacity import (
    AsyncRetrying,
    RetryError,
    retry_if_exception_type,
    stop_after_attempt,
    wait_exponential,
)

from .errors import AIServiceError, AIServiceRateLimit, AIServiceTimeout


T = TypeVar("T")


# Provider exception types we treat as transient. Imported lazily because not
# every consumer of yaya_ai will have every SDK installed.
def _classify(exc: BaseException, *, provider: str) -> AIServiceError:
    """Map an arbitrary provider exception to our error hierarchy."""
    name = type(exc).__name__.lower()
    status = getattr(exc, "status_code", None)
    msg = str(exc) or name

    if "timeout" in name or isinstance(exc, asyncio.TimeoutError):
        return AIServiceTimeout(msg, provider=provider, status_code=status)
    if "ratelimit" in name or status == 429:
        return AIServiceRateLimit(msg, provider=provider, status_code=status)
    return AIServiceError(msg, provider=provider, status_code=status)


def _is_transient(exc: BaseException) -> bool:
    """Whether to retry on this exception type."""
    if isinstance(exc, (AIServiceTimeout, AIServiceRateLimit)):
        return True
    if isinstance(exc, asyncio.TimeoutError):
        return True
    name = type(exc).__name__.lower()
    if "timeout" in name or "ratelimit" in name or "connection" in name:
        return True
    status = getattr(exc, "status_code", None)
    if status in (408, 429, 500, 502, 503, 504):
        return True
    return False


def with_retry(
    *,
    max_attempts: int = 3,
    initial_wait: float = 1.0,
    max_wait: float = 30.0,
    provider: str = "",
) -> Callable[[Callable[..., Awaitable[T]]], Callable[..., Awaitable[T]]]:
    """Decorator: retry async function on transient errors with exp backoff."""

    def decorator(fn: Callable[..., Awaitable[T]]) -> Callable[..., Awaitable[T]]:
        @wraps(fn)
        async def wrapper(*args: Any, **kwargs: Any) -> T:
            try:
                async for attempt in AsyncRetrying(
                    stop=stop_after_attempt(max_attempts),
                    wait=wait_exponential(multiplier=initial_wait, max=max_wait),
                    retry=retry_if_exception_type(_TransientShim),
                    reraise=True,
                ):
                    with attempt:
                        try:
                            return await fn(*args, **kwargs)
                        except Exception as exc:  # noqa: BLE001
                            if _is_transient(exc):
                                # Re-raise as a sentinel so tenacity picks it up,
                                # while preserving the underlying exception.
                                raise _TransientShim(exc, provider=provider) from exc
                            # Non-transient: classify and bail without retry
                            raise _classify(exc, provider=provider) from exc
            except _TransientShim as final:
                # All retries exhausted on transient error; convert to public
                # error type and raise.
                raise _classify(final.original, provider=provider) from final.original
            except RetryError as re:  # pragma: no cover — safety net
                last = re.last_attempt.exception()
                if isinstance(last, _TransientShim):
                    raise _classify(last.original, provider=provider) from last.original
                raise AIServiceError(
                    "retry exhausted with unexpected exception",
                    provider=provider,
                ) from re

            # Unreachable, but mypy needs a return.
            raise AIServiceError("retry loop exited unexpectedly", provider=provider)

        return wrapper

    return decorator


class _TransientShim(Exception):
    """Internal sentinel for transient errors, used so tenacity can retry on
    a single exception type while we keep the original around.
    """

    def __init__(self, original: BaseException, *, provider: str = ""):
        super().__init__(str(original))
        self.original = original
        self.provider = provider
