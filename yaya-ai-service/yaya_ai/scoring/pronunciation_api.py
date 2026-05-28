"""IELTS speaking scorer (C route) — dedicated pronunciation assessment APIs.

Two interchangeable async clients:

* ``AzurePronunciationScorer`` — Azure Speech Pronunciation Assessment.
  Free F0 tier = 5h audio/month. Returns phoneme-level accuracy.

* ``SpeechAceScorer`` — SpeechAce IELTS-aligned API. 100 free calls.

Both:
- Take credentials at construction (no env reads — D-006 / Settings injection).
- Lazy-import their SDKs at call time so a missing dep on Azure (Mac M-series
  install pain) doesn't break import for the rest of yaya_ai.
- Convert non-WAV/non-supported audio via ``_audio.ensure_format()``, which
  uses a self-deleting tempfile (fixes Top 10 issue #3 — /tmp leak).
- Async by wrapping the underlying sync SDK in ``asyncio.to_thread``.
"""
from __future__ import annotations

import asyncio
import json
from dataclasses import dataclass, field
from pathlib import Path

from .._audio import ensure_format
from .._retry import with_retry


# ── Common shape ─────────────────────────────────────────────────────────


@dataclass
class WordScore:
    word: str
    accuracy: float          # 0-100
    error_type: str = ""     # 'None' | 'Mispronunciation' | 'Omission' | 'Insertion'
    phonemes: list[dict] = field(default_factory=list)


@dataclass
class PronunciationReport:
    overall: float
    accuracy: float
    fluency: float
    completeness: float
    prosody: float = 0.0
    ielts_band: float = 0.0
    words: list[WordScore] = field(default_factory=list)
    provider: str = ""
    raw: dict = field(default_factory=dict, repr=False)

    # Words that are genuinely *recognized* and *attempted* — i.e. those Azure
    # actually heard and graded against the reference. Insertions/Omissions
    # are alignment artifacts (often Whisper-vs-Azure ASR disagreement),
    # not pronunciation defects, so they must NOT show up as "issues".
    _RECOGNIZED_TYPES = {"", "None", "Mispronunciation"}

    def recognized_words(self) -> list[WordScore]:
        return [w for w in self.words if (w.error_type or "None") in self._RECOGNIZED_TYPES]

    def omitted_words(self) -> list[WordScore]:
        """Words present in the reference text but not heard by the pron API."""
        return [w for w in self.words if (w.error_type or "") == "Omission"]

    def inserted_words(self) -> list[WordScore]:
        """Words the pron API heard that weren't in the reference text."""
        return [w for w in self.words if (w.error_type or "") == "Insertion"]

    def top_issue_words(self, n: int = 5, threshold: float = 75.0) -> list[WordScore]:
        """Lowest-accuracy words AMONG genuinely recognized words.

        Excludes Insertion/Omission entries because those have no
        pronunciation to assess — they're ASR-alignment signals.
        """
        issues = [w for w in self.recognized_words() if w.accuracy < threshold]
        return sorted(issues, key=lambda w: w.accuracy)[:n]

    def to_json(self) -> dict:
        return {
            "overall": self.overall,
            "accuracy": self.accuracy,
            "fluency": self.fluency,
            "completeness": self.completeness,
            "prosody": self.prosody,
            "ielts_band": self.ielts_band,
            "provider": self.provider,
            # Phase 3.5 fix: separate Insertion/Omission from real pron data
            "stats": {
                "recognized": len(self.recognized_words()),
                "omitted": len(self.omitted_words()),
                "inserted": len(self.inserted_words()),
                "total": len(self.words),
            },
            "words": [
                {
                    "word": w.word,
                    "accuracy": w.accuracy,
                    "error_type": w.error_type,
                    "phonemes": w.phonemes,
                }
                for w in self.words
            ],
        }


