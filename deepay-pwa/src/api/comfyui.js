/**
 * ComfyUI Direct API Client — Deepay
 *
 * 适用于运行在 RunPod Pod 上的 ComfyUI 实例（持续运行，8188 端口）。
 * 与 RunPod Serverless API（runpod.js）是两套完全不同的调用方式。
 *
 * Pod 地址格式示例：
 *   https://x1ik3wb0zvch3v-64410f01-8188.proxy.runpod.net
 *   http://localhost:8188  （本地开发）
 *
 * ComfyUI API 流程：
 *   1. POST  {base}/prompt          → { prompt_id }       提交工作流
 *   2. GET   {base}/history/{id}    → { [id]: {...} }     轮询直到有结果
 *   3. GET   {base}/view?filename=  → image blob/url      取图
 *
 * 内置 Flux.1 Dev / Flux.1 Schnell / SDXL 三套默认工作流模板。
 * 用户也可以从 ComfyUI 界面导出 workflow_api.json 直接使用。
 *
 * 安全说明：
 *   ComfyUI 内置 Auth 时，需要在 Header 带 Authorization: Bearer {key}
 *   如未启用 Auth，留 apiKey 为空即可
 */

import axios from 'axios'

// ── 轮询参数 ────────────────────────────────────────────────────
const POLL_INTERVAL_MS = 2000
const POLL_TIMEOUT_MS  = 600000  // 10 分钟

// ── 内置工作流模板 ──────────────────────────────────────────────

/**
 * Flux.1 Dev / Schnell 通用文生图模板
 * node "6" = 正向提示词
 * node "7" = 负向提示词
 * node "3" = KSampler（steps / cfg / seed）
 * node "5" = Empty Latent（width / height）
 * node "4" = checkpoint loader
 */
export const WORKFLOW_FLUX_TEXT2IMG = {
  "1": {
    "class_type": "CheckpointLoaderSimple",
    "inputs": { "ckpt_name": "flux1-dev.safetensors" }
  },
  "2": {
    "class_type": "CLIPTextEncode",
    "inputs": { "clip": ["1", 1], "text": "beautiful fashion model, high quality" }
  },
  "3": {
    "class_type": "CLIPTextEncode",
    "inputs": { "clip": ["1", 1], "text": "" }
  },
  "4": {
    "class_type": "EmptyLatentImage",
    "inputs": { "batch_size": 1, "height": 1024, "width": 1024 }
  },
  "5": {
    "class_type": "KSampler",
    "inputs": {
      "cfg": 1,
      "denoise": 1,
      "latent_image": ["4", 0],
      "model": ["1", 0],
      "negative": ["3", 0],
      "positive": ["2", 0],
      "sampler_name": "euler",
      "scheduler": "simple",
      "seed": -1,
      "steps": 20
    }
  },
  "6": {
    "class_type": "VAEDecode",
    "inputs": { "samples": ["5", 0], "vae": ["1", 2] }
  },
  "7": {
    "class_type": "SaveImage",
    "inputs": { "filename_prefix": "deepay_flux", "images": ["6", 0] }
  }
}

/**
 * Flux.1 Kontext 图像编辑模板（需要参考图）
 * node "1"  = checkpoint loader（flux1-kontext-dev.safetensors）
 * node "10" = LoadImage
 * node "11" = FluxKontextImageToImage（核心编辑节点）
 */
export const WORKFLOW_FLUX_KONTEXT = {
  "1": {
    "class_type": "CheckpointLoaderSimple",
    "inputs": { "ckpt_name": "flux1-kontext-dev.safetensors" }
  },
  "10": {
    "class_type": "LoadImageFromURL",
    "inputs": { "url": "" }
  },
  "2": {
    "class_type": "CLIPTextEncode",
    "inputs": { "clip": ["1", 1], "text": "edit this image" }
  },
  "3": {
    "class_type": "CLIPTextEncode",
    "inputs": { "clip": ["1", 1], "text": "" }
  },
  "4": {
    "class_type": "KSamplerAdvanced",
    "inputs": {
      "add_noise": "enable",
      "cfg": 1,
      "denoise": 0.75,
      "end_at_step": 28,
      "latent_image": ["11", 0],
      "model": ["1", 0],
      "negative": ["3", 0],
      "positive": ["2", 0],
      "return_with_leftover_noise": "disable",
      "sampler_name": "euler",
      "scheduler": "simple",
      "start_at_step": 0,
      "steps": 28,
      "noise_seed": -1
    }
  },
  "11": {
    "class_type": "VAEEncodeForInpaint",
    "inputs": { "grow_mask_by": 6, "pixels": ["10", 0], "vae": ["1", 2] }
  },
  "6": {
    "class_type": "VAEDecode",
    "inputs": { "samples": ["4", 0], "vae": ["1", 2] }
  },
  "7": {
    "class_type": "SaveImage",
    "inputs": { "filename_prefix": "deepay_kontext", "images": ["6", 0] }
  }
}

