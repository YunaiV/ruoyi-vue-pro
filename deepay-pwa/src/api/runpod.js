/**
 * RunPod API Client — Deepay AI Image Generation
 *
 * 端点注册表（4 模型，三位一体方案）：
 *   flux-dev     black-forest-labs/flux.1 [dev]         — 高质量服装设计
 *   flux-kontext black-forest-labs/flux.1 kontext [dev] — 文字指令局部编辑
 *   flux-schnell black-forest-labs/flux.1 [schnell]     — 极速预览（1-2s）
 *   qwen-edit    qwen/qwen 图片编辑 2511                 — 中文指令换装
 *
 * RunPod 异步流程：
 *   POST /v2/{endpointId}/run  → { id, status:"IN_QUEUE" }
 *   GET  /v2/{endpointId}/status/{jobId} → 轮询直到 COMPLETED / FAILED
 */

import axios from 'axios'

// ── 模型端点注册表 ──────────────────────────────────────────────
export const MODELS = {
  'flux-dev': {
    id:          'flux-dev',
    endpointId:  'black-forest-labs-flux-1-dev',
    label:       'Flux.1 Dev',
    desc:        '高质量服装设计 · 工业标准',
    badge:       '旗舰',
    badgeColor:  '#10a37f',
    supportsImg: false,
    defaults: {
      num_inference_steps: 28,
      guidance:            3.5,
      size:                '1024x1024',
    },
  },
  'flux-kontext': {
    id:          'flux-kontext',
    endpointId:  'black-forest-labs-flux-1-kontext-dev',
    label:       'Flux.1 Kontext',
    desc:        '文字指令局部编辑 · 改领口改颜色',
    badge:       '编辑',
    badgeColor:  '#7c3aed',
    supportsImg: true,
    defaults: {
      num_inference_steps: 28,
      guidance:            2,
      size:                '1024x1024',
    },
  },
  'flux-schnell': {
    id:          'flux-schnell',
    endpointId:  'black-forest-labs-flux-1-schnell',
    label:       'Flux.1 Schnell',
    desc:        '极速预览 1-2s · 调试参数用',
    badge:       '极速',
    badgeColor:  '#f59e0b',
    supportsImg: false,
    defaults: {
      num_inference_steps: 4,
      guidance:            0,
      size:                '1024x1024',
    },
  },
  'qwen-edit': {
    id:          'qwen-edit',
    endpointId:  'qwen-image-edit-2511',
    label:       'Qwen 图片编辑',
    desc:        '中文指令换装 · 多人姿态一致',
    badge:       '中文',
    badgeColor:  '#ef4444',
    supportsImg: true,
    defaults: {
      num_inference_steps: 20,
      guidance:            7.5,
      size:                '1024x1024',
    },
  },
}

// ── RunPod API base ─────────────────────────────────────────────
const BASE = 'https://api.runpod.ai/v2'

// ── 轮询参数 ────────────────────────────────────────────────────
const POLL_INTERVAL_MS = 2000   // 每 2 秒轮询一次
const POLL_TIMEOUT_MS  = 300000 // 最长等 5 分钟

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
 * 提交生成任务
 * @param {string} apiKey      RunPod API Key
 * @param {string} modelId     MODELS 中的 key
 * @param {object} params      生成参数
 * @returns {Promise<string>}  jobId
 */
export async function submitJob(apiKey, modelId, params) {
  const model = MODELS[modelId]
  if (!model) throw new Error(`未知模型: ${modelId}`)

  const client = makeClient(apiKey)
  const input = {
    prompt:                params.prompt,
    negative_prompt:       params.negative_prompt || '',
    seed:                  params.seed ?? -1,
    num_inference_steps:   params.num_inference_steps ?? model.defaults.num_inference_steps,
    guidance:              params.guidance ?? model.defaults.guidance,
    size:                  (params.size || model.defaults.size).replace('x', '*'),
    output_format:         params.output_format || 'png',
    enable_safety_checker: true,
  }

  if (params.image) {
    input.image = params.image
  }

  const resp = await client.post(`/${model.endpointId}/run`, { input })
  const jobId = resp.data?.id
  if (!jobId) throw new Error('RunPod 未返回 job ID，请检查 API Key 和端点配置')
  return jobId
}

/**
 * 轮询任务状态，直到完成或失败
 * @param {string}   apiKey
 * @param {string}   modelId
 * @param {string}   jobId
 * @param {Function} onProgress  (status: string, message: string) => void
 * @returns {Promise<{ images: string[], seed: number }>}
 */
export async function pollJob(apiKey, modelId, jobId, onProgress) {
  const model  = MODELS[modelId]
  const client = makeClient(apiKey)
  const start  = Date.now()

  const statusText = {
    IN_QUEUE:    '排队中...',
    IN_PROGRESS: '生成中...',
    COMPLETED:   '生成完成',
    FAILED:      '生成失败',
    CANCELLED:   '已取消',
    TIMED_OUT:   '任务超时',
  }

  while (true) {
    if (Date.now() - start > POLL_TIMEOUT_MS) {
      throw new Error('等待超时（5 分钟），请检查 RunPod 账户余额')
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
      return parseOutput(output)
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
  const client = makeClient(apiKey)
  await client.post(`/${model.endpointId}/cancel/${jobId}`)
}

// ── 内部工具 ────────────────────────────────────────────────────

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * 解析不同模型的 output 格式，统一返回 { images: string[], seed }
 * RunPod 上各模型输出结构略有差异：
 *   Flux:  output = [{ url, seed }] 或 output = { images: [base64] }
 *   Qwen:  output = { image: "base64" } 或 output = [{ image: "..." }]
 */
function parseOutput(output) {
  if (!output) throw new Error('生成结果为空')

  if (Array.isArray(output)) {
    const images = output.map(item =>
      typeof item === 'string' ? item : (item.url || item.image || '')
    ).filter(Boolean)
    const seed = output[0]?.seed ?? -1
    if (images.length > 0) return { images, seed }
  }

  if (output.images && Array.isArray(output.images)) {
    return { images: output.images, seed: output.seed ?? -1 }
  }

  if (output.image) {
    return { images: [output.image], seed: output.seed ?? -1 }
  }

  if (typeof output === 'string') {
    return { images: [output], seed: -1 }
  }

  throw new Error(`无法解析生成结果: ${JSON.stringify(output).slice(0, 200)}`)
}

/**
 * 将 base64 字符串或 URL 转为可显示的 img src
 */
export function toImageSrc(raw) {
  if (!raw) return ''
  if (raw.startsWith('http')) return raw
  if (!raw.startsWith('data:')) return `data:image/png;base64,${raw}`
  return raw
}
