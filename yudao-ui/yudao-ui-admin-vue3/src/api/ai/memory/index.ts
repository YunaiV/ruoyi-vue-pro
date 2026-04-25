import request from '@/config/axios'

// ============================================================
// Types
// ============================================================

export interface MemoryItem {
  id?: number
  tenantId?: number
  customerId: string
  module: string
  memoryType?: string
  memKey: string
  memValue?: string
  confidence?: number
  sourceSessionId?: string
  expiresAt?: string
  createdAt?: string
  updatedAt?: string
}

export interface SaveMemoryReq {
  tenantId?: number
  customerId: string
  module: string
  memoryType?: string
  memKey: string
  memValue: string
  confidence?: number
  sourceSessionId?: string
}

// ============================================================
// API
// ============================================================

export const AiMemoryApi = {

  /** 查询用户记忆列表（按板块，module 为空则查全部）*/
  list(customerId: string, module = '', tenantId = 0): Promise<MemoryItem[]> {
    return request.get({
      url: '/deepay/memory/list',
      params: { customerId, module, tenantId },
    }).then(r => r.data)
  },

  /** 手动写入/更新一条记忆 */
  save(req: SaveMemoryReq): Promise<boolean> {
    return request.post({ url: '/deepay/memory/save', data: req }).then(r => r.data)
  },

  /** 合规删除用户所有记忆（GDPR）*/
  clearAll(customerId: string, tenantId = 0): Promise<boolean> {
    return request.delete({
      url: '/deepay/memory/clear',
      params: { customerId, tenantId },
    }).then(r => r.data)
  },

  /** 查询各板块允许记忆的字段白名单 */
  getAllowedKeys(): Promise<Record<string, string[]>> {
    return request.get({ url: '/deepay/memory/allowed-keys' }).then(r => r.data)
  },

}
