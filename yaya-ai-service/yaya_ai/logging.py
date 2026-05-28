"""Structured logging for yaya_ai.

Uses ``structlog`` with a JSON renderer for production / log-aggregator
friendliness, falling back to a key-value renderer when stdout is a TTY
(easier to read during local dev).

Usage::

    from yaya_ai.logging import get_logger
    log = get_logger(__name__)
    log.info("evaluation_done", evaluation_id=str(eid), overall=5.5)

Idempotent: ``get_logger`` configures structlog on first call only.
"""
from __future__ import annotations

import logging
import sys

import structlog


_configured = False


def _configure() -> None:
    global _configured
    if _configured:
        return

    is_tty = sys.stdout.isatty()

    shared_processors = [
        structlog.contextvars.merge_contextvars,
        structlog.processors.add_log_level,
        structlog.processors.TimeStamper(fmt="iso", utc=True),
        structlog.processors.StackInfoRenderer(),
        structlog.processors.format_exc_info,
    ]

    if is_tty:
        renderer = structlog.dev.ConsoleRenderer(colors=True)
    else:
        renderer = structlog.processors.JSONRenderer()

    structlog.configure(
        processors=shared_processors + [renderer],
        wrapper_class=structlog.make_filtering_bound_logger(logging.INFO),
        context_class=dict,
        logger_factory=structlog.PrintLoggerFactory(),
        cache_logger_on_first_use=True,
    )

    _configured = True


def get_logger(name: str | None = None) -> structlog.stdlib.BoundLogger:
    """Return a configured structlog logger bound to ``name`` (typically __name__)."""
    _configure()
    return structlog.get_logger(name)
