import { ref, readonly, nextTick } from 'vue'
import { AiChatApi, type ChatMeta, type ChatReply } from '@/api/ai/chat'

// ============================================================
// 消息类型
// ============================================================

export type MessageRole = 'ai' | 'user'

export interface ChatMessage {
  id: string
  role: MessageRole
  /** 消息内容（流式时逐字追加） */
  content: string
  /** 是否正在流式输出 */
  streaming?: boolean
  images?: string[]
  quickReplies?: string[]
}

export interface UseAiChatOptions {
  /** 板块标识 */
  module: string
  /** 用户/客户 ID */
  customerId?: number
  /** 是否启用打字机动画（默认 true） */
  typewriter?: boolean
  /** 消息列表容器（用于自动滚动）*/
  scrollEl?: () => HTMLElement | null | undefined
}

// ============================================================
// Composable
// ============================================================

/**
 * AI 对话核心 Composable。
 *
 * 支持：
 * - SSE 流式输出（打字机动画，可关闭）
 * - REST 模式（立即显示完整回复）
 * - 自动补全
 * - 快速回复按钮
 * - 多轮会话（sessionId 自动保持）
 *
 * @example
 * const chat = useAiChat({ module: 'selection' })
 * await chat.send('我想做外套')
 * // messages.value 自动更新
 */
export function useAiChat(options: UseAiChatOptions) {
  const {
    module,
    customerId,
    typewriter = true,
    scrollEl,
  } = options

  // ---- 状态 ----------------------------------------------------------------
  const messages    = ref<ChatMessage[]>([])
  const loading     = ref(false)
  const sessionId   = ref<string | undefined>()
  const suggestions = ref<string[]>([])

  /** 当前 SSE 连接（需要时可调用 cancel() 中断） */
  let currentEs: EventSource | null = null

  // ---- 发送消息 -------------------------------------------------------------

  /**
   * 发送一条用户消息并获取 AI 回复。
   * 自动选择 SSE 或 REST 模式。
   *
   * @param text 用户输入文本
   */
  async function send(text: string): Promise<void> {
    if (!text.trim() || loading.value) return

    // 取消上一条未完成的流
    cancel()
    suggestions.value = []

    // 追加用户气泡
    pushMessage({ role: 'user', content: text })
    loading.value = true
    scrollToBottom()

    if (typewriter) {
      await sendStream(text)
    } else {
      await sendRest(text)
    }
  }

  // ---- SSE 流式模式 ---------------------------------------------------------

  async function sendStream(text: string): Promise<void> {
    return new Promise((resolve) => {
      // 创建空的 AI 气泡（流式时追加 content）
      const msgId  = uid()
      const aiMsg: ChatMessage = { id: msgId, role: 'ai', content: '', streaming: true }
      messages.value.push(aiMsg)
      scrollToBottom()

      currentEs = AiChatApi.streamMessage(
        {
          module,
          sessionId: sessionId.value,
          customerId,
          userMessage: text,
        },
        {
          onToken: (char) => {
            const m = findMessage(msgId)
            if (m) {
              m.content += char
              scrollToBottom()
            }
          },
          onMeta: (meta: ChatMeta) => {
            sessionId.value = meta.sessionId
            const m = findMessage(msgId)
            if (m) {
              m.streaming    = false
              m.quickReplies = meta.quickReplies
              m.images       = meta.images
            }
          },
          onError: (errMsg) => {
            const m = findMessage(msgId)
            if (m) {
              m.content   = m.content || '抱歉，出现了一点小问题，请稍后重试。'
              m.streaming = false
            }
            loading.value = false
            resolve()
          },
          onClose: () => {
            const m = findMessage(msgId)
            if (m) m.streaming = false
            loading.value  = false
            currentEs      = null
            scrollToBottom()
            resolve()
          },
        }
      )
    })
  }

  // ---- REST 模式 ------------------------------------------------------------

  async function sendRest(text: string): Promise<void> {
    try {
      const reply: ChatReply = await AiChatApi.sendMessage({
        module,
        sessionId: sessionId.value,
        customerId,
        userMessage: text,
      })

      sessionId.value = reply.sessionId
      pushMessage({
        role:         'ai',
        content:      reply.aiMessage,
        images:       reply.images,
        quickReplies: reply.quickReplies,
      })
    } catch {
      pushMessage({ role: 'ai', content: '抱歉，网络出现了点小问题，请稍后重试。' })
    } finally {
      loading.value = false
      scrollToBottom()
    }
  }

  // ---- 快速回复 -------------------------------------------------------------

  /**
   * 处理快速回复按钮点击。
   * 自动解析 "显示文本(VALUE)" 格式，提取括号内的值。
   */
  function quickReply(text: string): void {
    const match = text.match(/^.+?\((.+?)\)$/)
    const value = match ? match[1] : text
    send(value)
  }

  // ---- 自动补全 -------------------------------------------------------------

  let debounceTimer: ReturnType<typeof setTimeout> | null = null

  /**
   * 根据用户已输入文本刷新自动补全候选词。
   * 带 200ms 防抖。
   */
  function updateSuggestions(prefix: string): void {
    if (!prefix.trim()) {
      suggestions.value = []
      return
    }
    if (debounceTimer) clearTimeout(debounceTimer)
    debounceTimer = setTimeout(async () => {
      try {
        suggestions.value = await AiChatApi.autocomplete(prefix)
      } catch {
        suggestions.value = []
      }
    }, 200)
  }

  function clearSuggestions(): void {
    suggestions.value = []
  }

  // ---- 会话管理 -------------------------------------------------------------

  /**
   * 结束并清除当前会话。
   */
  async function endSession(): Promise<void> {
    cancel()
    if (sessionId.value) {
      try { await AiChatApi.endSession(sessionId.value) } catch { /* ignore */ }
      sessionId.value = undefined
    }
    messages.value = []
  }

  /**
   * 主动取消当前 SSE 流。
   */
  function cancel(): void {
    if (currentEs) {
      currentEs.close()
      currentEs = null
      // 标记最后一条 AI 消息为非流式
      const last = [...messages.value].reverse().find(m => m.role === 'ai')
      if (last) last.streaming = false
      loading.value = false
    }
    if (debounceTimer) {
      clearTimeout(debounceTimer)
      debounceTimer = null
    }
  }

  // ---- 工具 ----------------------------------------------------------------

  function pushMessage(msg: Omit<ChatMessage, 'id'>): void {
    messages.value.push({ id: uid(), ...msg })
  }

  function findMessage(id: string): ChatMessage | undefined {
    return messages.value.find(m => m.id === id)
  }

  function scrollToBottom(): void {
    nextTick(() => {
      const el = scrollEl?.()
      if (el) el.scrollTop = el.scrollHeight
    })
  }

  function uid(): string {
    return Date.now().toString(36) + Math.random().toString(36).slice(2, 6)
  }

  // ---- 返回公开 API --------------------------------------------------------

  return {
    /** 消息列表（响应式，只读引用） */
    messages: readonly(messages) as typeof messages,
    /** 是否正在等待 AI 回复 */
    loading:     readonly(loading),
    /** 当前会话 ID */
    sessionId:   readonly(sessionId),
    /** 自动补全候选词 */
    suggestions: readonly(suggestions),

    send,
    quickReply,
    updateSuggestions,
    clearSuggestions,
    endSession,
    cancel,
  }
}
