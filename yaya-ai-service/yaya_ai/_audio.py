"""Audio file utilities shared across scorers.

* ``encode_audio_inline`` — read an audio file and return base64 content for
  inline inclusion in chat completions (OpenAI / Gemini formats).
* ``ensure_format`` — convert audio to a target format (mp3/wav) using ffmpeg
  via a self-deleting temp file. Always returns a Path that the caller is
  expected to use immediately, then let the temp file be cleaned up.

The temp file is **always deleted** when the helper context is exited — this
fixes the /tmp leak from the demo (Top 10 issue #3).
"""
from __future__ import annotations

import base64
import shutil
import subprocess
import tempfile
from contextlib import contextmanager
from pathlib import Path
from typing import Iterator


SUPPORTED_INLINE = {".mp3", ".wav"}


def _have_ffmpeg() -> bool:
    return shutil.which("ffmpeg") is not None


@contextmanager
def ensure_format(
    audio_path: str | Path,
    *,
    target_ext: str,
    sample_rate: int | None = None,
    mono: bool = False,
) -> Iterator[Path]:
    """Yield a Path to a temp file in ``target_ext`` (e.g. ``.mp3`` / ``.wav``).

    If the input is already in target format, yield the original path.
    Otherwise convert via ffmpeg into a NamedTemporaryFile that is deleted
    when the context exits.
    """
    src = Path(audio_path).resolve()
    target_ext = target_ext.lower()
    if not target_ext.startswith("."):
        target_ext = "." + target_ext

    if src.suffix.lower() == target_ext and sample_rate is None and not mono:
        yield src
        return

    if not _have_ffmpeg():
        raise RuntimeError(
            f"Audio at {src.name} needs conversion to {target_ext} but ffmpeg "
            "is not on PATH. Install it (brew install ffmpeg) or upload audio "
            "in a supported format."
        )

    with tempfile.NamedTemporaryFile(
        prefix="yaya-ai-", suffix=target_ext, delete=True
    ) as tmp:
        cmd: list[str] = ["ffmpeg", "-y", "-loglevel", "error", "-i", str(src)]
        if mono:
            cmd += ["-ac", "1"]
        if sample_rate is not None:
            cmd += ["-ar", str(sample_rate)]
        if target_ext == ".wav":
            cmd += ["-acodec", "pcm_s16le"]
        cmd.append(tmp.name)

        proc = subprocess.run(cmd, capture_output=True, text=True)
        if proc.returncode != 0:
            raise RuntimeError(
                f"ffmpeg conversion {src.suffix} -> {target_ext} failed:\n{proc.stderr}"
            )
        yield Path(tmp.name)


def encode_audio_inline(audio_path: str | Path) -> tuple[str, str]:
    """Return (base64_data, format_str) for OpenAI chat audio messages.

    Format string is the file extension without the leading dot, e.g. ``"mp3"``.
    """
    p = Path(audio_path)
    data = p.read_bytes()
    fmt = p.suffix.lower().lstrip(".") or "wav"
    return base64.b64encode(data).decode("ascii"), fmt
