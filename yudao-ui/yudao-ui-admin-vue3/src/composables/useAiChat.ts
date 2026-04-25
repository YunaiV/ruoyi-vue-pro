import { ref, nextTick } from 'vue'
import { AiChatApi, type ChatMeta, type ChatReply } from '@/api/ai/chat'
import { uid } from '@/utils/uid'

// ============================================================
// Types
// ============================================================

export type MessageRole = 'ai' | 'user'

export interface ChatMessage {
  id: string
  role: MessageRole
  content: string
  streaming?: boolean
  images?: string[]
  quickReplies?: string[]
}

export interface UseAiChatOptions {
  module: string
  customerId?: number
  /** Enable typewriter animation (SSE). Default true. */
  typewriter?: boolean | (() => boolean)
  /** Ref to the scroll container for auto-scroll */
  scrollEl?: () => HTMLElement | null | undefined
  /** Initial greeting message */
  greeting?: string
  /** Persist sessionId in localStorage under this key */
  persistKey?: string
}

// ============================================================
// Composable
// ============================================================

export function useAiChat(options: UseAiChatOptions) {
  const { module, customerId, scrollEl, greeting, persistKey } = options

  // ── State ──────────────────────────────────────────────────
  const messages    = ref<ChatMessage[]>([])
  const loading     = ref(false)
  const suggestions = ref<string[]>([])

  // Restore sessionId from localStorage (persist across page reloads)
  const storageKey = persistKey ?? `ai_session_${module}`
  const _savedSid  = typeof localStorage !== 'undefined' ? localStorage.getItem(storageKey) : null
  const sessionId  = ref<string | undefined>(_savedSid ?? undefined)

  let currentEs: EventSource | null = null

  // ── Typewriter getter ───────────────────────────────────────
  function isTypewriter(): boolean {
    const t = options.typewriter
    if (t === undefined) return true
    return typeof t === 'function' ? t() : !!t
  }

  // ── Greeting ────────────────────────────────────────────────
  /** Add greeting bubble (called by component on first open) */
  function showGreeting() {
    if (greeting && messages.value.length === 0) {
      messages.value.push({ id: 'greeting', role: 'ai', content: greeting })
    }
  }

  // ── Send message ────────────────────────────────────────────
  async function send(text: string): Promise<void> {
    if (!text.trim() || loading.value) return
    cancel()
    suggestions.value = []
    pushMsg({ role: 'user', content: text })
    loading.value = true
    scrollToBottom()

    if (isTypewriter()) {
      await _sendStream(text)
    } else {
      await _sendRest(text)
    }
  }

  // ── SSE stream ──────────────────────────────────────────────
  async function _sendStream(text: string): Promise<void> {
    return new Promise((resolve) => {
      const msgId  = uid()
      messages.value.push({ id: msgId, role: 'ai', content: '', streaming: true })
      scrollToBottom()

      currentEs = AiChatApi.streamMessage(
        { module, sessionId: sessionId.value, customerId, userMessage: text },
        {
          onToken: (char) => {
            const m = _find(msgId)
            if (m) { m.content += char; scrollToBottom() }
          },
          onMeta: (meta: ChatMeta) => {
            _saveSession(meta.sessionId)
            const m = _find(msgId)
            if (m) {
              m.streaming    = false
              m.quickReplies = meta.quickReplies
              m.images       = meta.images
            }
          },
          onError: (_err) => {
            const m = _find(msgId)
            if (m) { m.content = m.content || '抱歉，出现了一点小问题，请稍后重试。'; m.streaming = false }
            loading.value = false; resolve()
          },
          onClose: () => {
            const m = _find(msgId)
            if (m) m.streaming = false
            loading.value = false; currentEs = null; scrollToBottom(); resolve()
          },
        }
      )
    })
  }

  // ── REST fallback ───────────────────────────────────────────
  async function _sendRest(text: string): Promise<void> {
    try {
      const reply: ChatReply = await AiChatApi.sendMessage({
        module, sessionId: sessionId.value, customerId, userMessage: text,
      })
      _saveSession(reply.sessionId)
      pushMsg({ role: 'ai', content: reply.aiMessage, images: reply.images, quickReplies: reply.quickReplies })
    } catch {
      pushMsg({ role: 'ai', content: '抱歉，网络出现了点小问题，请稍后重试。' })
    } finally {
      loading.value = false; scrollToBottom()
    }
  }

  // ── Quick reply ─────────────────────────────────────────────
  function quickReply(text: string): void {
    const match = text.match(/^.+?\((.+?)\)$/)
    send(match ? match[1] : text)
  }

  // ── Autocomplete ────────────────────────────────────────────
  let _debounce: ReturnType<typeof setTimeout> | null = null

  function updateSuggestions(prefix: string): void {
    if (!prefix.trim()) { suggestions.value = []; return }
    if (_debounce) clearTimeout(_debounce)
    _debounce = setTimeout(async () => {
      try { suggestions.value = await AiChatApi.autocomplete(prefix) }
      catch { suggestions.value = [] }
    }, 200)
  }

  function clearSuggestions(): void { suggestions.value = [] }

  // ── Session ─────────────────────────────────────────────────
  async function endSession(): Promise<void> {
    cancel()
    if (sessionId.value) {
      try { await AiChatApi.endSession(sessionId.value) } catch { /* ignore */ }
    }
    sessionId.value = undefined
    if (typeof localStorage !== 'undefined') localStorage.removeItem(storageKey)
    messages.value = []
  }

  function cancel(): void {
    if (currentEs) {
      currentEs.close(); currentEs = null
      const last = [...messages.value].reverse().find(m => m.role === 'ai')
      if (last) last.streaming = false
      loading.value = false
    }
    if (_debounce) { clearTimeout(_debounce); _debounce = null }
  }

  // ── Helpers ─────────────────────────────────────────────────
  function pushMsg(msg: Omit<ChatMessage, 'id'>): void {
    messages.value.push({ id: uid(), ...msg })
  }

  function _find(id: string): ChatMessage | undefined {
    return messages.value.find(m => m.id === id)
  }

  function _saveSession(sid: string | undefined): void {
    if (!sid) return
    sessionId.value = sid
    if (typeof localStorage !== 'undefined') localStorage.setItem(storageKey, sid)
  }

  function scrollToBottom(): void {
    nextTick(() => { const el = scrollEl?.(); if (el) el.scrollTop = el.scrollHeight })
  }

  // uid imported from @/utils/uid

  // ── Public API ───────────────────────────────────────────────
  return {
    messages,      // writable ref
    loading,
    sessionId,
    suggestions,
    showGreeting,
    send,
    quickReply,
    updateSuggestions,
    clearSuggestions,
    endSession,
    cancel,
  }
}
