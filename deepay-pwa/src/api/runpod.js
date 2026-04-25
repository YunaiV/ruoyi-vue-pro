/**
 * RunPod API Client — Deepay AI 多模型生成引擎
 *
 * 三大类别、16 个端点：
 *   【图像生成 txt2img】  flux-dev · flux-schnell · seedream-t2i · qwen-img · qwen-img-lora
 *   【图像编辑 img2img】  flux-kontext · qwen-edit · qwen-edit-2511 · nano-banana · seedream-edit · qwen-img-lora
 *   【图生视频 img2vid】  wan-2.5-i2v · wan-2.2-i2v · wan-2.2-i2v-lora · wan-2.1-i2v · kling-v2.1-i2v
 *
 * API Key 优先级：
 *   1. localStorage（用户在 Settings 页输入）
 *   2. import.meta.env.VITE_RUNPOD_API_KEY（本地 .env.local 文件，仅开发用，不提交 git）
 *
 * RunPod 异步流程：
 *   POST /v2/{endpointId}/run  → { id, status:"IN_QUEUE" }
 *   GET  /v2/{endpointId}/status/{jobId} → 轮询直到 COMPLETED / FAILED
 */

import axios from 'axios'

// ── 模型类别 ────────────────────────────────────────────────────
export const MODEL_TYPES = {
  TXT2IMG: 'txt2img',  // 纯文字 → 图像
  IMG2IMG: 'img2img',  // 图像 + 文字 → 图像（编辑/换装）
  IMG2VID: 'img2vid',  // 图像 + 文字 → 视频（模特走秀）
}