// ── 工作流注册表 ────────────────────────────────────────────────
export const WORKFLOWS = {
  'flux-t2i': {
    id:          'flux-t2i',
    label:       'Flux.1 文生图',
    desc:        '标准 Flux.1 Dev/Schnell 文生图',
    template:    WORKFLOW_FLUX_TEXT2IMG,
    promptNode:  '2',   // 正向提示词节点 ID
    negNode:     '3',
    samplerNode: '5',
    latentNode:  '4',
    outputNode:  '7',
  },
  'flux-kontext': {
    id:          'flux-kontext',
    label:       'Flux.1 Kontext 编辑',
    desc:        '文字指令局部编辑，需提供参考图',
    template:    WORKFLOW_FLUX_KONTEXT,
    promptNode:  '2',
    negNode:     '3',
    samplerNode: '4',
    imageNode:   '10',
    outputNode:  '7',
  },
}

// ── HTTP 客户端 ─────────────────────────────────────────────────
function makeClient(baseUrl, apiKey) {
  const headers = { 'Content-Type': 'application/json' }
  if (apiKey && apiKey.trim()) {
    headers['Authorization'] = `Bearer ${apiKey.trim()}`
  }
  return axios.create({
    baseURL: baseUrl.replace(/\/+$/, ''),
    headers,
    timeout: 30000,
  })
}

// ── resolveUrl ──────────────────────────────────────────────────
export function resolveComfyUrl() {
  const fromStorage = localStorage.getItem('deepay_comfy_url')
  if (fromStorage) return fromStorage
  return import.meta.env.VITE_COMFY_URL || ''
}

export function resolveComfyKey() {
  const fromStorage = localStorage.getItem('deepay_comfy_key')
  if (fromStorage) return fromStorage
  return import.meta.env.VITE_COMFY_KEY || ''
}

/**
 * 测试 ComfyUI 服务是否在线
 * @returns {Promise<{ ok: boolean, version: string }>}
 */
export async function pingComfy(baseUrl, apiKey) {
  try {
    const client = makeClient(baseUrl, apiKey)
    const resp = await client.get('/system_stats', { timeout: 8000 })
    const version = resp.data?.system?.comfyui_version || resp.data?.version || '在线'
    return { ok: true, version }
  } catch (err) {
    const msg = err.response?.status === 401
      ? '认证失败，请检查 API Key'
      : err.response?.status === 403
        ? '访问被拒绝（403）'
        : `无法连接：${err.message}`
    return { ok: false, version: '', error: msg }
  }
}

/**
 * 深克隆并将用户参数注入工作流 JSON
 * @param {object} workflow  - 从 WORKFLOWS 注册表拿到的 template 或用户自定义 JSON
 * @param {object} params    - 注入参数
 *   params.prompt       - 正向提示词
 *   params.negative     - 负向提示词
 *   params.seed         - 随机种子（-1 = 随机）
 *   params.steps        - 推理步数
 *   params.cfg          - CFG scale
 *   params.width        - 图像宽度
 *   params.height       - 图像高度
 *   params.imageUrl     - 参考图 URL（编辑模型用）
 *   params.promptNode   - 提示词所在的节点 ID（默认 '6'）
 *   params.negNode      - 负向提示词节点 ID（默认 '7'）
 *   params.samplerNode  - KSampler 节点 ID（默认 '3'）
 *   params.latentNode   - EmptyLatent 节点 ID（默认 '5'）
 *   params.imageNode    - LoadImage 节点 ID（编辑用，默认 '10'）
 * @returns {object} 注入后的工作流
 */
export function injectParams(workflow, params) {
  const wf = JSON.parse(JSON.stringify(workflow))  // deep clone

  const {
    prompt,
    negative     = '',
    seed         = -1,
    steps,
    cfg,
    width,
    height,
    imageUrl,
    promptNode   = '6',
    negNode      = '7',
    samplerNode  = '3',
    latentNode   = '5',
    imageNode    = '10',
  } = params

  // 正向提示词
  if (wf[promptNode]?.inputs) wf[promptNode].inputs.text = prompt

  // 负向提示词
  if (negNode && wf[negNode]?.inputs) wf[negNode].inputs.text = negative

  // KSampler / KSamplerAdvanced
  if (wf[samplerNode]?.inputs) {
    if (seed !== -1) {
      const seedKey = 'seed' in wf[samplerNode].inputs ? 'seed' : 'noise_seed'
      wf[samplerNode].inputs[seedKey] = seed
    }
    if (steps !== undefined)  wf[samplerNode].inputs.steps = steps
    if (cfg   !== undefined)  wf[samplerNode].inputs.cfg   = cfg
  }

  // 图像尺寸
  if (latentNode && wf[latentNode]?.inputs) {
    if (width  !== undefined) wf[latentNode].inputs.width  = width
    if (height !== undefined) wf[latentNode].inputs.height = height
  }

  // 参考图（编辑模型）
  if (imageUrl && imageNode && wf[imageNode]?.inputs) {
    // 支持 LoadImageFromURL 节点（url 字段）
    if ('url' in wf[imageNode].inputs) {
      wf[imageNode].inputs.url = imageUrl
    } else if ('image' in wf[imageNode].inputs) {
      wf[imageNode].inputs.image = imageUrl
    }
  }

  return wf
}

