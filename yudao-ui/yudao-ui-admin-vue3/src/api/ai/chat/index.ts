import request from '@/config/axios'

// ============================================================
// 类型定义
// ============================================================

/** AI 回复结构（REST 接口返回）*/
export interface ChatReply {
  aiMessage: string
  pendingField?: string
  quickReplies?: string[]
  images?: string[]
  done: boolean
  sessionId: string
}

/** SSE 元数据事件（streaming 接口推送）*/
export interface ChatMeta {
  sessionId: string
  pendingField?: string
  quickReplies?: string[]
  images?: string[]
  done: boolean
}

/** 发消息请求体 */
export interface ChatMessageReq {
  /** selection / design / product / inventory / finance / trend / order */
  module: string
  /** 首次可不传，后续必传 */
  sessionId?: string
  customerId?: number
  userMessage: string
}

/** SSE 流式对话回调 */
export interface StreamCallbacks {
  /** 每次收到新 token（追加字符）*/
  onToken: (char: string) => void
  /** 收到完整元数据（pendingField, quickReplies, images, done, sessionId）*/
  onMeta: (meta: ChatMeta) => void
  /** 出错 */
  onError?: (msg: string) => void
  /** 连接关闭 */
  onClose?: () => void
}

// ============================================================
// REST API
// ============================================================

export const AiChatApi = {
  /**
   * 发送对话消息（REST，非流式）。
   * 适合快速响应场景，无打字机效果。
   */
  sendMessage: async (data: ChatMessageReq): Promise<ChatReply> => {
    return await request.post({ url: '/deepay/chat/message', data })
  },

  /**
   * 输入框自动补全（最多返回 8 个候选词）。
   * @param prefix 已输入的前缀
   */
  autocomplete: async (prefix: string): Promise<string[]> => {
    return await request.get({ url: '/deepay/chat/autocomplete', params: { prefix } })
  },

  /**
   * 结束对话会话（清除 Redis 中的 Context）。
   */
  endSession: async (sessionId: string): Promise<void> => {
    await request.delete({ url: `/deepay/chat/session/${sessionId}` })
  },

  // ============================================================
  // SSE 流式 API
  // ============================================================

  /**
   * 开始 SSE 流式对话（打字机效果）。
   *
   * @param params   请求参数
   * @param cbs      事件回调
   * @returns        EventSource 实例（可调用 .close() 主动断开）
   *
   * @example
   * const es = AiChatApi.streamMessage({ module: 'selection', userMessage: '我想做外套' }, {
   *   onToken: char  => appendChar(char),
   *   onMeta:  meta  => updateMeta(meta),
   *   onClose: ()    => markDone(),
   * })
   * // 主动取消：es.close()
   */
  streamMessage(params: ChatMessageReq, cbs: StreamCallbacks): EventSource {
    const qs = new URLSearchParams()
    qs.set('module',      params.module)
    qs.set('userMessage', params.userMessage)
    if (params.sessionId)   qs.set('sessionId',  params.sessionId)
    if (params.customerId != null) qs.set('customerId', String(params.customerId))

    // Use relative URL so it goes through the same base path as REST calls
    const url = `/deepay/chat/stream?${qs.toString()}`
    const es  = new EventSource(url, { withCredentials: true })

    es.addEventListener('token', (e: MessageEvent) => {
      cbs.onToken(e.data)
    })

    es.addEventListener('meta', (e: MessageEvent) => {
      try {
        const meta: ChatMeta = JSON.parse(e.data)
        cbs.onMeta(meta)
      } catch {
        // ignore malformed meta
      }
    })

    es.addEventListener('error', (e: MessageEvent) => {
      cbs.onError?.(e.data ?? '连接异常')
      es.close()
      cbs.onClose?.()
    })

    es.addEventListener('done', () => {
      es.close()
      cbs.onClose?.()
    })

    es.onerror = () => {
      cbs.onError?.('SSE 连接中断，请重试')
      es.close()
      cbs.onClose?.()
    }

    return es
  }
}
