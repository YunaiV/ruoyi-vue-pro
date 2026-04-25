import request from '@/config/axios'

// ============================================================
// Types
// ============================================================

export interface AiPersona {
  id?: number
  tenantId?: number
  module: string
  roleName?: string
  systemPrompt?: string
  examples?: string
  toolWhitelist?: string
  enabled?: number
  createTime?: string
  updateTime?: string
}

export interface QuotaInfo {
  dailyLimit:   number
  dailyUsed:    number
  dailyRemain:  number
  minuteLimit:  number
}

// ============================================================
// API
// ============================================================

export const AiPersonaApi = {

  /** 查询所有启用的 persona */
  list(): Promise<AiPersona[]> {
    return request.get({ url: '/deepay/persona/list' }).then(r => r.data)
  },

  /** 预览指定模块 system prompt */
  preview(module: string, tenantId = 0): Promise<{ module: string; tenantId: string; systemPrompt: string }> {
    return request.get({ url: '/deepay/persona/preview', params: { module, tenantId } }).then(r => r.data)
  },

  /** 新增 persona */
  create(persona: AiPersona): Promise<AiPersona> {
    return request.post({ url: '/deepay/persona/create', data: persona }).then(r => r.data)
  },

  /** 更新 persona */
  update(persona: AiPersona): Promise<boolean> {
    return request.put({ url: '/deepay/persona/update', data: persona }).then(r => r.data)
  },

  /** 删除 persona（软删除）*/
  delete(id: number): Promise<boolean> {
    return request.delete({ url: `/deepay/persona/${id}` }).then(r => r.data)
  },

  /** 查询当前用户剩余配额 */
  getQuota(userId?: string, tenantId = 0): Promise<QuotaInfo> {
    return request.get({ url: '/deepay/persona/quota', params: { userId, tenantId } }).then(r => r.data)
  },

}