/**
 * 提交工作流到 ComfyUI
 * @returns {Promise<string>} prompt_id
 */
export async function submitWorkflow(baseUrl, apiKey, workflowJson) {
  if (!baseUrl) throw new Error('未设置 ComfyUI Pod 地址，请前往设置页面填写')
  const client = makeClient(baseUrl, apiKey)
  const resp = await client.post('/prompt', { prompt: workflowJson })
  const promptId = resp.data?.prompt_id
  if (!promptId) throw new Error(`提交失败，未返回 prompt_id。Response: ${JSON.stringify(resp.data).slice(0, 300)}`)
  return promptId
}

/**
 * 轮询 /history/{promptId} 直到任务完成
 * @param {Function} onProgress  (msg: string, percent: number) => void
 * @returns {Promise<Array<{ filename, subfolder, type }>>}  输出图像列表
 */
export async function pollUntilDone(baseUrl, apiKey, promptId, onProgress) {
  const client = makeClient(baseUrl, apiKey)
  const start  = Date.now()
  let pollCount = 0

  while (true) {
    if (Date.now() - start > POLL_TIMEOUT_MS) throw new Error('轮询超时（10分钟），请检查 ComfyUI 日志')

    await sleep(POLL_INTERVAL_MS)
    pollCount++

    const elapsed = Math.round((Date.now() - start) / 1000)
    onProgress?.(`生成中... (${elapsed}s · ${pollCount}次)`, Math.min(90, elapsed))

    let resp
    try {
      resp = await client.get(`/history/${promptId}`)
    } catch {
      onProgress?.('网络波动，重试...', 0)
      continue
    }

    const entry = resp.data?.[promptId]
    if (!entry) continue  // 还没开始

    // 检查是否有错误
    if (entry.status?.messages) {
      const errors = entry.status.messages.filter(m => m[0] === 'execution_error')
      if (errors.length) throw new Error(`ComfyUI 执行错误: ${JSON.stringify(errors[0][1]).slice(0, 300)}`)
    }

    // 检查是否完成（outputs 里有 images）
    const outputs = entry.outputs || {}
    const allImages = []
    for (const nodeId of Object.keys(outputs)) {
      const nodeOut = outputs[nodeId]
      if (nodeOut.images && Array.isArray(nodeOut.images)) {
        allImages.push(...nodeOut.images)
      }
    }

    if (allImages.length > 0) {
      onProgress?.(`✅ 完成，共 ${allImages.length} 张图 (${elapsed}s)`, 100)
      return allImages
    }
  }
}

/**
 * 将 ComfyUI 输出的图像信息转换为可直接展示的 URL
 * ComfyUI /view 接口：GET /view?filename=xxx&subfolder=xxx&type=output
 */
export function buildImageUrl(baseUrl, { filename, subfolder = '', type = 'output' }) {
  const base = baseUrl.replace(/\/+$/, '')
  const params = new URLSearchParams({ filename, type })
  if (subfolder) params.set('subfolder', subfolder)
  return `${base}/view?${params.toString()}`
}

/**
 * 一站式：提交工作流 → 轮询 → 返回可显示的图片 URL 列表
 * @returns {Promise<{ images: string[], promptId: string }>}
 */
export async function generate(baseUrl, apiKey, workflowJson, onProgress) {
  onProgress?.('正在提交到 ComfyUI...', 0)
  const promptId = await submitWorkflow(baseUrl, apiKey, workflowJson)
  onProgress?.(`已提交 (${promptId.slice(0, 8)}...)`, 5)

  const imageInfos = await pollUntilDone(baseUrl, apiKey, promptId, onProgress)
  const images     = imageInfos.map(info => buildImageUrl(baseUrl, info))

  return { images, promptId }
}

// ── 工具 ────────────────────────────────────────────────────────
function sleep(ms) { return new Promise(r => setTimeout(r, ms)) }

/**
 * 从 ComfyUI 工作流 JSON 中自动检测关键节点 ID
 * 用于帮助用户调试自定义工作流
 */
export function detectNodes(workflow) {
  const result = {
    promptNodes:   [],
    samplerNodes:  [],
    latentNodes:   [],
    saveNodes:     [],
    loadImgNodes:  [],
    checkpointNodes: [],
  }

  for (const [id, node] of Object.entries(workflow)) {
    const ct = node.class_type || ''
    if (ct.includes('CLIPTextEncode'))         result.promptNodes.push({ id, ct })
    if (ct.includes('KSampler'))               result.samplerNodes.push({ id, ct })
    if (ct.includes('EmptyLatent'))            result.latentNodes.push({ id, ct })
    if (ct.includes('SaveImage') || ct.includes('PreviewImage')) result.saveNodes.push({ id, ct })
    if (ct.includes('LoadImage'))              result.loadImgNodes.push({ id, ct })
    if (ct.includes('CheckpointLoader'))       result.checkpointNodes.push({ id, ct })
  }

  return result
}