// ── 模型端点注册表 ──────────────────────────────────────────────
export const MODELS = {

  /* ═══ 图像生成 (txt2img) ═══════════════════════════════════ */

  'flux-dev': {
    id:          'flux-dev',
    endpointId:  'black-forest-labs-flux-1-dev',
    type:        MODEL_TYPES.TXT2IMG,
    label:       'Flux.1 Dev',
    maker:       'Black Forest Labs',
    desc:        '最高质量服装设计 · 面料质感无敌',
    badge:       '旗舰',
    badgeColor:  '#10a37f',
    defaults: { num_inference_steps: 28, guidance: 3.5, size: '1024x1024' },
  },

  'flux-schnell': {
    id:          'flux-schnell',
    endpointId:  'black-forest-labs-flux-1-schnell',
    type:        MODEL_TYPES.TXT2IMG,
    label:       'Flux.1 Schnell',
    maker:       'Black Forest Labs',
    desc:        '极速预览 1-2s · 调试参数首选',
    badge:       '极速',
    badgeColor:  '#f59e0b',
    defaults: { num_inference_steps: 4, guidance: 0, size: '1024x1024' },
  },

  'seedream-t2i': {
    id:          'seedream-t2i',
    endpointId:  'bytedance-seedream-4-0-t2i',
    type:        MODEL_TYPES.TXT2IMG,
    label:       'Seedream 4.0',
    maker:       'ByteDance',
    desc:        '新一代统一架构 · 生成编辑二合一',
    badge:       '字节',
    badgeColor:  '#1d4ed8',
    defaults: { num_inference_steps: 30, guidance: 7.5, size: '1024x1024' },
  },

  'qwen-img': {
    id:          'qwen-img',
    endpointId:  'qwen-qwen-image',
    type:        MODEL_TYPES.TXT2IMG,
    label:       'Qwen Image',
    maker:       'Alibaba / Qwen',
    desc:        '复杂文字渲染 · 精确图像生成',
    badge:       '阿里',
    badgeColor:  '#ff6600',
    defaults: { num_inference_steps: 25, guidance: 7.5, size: '1024x1024' },
  },

  'qwen-img-lora': {
    id:          'qwen-img-lora',
    endpointId:  'qwen-qwen-image-lora',
    type:        MODEL_TYPES.TXT2IMG,
    label:       'Qwen Image LoRA',
    maker:       'Alibaba / Qwen',
    desc:        '支持 LoRA · 自定义风格微调',
    badge:       'LoRA',
    badgeColor:  '#7c3aed',
    defaults: { num_inference_steps: 25, guidance: 7.5, size: '1024x1024' },
  },

  /* ═══ 图像编辑 (img2img) ═══════════════════════════════════ */

  'flux-kontext': {
    id:          'flux-kontext',
    endpointId:  'black-forest-labs-flux-1-kontext-dev',
    type:        MODEL_TYPES.IMG2IMG,
    label:       'Flux.1 Kontext',
    maker:       'Black Forest Labs',
    desc:        '文字指令精准编辑 · 改领口换颜色',
    badge:       '编辑',
    badgeColor:  '#7c3aed',
    defaults: { num_inference_steps: 28, guidance: 2, size: '1024x1024' },
  },

  'qwen-edit-2511': {
    id:          'qwen-edit-2511',
    endpointId:  'qwen-image-edit-2511',
    type:        MODEL_TYPES.IMG2IMG,
    label:       'Qwen 编辑 2511',
    maker:       'Alibaba / Qwen',
    desc:        '中文指令换装 · 多人姿态一致',
    badge:       '中文',
    badgeColor:  '#ef4444',
    defaults: { num_inference_steps: 20, guidance: 7.5, size: '1024x1024' },
  },

  'qwen-edit': {
    id:          'qwen-edit',
    endpointId:  'qwen-qwen-image-edit',
    type:        MODEL_TYPES.IMG2IMG,
    label:       'Qwen Image Edit',
    maker:       'Alibaba / Qwen',
    desc:        '精确文字编辑 · 扩展至图像编辑',
    badge:       '文字',
    badgeColor:  '#ff6600',
    defaults: { num_inference_steps: 20, guidance: 7.5, size: '1024x1024' },
  },

  'nano-banana': {
    id:          'nano-banana',
    endpointId:  'google-nano-banana-edit',
    type:        MODEL_TYPES.IMG2IMG,
    label:       'Nano Banana Edit',
    maker:       'Google',
    desc:        '谷歌最先进图像编辑 · 精细局部修改',
    badge:       'Google',
    badgeColor:  '#4285f4',
    defaults: { num_inference_steps: 25, guidance: 7.5, size: '1024x1024' },
  },

  'seedream-edit': {
    id:          'seedream-edit',
    endpointId:  'bytedance-seedream-4-0-edit',
    type:        MODEL_TYPES.IMG2IMG,
    label:       'Seedream 4.0 Edit',
    maker:       'ByteDance',
    desc:        '统一架构 · 生成与编辑无缝切换',
    badge:       '编辑',
    badgeColor:  '#1d4ed8',
    defaults: { num_inference_steps: 30, guidance: 7.5, size: '1024x1024' },
  },

  /* ═══ 图生视频 (img2vid) ═══════════════════════════════════ */

  'wan-2.5-i2v': {
    id:          'wan-2.5-i2v',
    endpointId:  'alibaba-wan-2-5-i2v',
    type:        MODEL_TYPES.IMG2VID,
    label:       'WAN 2.5 i2v',
    maker:       'Alibaba',
    desc:        '最先进图生视频 · 模特走秀首选',
    badge:       '最新',
    badgeColor:  '#10a37f',
    defaults: { num_inference_steps: 25, guidance: 7.5, num_frames: 81, fps: 16 },
  },

  'wan-2.2-i2v': {
    id:          'wan-2.2-i2v',
    endpointId:  'alibaba-wan-2-2-i2v-720p',
    type:        MODEL_TYPES.IMG2VID,
    label:       'WAN 2.2 i2v 720p',
    maker:       'Alibaba',
    desc:        '720p 视频 · 扩散 Transformer 架构',
    badge:       '720p',
    badgeColor:  '#7c3aed',
    defaults: { num_inference_steps: 25, guidance: 7.5, num_frames: 81, fps: 16 },
  },

  'wan-2.2-i2v-lora': {
    id:          'wan-2.2-i2v-lora',
    endpointId:  'alibaba-wan-2-2-i2v-720p-lora',
    type:        MODEL_TYPES.IMG2VID,
    label:       'WAN 2.2 i2v LoRA',
    maker:       'Alibaba',
    desc:        '720p + LoRA · 自定义风格视频',
    badge:       'LoRA',
    badgeColor:  '#f59e0b',
    defaults: { num_inference_steps: 25, guidance: 7.5, num_frames: 81, fps: 16 },
  },

  'wan-2.1-i2v': {
    id:          'wan-2.1-i2v',
    endpointId:  'alibaba-wan-2-1-i2v-720p',
    type:        MODEL_TYPES.IMG2VID,
    label:       'WAN 2.1 i2v 720p',
    maker:       'Alibaba',
    desc:        '稳定版 720p · 性价比最优',
    badge:       '稳定',
    badgeColor:  '#64748b',
    defaults: { num_inference_steps: 25, guidance: 7.5, num_frames: 81, fps: 16 },
  },

  'kling-v2.1-i2v': {
    id:          'kling-v2.1-i2v',
    endpointId:  'kwaivgi-kling-v2-1-i2v-pro',
    type:        MODEL_TYPES.IMG2VID,
    label:       'Kling v2.1 Pro',
    maker:       'Kwai',
    desc:        '专业级视频 · 精确相机运动控制',
    badge:       'Pro',
    badgeColor:  '#ef4444',
    defaults: { num_inference_steps: 30, guidance: 7.5, num_frames: 97, fps: 24 },
  },

}

