"""Deepgram STT — async cloud transcription with word-level timestamps."""
from __future__ import annotations

import json
from pathlib import Path

import httpx

from .._retry import with_retry
from .base import BaseSTT, STTResult, WordTiming

_MIME_BY_EXT = {
    ".m4a": "audio/mp4",
    ".mp4": "audio/mp4",
    ".mp3": "audio/mpeg",
    ".wav": "audio/wav",
    ".ogg": "audio/ogg",
    ".flac": "audio/flac",
    ".webm": "audio/webm",
}


class DeepgramSTT(BaseSTT):
    def __init__(
        self,
        *,
        api_key: str,
        model: str = "nova-3",
        smart_format: bool = True,
        punctuate: bool = True,
        utterances: bool = False,
        timeout: float = 180.0,
        max_retries: int = 3,
    ):
        if not api_key:
            raise ValueError("DeepgramSTT requires api_key")
        self.api_key = api_key
        self.model = model
        self.smart_format = smart_format
        self.punctuate = punctuate
        self.utterances = utterances
        self.timeout = timeout
        self._provider = f"deepgram-{model}"
        self._transcribe = with_retry(
            max_attempts=max_retries, provider="deepgram"
        )(self._transcribe_raw)

    async def _transcribe_raw(
        self,
        audio_path: str | Path,
        language: str | None = None,
        prompt: str | None = None,
    ) -> STTResult:
        del prompt  # Deepgram has no Whisper-style prompt
        path = Path(audio_path)
        mime = _MIME_BY_EXT.get(path.suffix.lower(), "audio/mp4")
        audio_bytes = path.read_bytes()

        params: list[str] = [f"model={self.model}"]
        if self.punctuate:
            params.append("punctuate=true")
        if self.smart_format:
            params.append("smart_format=true")
        if self.utterances:
            params.append("utterances=true")
        if language:
            params.append(f"language={language}")
        url = "https://api.deepgram.com/v1/listen?" + "&".join(params)

        async with httpx.AsyncClient(timeout=self.timeout) as client:
            response = await client.post(
                url,
                content=audio_bytes,
                headers={
                    "Authorization": f"Token {self.api_key}",
                    "Content-Type": mime,
                },
            )
            response.raise_for_status()
            data = response.json()

        ch = data["results"]["channels"][0]["alternatives"][0]
        words = [
            WordTiming(
                word=w.get("punctuated_word") or w["word"],
                start=float(w["start"]),
                end=float(w["end"]),
                probability=float(w.get("confidence", 1.0)),
            )
            for w in ch.get("words", [])
        ]
        duration = float(data.get("metadata", {}).get("duration") or 0) or None
        return STTResult(
            text=(ch.get("transcript") or "").strip(),
            language=ch.get("detected_language") or language,
            confidence=float(ch.get("confidence", 0.0)) or None,
            provider=self._provider,
            words=words,
            audio_duration=duration,
        )

    async def transcribe(
        self,
        audio_path: str | Path,
        language: str | None = None,
        prompt: str | None = None,
    ) -> STTResult:
        return await self._transcribe(audio_path, language=language, prompt=prompt)
