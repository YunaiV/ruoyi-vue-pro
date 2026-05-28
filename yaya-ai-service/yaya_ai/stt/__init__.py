"""STT (speech-to-text) clients."""

from .base import BaseSTT, STTResult, WordTiming
from .deepgram import DeepgramSTT
from .factory import get_stt
from .openai_whisper import OpenAIWhisperSTT

__all__ = ["BaseSTT", "DeepgramSTT", "STTResult", "WordTiming", "OpenAIWhisperSTT", "get_stt"]

