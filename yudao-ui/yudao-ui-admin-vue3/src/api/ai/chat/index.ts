import request from '@/config/axios'

// ---- 类型定义 ----

export interface ChatReply {
  aiMessage: string
  pendingField?: string
  quickReplies?: string[]
  images?: string[]
  done: boolean
  sessionId: string
}

export interface ChatMessageReq {
  module: string        // selection / design / product / inventory / finance / trend / order
  sessionId?: string    // 首次可不传，后续必传
  customerId?: number
  userMessage: string
}

// ---- API ----

export const AiChatApi = {
  /**
   * 发送对话消息，获取 AI 回复
   */
  sendMessage: async (data: ChatMessageReq): Promise<ChatReply> => {
    return await request.post({ url: '/deepay/chat/message', data })
  },

  /**
   * 输入框自动补全
   * @param prefix 已输入的前缀
   */
  autocomplete: async (prefix: string): Promise<string[]> => {
    return await request.get({ url: '/deepay/chat/autocomplete', params: { prefix } })
  },

  /**
   * 结束对话会话（清除 Redis 中的 Context）
   */
  endSession: async (sessionId: string): Promise<void> => {
    await request.delete({ url: `/deepay/chat/session/${sessionId}` })
  }
}
