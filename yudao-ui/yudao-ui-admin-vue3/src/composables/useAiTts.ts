/**
 * useAiTts — Web Speech API 语音合成 Composable
 *
 * 功能：
 * - AI 回复文字转语音播报（中文）
 * - 支持暂停 / 继续 / 停止
 * - 音量、语速、音调可配置
 * - 自动跳过 HTML 标签和 Emoji
 *
 * 使用方式：
 * ```ts
 * const tts = useAiTts()
 * tts.speak('你好，我是 AI 助手')
 * tts.stop()
 * ```
 */
import { ref, onUnmounted } from 'vue'

export interface TtsOptions {
  lang?:   string   // default 'zh-CN'
  rate?:   number   // 0.5 ~ 2.0, default 1.0
  pitch?:  number   // 0 ~ 2,   default 1.0
  volume?: number   // 0 ~ 1,   default 1.0
}

export function useAiTts(defaults: TtsOptions = {}) {
  const speaking  = ref(false)
  const supported = ref(typeof window !== 'undefined' && !!window.speechSynthesis)

  let utterance: SpeechSynthesisUtterance | null = null

  /** Strip HTML tags, emoji blocks, markdown, and whitespace */
  function clean(text: string): string {
    return text
      .replace(/<[^>]+>/g, '')                                    // HTML tags
      .replace(/\*\*(.+?)\*\*/g, '$1')                            // bold
      .replace(/[^\u0000-\u00FF\u4E00-\u9FFF\u3000-\u303F\u30A0-\u30FF\uFF00-\uFFEF\s\d,.!?;:'"()%¥$]/g, '') // keep CJK + ascii
      .replace(/\s+/g, ' ')
      .trim()
  }

  function speak(text: string, options: TtsOptions = {}): void {
    if (!supported.value) return
    stop()

    const cleaned = clean(text)
    if (!cleaned) return

    utterance           = new SpeechSynthesisUtterance(cleaned)
    utterance.lang      = options.lang   ?? defaults.lang   ?? 'zh-CN'
    utterance.rate      = options.rate   ?? defaults.rate   ?? 1.0
    utterance.pitch     = options.pitch  ?? defaults.pitch  ?? 1.0
    utterance.volume    = options.volume ?? defaults.volume ?? 1.0

    utterance.onstart = () => { speaking.value = true }
    utterance.onend   = () => { speaking.value = false; utterance = null }
    utterance.onerror = () => { speaking.value = false; utterance = null }

    // Pick a Chinese voice if available
    const voices = window.speechSynthesis.getVoices()
    const zhVoice = voices.find(v =>
      v.lang.startsWith('zh') && (v.name.includes('Female') || v.name.includes('Ting-Ting') || v.name.includes('Tingting'))
    ) ?? voices.find(v => v.lang.startsWith('zh'))
    if (zhVoice) utterance.voice = zhVoice

    window.speechSynthesis.speak(utterance)
  }

  function stop(): void {
    if (!supported.value) return
    window.speechSynthesis.cancel()
    speaking.value = false
    utterance = null
  }

  function pause(): void {
    if (!supported.value) return
    window.speechSynthesis.pause()
  }

  function resume(): void {
    if (!supported.value) return
    window.speechSynthesis.resume()
  }

  onUnmounted(stop)

  return { speaking, supported, speak, stop, pause, resume }
}