def _to_ielts_band(overall_0_100: float) -> float:
    """Map a 0-100 pron score to an IELTS P band (rough industry anchors)."""
    anchors = [
        (95, 9.0), (90, 8.5), (85, 8.0), (78, 7.5),
        (70, 7.0), (62, 6.5), (55, 6.0), (48, 5.5),
        (40, 5.0), (30, 4.5), (0,  4.0),
    ]
    for threshold, band in anchors:
        if overall_0_100 >= threshold:
            return band
    return 4.0


# ── Azure ────────────────────────────────────────────────────────────────


class AzurePronunciationScorer:
    """Azure Speech Pronunciation Assessment (async wrapper around sync SDK).

    Args:
        key, region: Azure Speech credentials. Required.
        locale: target locale (default ``en-US``).
        sdk_init_timeout: seconds to wait for SDK first import + recognizer
            construction (Top 10 issue #7 — guards against hangs).
        timeout: seconds to wait for the recognize_once_async() result.
        max_retries: tenacity attempts.
    """

    def __init__(
        self,
        *,
        key: str,
        region: str,
        locale: str = "en-US",
        sdk_init_timeout: float = 10.0,
        timeout: float = 60.0,
        max_retries: int = 2,
    ):
        if not key or not region:
            raise ValueError("AzurePronunciationScorer requires key + region")
        self.key = key
        self.region = region
        self.locale = locale
        self.sdk_init_timeout = sdk_init_timeout
        self.timeout = timeout
        self._score = with_retry(max_attempts=max_retries, provider="azure-speech")(
            self._score_raw
        )

    async def _score_raw(
        self,
        audio_path: str | Path,
        reference_text: str,
    ) -> PronunciationReport:
        # Lazy SDK import + bounded init time.
        speechsdk = await asyncio.wait_for(
            asyncio.to_thread(_import_azure_sdk),
            timeout=self.sdk_init_timeout,
        )

        with ensure_format(
            audio_path, target_ext=".wav", sample_rate=16000, mono=True
        ) as wav_path:
            return await asyncio.wait_for(
                asyncio.to_thread(
                    self._run_sync, speechsdk, wav_path, reference_text
                ),
                timeout=self.timeout,
            )

    def _run_sync(self, speechsdk, wav_path: Path, reference_text: str) -> PronunciationReport:
        speech_config = speechsdk.SpeechConfig(subscription=self.key, region=self.region)
        audio_config = speechsdk.audio.AudioConfig(filename=str(wav_path))

        pron_config = speechsdk.PronunciationAssessmentConfig(
            reference_text=reference_text,
            grading_system=speechsdk.PronunciationAssessmentGradingSystem.HundredMark,
            granularity=speechsdk.PronunciationAssessmentGranularity.Phoneme,
            enable_miscue=True,
        )
        try:
            pron_config.enable_prosody_assessment()
        except AttributeError:
            pass

        recognizer = speechsdk.SpeechRecognizer(
            speech_config=speech_config,
            audio_config=audio_config,
            language=self.locale,
        )
        pron_config.apply_to(recognizer)
        result = recognizer.recognize_once_async().get()

        if result.reason != speechsdk.ResultReason.RecognizedSpeech:
            details = getattr(result, "cancellation_details", "") or ""
            raise RuntimeError(
                f"Azure recognition failed: reason={result.reason} details={details}"
            )

        raw = json.loads(
            result.properties.get(
                speechsdk.PropertyId.SpeechServiceResponse_JsonResult
            )
        )
        nbest = raw.get("NBest", [{}])[0]
        pa = nbest.get("PronunciationAssessment", {})

        words = []
        for w in nbest.get("Words", []):
            wpa = w.get("PronunciationAssessment", {})
            phonemes = [
                {
                    "phoneme": p.get("Phoneme", ""),
                    "score": float(
                        p.get("PronunciationAssessment", {}).get("AccuracyScore", 0)
                    ),
                }
                for p in w.get("Phonemes", [])
            ]
            words.append(
                WordScore(
                    word=w.get("Word", ""),
                    accuracy=float(wpa.get("AccuracyScore", 0)),
                    error_type=wpa.get("ErrorType", "None"),
                    phonemes=phonemes,
                )
            )

        overall = float(pa.get("PronScore", 0))
        return PronunciationReport(
            overall=overall,
            accuracy=float(pa.get("AccuracyScore", 0)),
            fluency=float(pa.get("FluencyScore", 0)),
            completeness=float(pa.get("CompletenessScore", 0)),
            prosody=float(pa.get("ProsodyScore", 0)),
            ielts_band=_to_ielts_band(overall),
            words=words,
            provider="azure",
            raw=raw,
        )

    async def score(
        self,
        audio_path: str | Path,
        reference_text: str,
    ) -> PronunciationReport:
        return await self._score(audio_path, reference_text)


