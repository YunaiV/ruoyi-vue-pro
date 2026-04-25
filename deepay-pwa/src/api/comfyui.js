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
 * WebSocket 实时进度监听 — 替代 HTTP 轮询，体验更流畅
 *
 * ComfyUI WS 端点: ws(s)://{host}/ws?clientId={uuid}
 * 消息类型:
 *   status      → 队列状态
 *   progress    → { value, max }          当前节点步数进度
 *   executing   → { node, prompt_id }     正在执行哪个节点
 *   executed    → { node, output, prompt_id }  某节点执行完毕（含图像）
 *   execution_success / execution_error
 *
 * @param {string}   baseUrl    ComfyUI Pod 地址（http/https，自动转 ws/wss）
 * @param {string}   apiKey     Auth Key（RunPod built-in auth）
 * @param {string}   promptId   submitWorkflow 返回的 prompt_id
 * @param {object}   callbacks
 *   onProgress(step, maxStep, nodeId, msg)  — 步进回调
 *   onNodeStart(nodeId, nodeType)            — 节点开始
 *   onNodeDone(nodeId, images)              — 节点完成（如有图）
 *   onDone(imageInfos)                      — 全部完成
 *   onError(msg)                            — 失败
 * @returns {{ close: () => void }}  调用 close() 可手动断开
 */
export function connectWs(baseUrl, apiKey, promptId, callbacks = {}) {
  const { onProgress, onNodeStart, onNodeDone, onDone, onError } = callbacks

  // http(s) → ws(s)
  const wsBase = baseUrl.replace(/\/+$/, '').replace(/^http/, 'ws')
  const clientId = crypto.randomUUID?.() || Math.random().toString(36).slice(2)

  // Build WebSocket URL; attach auth token as query param (RunPod built-in auth method)
  const url = `${wsBase}/ws?clientId=${clientId}${apiKey ? `&token=${encodeURIComponent(apiKey)}` : ''}`

  let ws
  let timeoutId
  const TIMEOUT_MS = 600000  // 10 min

  function cleanup() {
    clearTimeout(timeoutId)
    try { ws?.close() } catch {}
  }

  timeoutId = setTimeout(() => {
    onError?.('WebSocket 超时（10分钟）')
    cleanup()
  }, TIMEOUT_MS)

  try {
    ws = new WebSocket(url)
  } catch (e) {
    onError?.(`WebSocket 创建失败: ${e.message}`)
    return { close: () => {} }
  }

  ws.onopen = () => {
    onProgress?.(0, 0, null, '已连接 ComfyUI WebSocket，等待生成...')
  }

  ws.onerror = (e) => {
    cleanup()
    onError?.(`WebSocket 连接失败 — 请检查 Pod 地址和 Auth Key（错误: ${e.message || 'network error'}）`)
  }

  ws.onclose = (e) => {
    clearTimeout(timeoutId)
    // code 1000 = normal close; others may be unexpected
    if (e.code !== 1000 && e.code !== 1005) {
      onError?.(`WebSocket 意外断开 (code ${e.code})`)
    }
  }

  ws.onmessage = (event) => {
    let msg
    try {
      msg = typeof event.data === 'string'
        ? JSON.parse(event.data)
        : null  // binary frame (preview images) — skip
    } catch { return }
    if (!msg) return

    const { type, data } = msg

    if (type === 'progress') {
      const { value, max, node } = data
      onProgress?.(value, max, node, `节点 ${node} — 步进 ${value}/${max}`)
    }

    else if (type === 'executing') {
      if (!data.node) return  // null node = workflow done marker
      onNodeStart?.(data.node, '')
      onProgress?.(0, 0, data.node, `执行节点 ${data.node}...`)
    }

    else if (type === 'executed') {
      if (data.prompt_id !== promptId) return  // belongs to another job
      const images = data.output?.images || []
      onNodeDone?.(data.node, images)
      if (images.length) {
        onProgress?.(1, 1, data.node, `节点 ${data.node} 完成，输出 ${images.length} 张图`)
      }
    }

    else if (type === 'execution_success') {
      if (data.prompt_id !== promptId) return
      // Fetch final outputs via HTTP history as WS only gives partial info
      cleanup()
      onDone?.(promptId)  // caller should fetch /history/{id} to get full image list
    }

    else if (type === 'execution_error') {
      if (data.prompt_id !== promptId) return
      cleanup()
      const detail = data.exception_message || data.error || JSON.stringify(data).slice(0, 200)
      onError?.(`ComfyUI 执行错误: ${detail}`)
    }

    else if (type === 'status') {
      const q = data?.status?.exec_info?.queue_remaining
      if (q !== undefined && q > 0) {
        onProgress?.(0, 0, null, `排队中，前方还有 ${q} 个任务...`)
      }
    }
  }

  return { close: cleanup }
}

/**
 * 一站式 WebSocket 生成（提交 + WS 监听 + HTTP 拉取最终图）
 * 优先用 WS，WS 失败自动降级到 HTTP 轮询
 * @returns {Promise<{ images: string[], promptId: string }>}
 */
export async function generateWithWs(baseUrl, apiKey, workflowJson, callbacks = {}) {
  const { onProgress, onNodeStart, onNodeDone, onError: _onError } = callbacks

  onProgress?.(0, 0, null, '正在提交到 ComfyUI...')
  const promptId = await submitWorkflow(baseUrl, apiKey, workflowJson)
  onProgress?.(0, 0, null, `✓ 已提交 (${promptId.slice(0, 8)}...)`)

  const client = makeClient(baseUrl, apiKey)

  async function fetchFinalImages() {
    // Poll history once to get the definitive image list
    for (let i = 0; i < 10; i++) {
      await sleep(1500)
      try {
        const resp = await client.get(`/history/${promptId}`)
        const entry = resp.data?.[promptId]
        if (!entry) continue
        const allImages = []
        for (const nodeOut of Object.values(entry.outputs || {})) {
          if (nodeOut.images) allImages.push(...nodeOut.images)
        }
        if (allImages.length) return allImages
      } catch { /* retry */ }
    }
    throw new Error('无法从 /history 获取最终图像，请检查 ComfyUI 日志')
  }

  return new Promise((resolve, reject) => {
    const wsHandle = connectWs(baseUrl, apiKey, promptId, {
      onProgress,
      onNodeStart,
      onNodeDone,
      onDone: async () => {
        try {
          const imageInfos = await fetchFinalImages()
          const images = imageInfos.map(info => buildImageUrl(baseUrl, info))
          resolve({ images, promptId })
        } catch (e) { reject(e) }
      },
      onError: async (msg) => {
        // Fallback: try HTTP polling when WS fails (e.g. auth mismatch on WS)
        if (msg.includes('WebSocket') || msg.includes('连接失败')) {
          onProgress?.(0, 0, null, `⚠️ WS 失败，切换到 HTTP 轮询: ${msg}`)
          try {
            const imageInfos = await pollUntilDone(baseUrl, apiKey, promptId, onProgress)
            const images = imageInfos.map(info => buildImageUrl(baseUrl, info))
            resolve({ images, promptId })
          } catch (e2) { reject(e2) }
        } else {
          reject(new Error(msg))
        }
      },
    })

    // Safety: also set overall timeout
    setTimeout(() => {
      wsHandle.close()
      reject(new Error('整体超时（10分钟）'))
    }, 600000)
  })
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