// ── RunPod API base ─────────────────────────────────────────────
const BASE = 'https://api.runpod.ai/v2'

// ── 轮询参数 ────────────────────────────────────────────────────
const POLL_INTERVAL_MS = 2000    // 每 2 秒轮询
const POLL_TIMEOUT_MS  = 600000  // 最长等 10 分钟（视频生成更慢）

// ── 创建 axios 实例 ─────────────────────────────────────────────
function makeClient(apiKey) {
  return axios.create({
    baseURL: BASE,
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${apiKey}`,
    },
    timeout: 30000,
  })
}

/**
 * 获取有效的 API Key
 * 优先级：传入参数 > localStorage > VITE_RUNPOD_API_KEY（.env.local，不提交 git）
 */
export function resolveApiKey(override) {
  if (override) return override
  const fromStorage = localStorage.getItem('deepay_runpod_key')
  if (fromStorage) return fromStorage
  return import.meta.env.VITE_RUNPOD_API_KEY || ''
}

/**
 * 构建各模型的 input 参数
 * 不同类型的模型 input 结构不同
 */
function buildInput(model, params) {
  const base = {
    prompt:                params.prompt,
    negative_prompt:       params.negative_prompt || '',
    seed:                  params.seed ?? -1,
    output_format:         params.output_format || 'png',
    enable_safety_checker: true,
  }

  if (model.type === MODEL_TYPES.IMG2VID) {
    // 视频模型参数
    return {
      ...base,
      image:             params.image || '',
      num_inference_steps: params.num_inference_steps ?? model.defaults.num_inference_steps,
      guidance_scale:    params.guidance ?? model.defaults.guidance,
      num_frames:        params.num_frames ?? model.defaults.num_frames,
      fps:               params.fps ?? model.defaults.fps,
    }
  }

  // 图像模型（txt2img / img2img）
  const input = {
    ...base,
    num_inference_steps: params.num_inference_steps ?? model.defaults.num_inference_steps,
    guidance:            params.guidance ?? model.defaults.guidance,
    size:                (params.size || model.defaults.size).replace('x', '*'),
  }
  if (params.image) input.image = params.image
  return input
}

/**
 * 提交生成任务 → 返回 jobId
 */
export async function submitJob(apiKey, modelId, params) {
  const model = MODELS[modelId]
  if (!model) throw new Error(`未知模型: ${modelId}`)

  const key = resolveApiKey(apiKey)
  if (!key) throw new Error('未设置 RunPod API Key，请前往设置页面填写')

  const client = makeClient(key)
  const input  = buildInput(model, params)
  const resp   = await client.post(`/${model.endpointId}/run`, { input })
  const jobId  = resp.data?.id
  if (!jobId) throw new Error('RunPod 未返回 job ID，请检查 API Key 和端点 ID 是否正确')
  return jobId
}

/**
 * 轮询任务状态，直到完成或失败
 * @returns {Promise<{ images?: string[], video?: string, seed: number, type: string }>}
 */
export async function pollJob(apiKey, modelId, jobId, onProgress) {
  const model  = MODELS[modelId]
  const key    = resolveApiKey(apiKey)
  const client = makeClient(key)
  const start  = Date.now()

  const statusText = {
    IN_QUEUE:    '排队中...',
    IN_PROGRESS: '生成中...',
    COMPLETED:   '生成完成 ✓',
    FAILED:      '生成失败',
    CANCELLED:   '已取消',
    TIMED_OUT:   '任务超时',
  }

  while (true) {
    if (Date.now() - start > POLL_TIMEOUT_MS) {
      throw new Error('等待超时（10 分钟），请检查 RunPod 账户余额与端点状态')
    }

    await sleep(POLL_INTERVAL_MS)

    let resp
    try {
      resp = await client.get(`/${model.endpointId}/status/${jobId}`)
    } catch {
      onProgress?.('IN_PROGRESS', '网络波动，重试中...')
      continue
    }

    const { status, output, error } = resp.data
    const elapsed = Math.round((Date.now() - start) / 1000)
    onProgress?.(status, `${statusText[status] || status} (${elapsed}s)`)

    if (status === 'COMPLETED') {
      return parseOutput(output, model.type)
    }
    if (['FAILED', 'CANCELLED', 'TIMED_OUT'].includes(status)) {
      throw new Error(error || `任务${statusText[status] || status}`)
    }
  }
}

/**
 * 一站式：提交 + 轮询 + 返回结果
 */
export async function generate(apiKey, modelId, params, onProgress) {
  onProgress?.('IN_QUEUE', '正在提交任务...')
  const jobId = await submitJob(apiKey, modelId, params)
  onProgress?.('IN_QUEUE', `任务已提交 (${jobId.slice(0, 8)}...)`)
  return pollJob(apiKey, modelId, jobId, onProgress)
}

/**
 * 取消任务
 */
export async function cancelJob(apiKey, modelId, jobId) {
  const model  = MODELS[modelId]
  const key    = resolveApiKey(apiKey)
  const client = makeClient(key)
  await client.post(`/${model.endpointId}/cancel/${jobId}`)
}

// ── 内部工具 ────────────────────────────────────────────────────

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * 解析各模型输出，统一返回 { images?, video?, seed, type }
 *
 * 图像模型 output 格式（RunPod 上不同模型略有差异）：
 *   Flux:  [ { url, seed } ] 或 { images: [base64] }
 *   Qwen:  { image: "base64" } 或 [ { image: "..." } ]
 *
 * 视频模型 output 格式：
 *   WAN / Kling: { video: "https://..." } 或 { output: { video_url: "..." } }
 */
function parseOutput(output, modelType) {
  if (!output) throw new Error('生成结果为空，请检查端点是否正常运行')

  // ── 视频输出 ──────────────────────────────────────────
  if (modelType === MODEL_TYPES.IMG2VID) {
    // 形式1: { video: url }
    if (output.video)               return { video: output.video,      seed: output.seed ?? -1, type: 'video' }
    // 形式2: { video_url: url }
    if (output.video_url)           return { video: output.video_url,  seed: output.seed ?? -1, type: 'video' }
    // 形式3: array with url
    if (Array.isArray(output)) {
      const item = output[0]
      const url  = item?.url || item?.video || (typeof item === 'string' ? item : '')
      if (url) return { video: url, seed: item?.seed ?? -1, type: 'video' }
    }
    // 形式4: output.output.video_url (nested)
    if (output.output?.video_url)   return { video: output.output.video_url, seed: -1, type: 'video' }
  }

  // ── 图像输出 ──────────────────────────────────────────
  if (Array.isArray(output)) {
    const images = output.map(item =>
      typeof item === 'string' ? item : (item.url || item.image || '')
    ).filter(Boolean)
    if (images.length > 0) return { images, seed: output[0]?.seed ?? -1, type: 'image' }
  }
  if (output.images && Array.isArray(output.images)) {
    return { images: output.images, seed: output.seed ?? -1, type: 'image' }
  }
  if (output.image)  return { images: [output.image],  seed: output.seed ?? -1, type: 'image' }
  if (typeof output === 'string') return { images: [output], seed: -1, type: 'image' }

  throw new Error(`无法解析生成结果，请联系支持:\n${JSON.stringify(output).slice(0, 300)}`)
}

/**
 * 将 base64 字符串或 URL 转为可显示的 <img> src
 */
export function toImageSrc(raw) {
  if (!raw) return ''
  if (raw.startsWith('http') || raw.startsWith('blob:')) return raw
  if (!raw.startsWith('data:')) return `data:image/png;base64,${raw}`
  return raw
}

/**
 * 按类型获取模型列表
 */
export function getModelsByType(type) {
  return Object.values(MODELS).filter(m => m.type === type)
}

/**
 * 检查当前环境是否有可用的 API Key（用于 UI 提示）
 */
export function hasApiKey() {
  return !!resolveApiKey()
}