def _import_azure_sdk():  # pragma: no cover — import side-effect
    import azure.cognitiveservices.speech as speechsdk
    return speechsdk


# ── SpeechAce (HTTP) ─────────────────────────────────────────────────────


class SpeechAceScorer:
    """SpeechAce IELTS Scoring API (async via to_thread)."""

    BASE_URL = "https://api.speechace.co/api/scoring/speech/v9/json"

    def __init__(
        self,
        *,
        api_key: str,
        dialect: str = "en-us",
        user_id: str = "anonymous",
        timeout: float = 120.0,
        max_retries: int = 2,
    ):
        if not api_key:
            raise ValueError("SpeechAceScorer requires api_key")
        self.api_key = api_key
        self.dialect = dialect
        self.user_id = user_id
        self.timeout = timeout
        self._score = with_retry(max_attempts=max_retries, provider="speechace")(
            self._score_raw
        )

    async def _score_raw(
        self,
        audio_path: str | Path,
        reference_text: str,
    ) -> PronunciationReport:
        return await asyncio.to_thread(self._run_sync, audio_path, reference_text)

    def _run_sync(
        self, audio_path: str | Path, reference_text: str
    ) -> PronunciationReport:
        import requests

        path = Path(audio_path)
        params = {
            "key": self.api_key,
            "dialect": self.dialect,
            "user_id": self.user_id,
            "include_ielts_subscore": 1,
        }
        data = {"text": reference_text}
        with path.open("rb") as f:
            files = {"user_audio_file": (path.name, f)}
            resp = requests.post(
                self.BASE_URL,
                params=params,
                data=data,
                files=files,
                timeout=self.timeout,
            )
        resp.raise_for_status()
        raw = resp.json()
        if raw.get("status") != "success":
            raise RuntimeError(f"SpeechAce API error: {raw}")

        ts = raw.get("text_score", {})
        ielts = ts.get("ielts_score", {})
        overall_0_100 = float(
            ielts.get("pronunciation", 0) or ts.get("quality_score", 0)
        )
        ielts_band = (
            float(ielts.get("pronunciation", 0))
            if ielts
            else _to_ielts_band(overall_0_100)
        )
        words = []
        for w in ts.get("word_score_list", []):
            words.append(
                WordScore(
                    word=w.get("word", ""),
                    accuracy=float(w.get("quality_score", 0)),
                    error_type="",
                    phonemes=[
                        {
                            "phoneme": p.get("phone", ""),
                            "score": float(p.get("quality_score", 0)),
                        }
                        for p in w.get("phone_score_list", [])
                    ],
                )
            )
        fluency = float(ielts.get("fluency", 0)) if ielts else 0.0
        return PronunciationReport(
            overall=overall_0_100,
            accuracy=overall_0_100,
            fluency=fluency,
            completeness=100.0,
            prosody=0.0,
            ielts_band=ielts_band,
            words=words,
            provider="speechace",
            raw=raw,
        )

    async def score(
        self,
        audio_path: str | Path,
        reference_text: str,
    ) -> PronunciationReport:
        return await self._score(audio_path, reference_text)
