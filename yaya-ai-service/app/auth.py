"""Internal request authentication for Java-to-Python calls."""

from __future__ import annotations

import os
from hmac import compare_digest

from fastapi import Header, HTTPException


def expected_internal_key() -> str:
    return os.getenv("YAYA_INTERNAL_KEY", "local-internal-key")


async def require_internal_auth(
    internal_key: str | None = Header(default=None, alias="X-Yaya-Internal-Key"),
    request_id: str | None = Header(default=None, alias="X-Yaya-Request-Id"),
) -> str:
    if not request_id:
        raise HTTPException(status_code=400, detail="missing X-Yaya-Request-Id")
    expected = expected_internal_key()
    if not internal_key or not compare_digest(internal_key, expected):
        raise HTTPException(status_code=401, detail="invalid X-Yaya-Internal-Key")
    return request_id
