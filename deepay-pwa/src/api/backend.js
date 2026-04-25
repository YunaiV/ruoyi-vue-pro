/**
 * 后端代理出图 API — Deepay
 *
 * 调用网站自己的 Java 后端（/api/image/），由后端持有 RunPod API Key。
 * 适合生产环境：浏览器永远不会看到 RUNPOD_API_KEY。
 *
 * 调用流程：
 *   1. POST /api/image/generate  → { jobId }         提交工作流
 *   2. GET  /api/image/status/{jobId}                 轮询直到 COMPLETED
 *   3. 返回 images 列表（base64 data-URL 或 CDN URL）
 *
 * 与 runpod.js（直连）的区别：
 *   runpod.js    — 前端直接调 api.runpod.ai，需要在浏览器里有 API Key（测试用）
 *   backend.js   — 前端调自己的后端，后端再调 RunPod（生产用）
 */

import axios from 'axios'

const POLL_INTERVAL_MS = 2000
const POLL_TIMEOUT_MS  = 600000

// 后端 base URL — 同域（相对路径），Vite dev proxy 会自动转发
const backendClient = axios.create({
  baseURL: '/',
  headers: { 'Content-Type': 'application/json' },
  timeout: 30000,
  withCredentials: true,  // 携带 session cookie（若后端有鉴权）
})

/**
 * 通过后端代理生成图片
 *
 * @param {object} workflowJson   ComfyUI workflow_api.json 内容（已 parse 的对象）
 * @param {object} opts
 *   opts.promptNode  {string}   可选：要注入 prompt 的节点 ID（如 '6'）
 *   opts.promptText  {string}   可选：注入的提示词文本
 *   opts.onProgress  {Function} (status, msg) => void  进度回调
 * @returns {Promise<{ images: string[], jobId: string }>}
 */
export async function generateViaBackend(workflowJson, opts = {}) {
  const { promptNode, promptText, onProgress } = opts

  onProgress?.('IN_QUEUE', '正在提交到服务器...')

  // 1. 提交
  const submitResp = await backendClient.post('/api/image/generate', {
    workflow:   workflowJson,
    promptNode: promptNode || undefined,
    promptText: promptText || undefined,
  })

  const submitData = submitResp.data?.data ?? submitResp.data
  if (submitData?.error) throw new Error(submitData.error)

  const jobId = submitData?.jobId
  if (!jobId) throw new Error(`后端未返回 jobId: ${JSON.stringify(submitData).slice(0, 200)}`)

  onProgress?.('IN_QUEUE', `任务已提交 (${jobId.slice(0, 8)}...)`)

  // 2. 轮询
  return pollBackendStatus(jobId, onProgress)
}

/**
 * 轮询后端 /api/image/status/{jobId}
 */
export async function pollBackendStatus(jobId, onProgress) {
  const start = Date.now()
  const LABEL = { IN_QUEUE: '排队中', IN_PROGRESS: '生成中', COMPLETED: '完成', FAILED: '失败' }
  let pollCount = 0

  while (true) {
    if (Date.now() - start > POLL_TIMEOUT_MS) throw new Error('轮询超时（10分钟）')

    await sleep(POLL_INTERVAL_MS)
    pollCount++

    let resp
    try {
      resp = await backendClient.get(`/api/image/status/${jobId}`)
    } catch {
      onProgress?.('IN_PROGRESS', '网络波动，重试中...')
      continue
    }

    const d = resp.data?.data ?? resp.data
    const status = d?.status || 'UNKNOWN'
    const elapsed = Math.round((Date.now() - start) / 1000)

    onProgress?.(status, `${LABEL[status] || status} (${elapsed}s · #${pollCount})`)

    if (status === 'COMPLETED') {
      const images = d.images || []
      if (!images.length) throw new Error('生成完成但未返回图片，请检查后端日志')
      return { images, jobId }
    }

    if (['FAILED', 'CANCELLED', 'TIMED_OUT'].includes(status)) {
      throw new Error(d.error || `任务${LABEL[status] || status}`)
    }
  }
}

/**
 * 检查后端是否已配置 RunPod（通过一个轻量 GET 接口）
 * 如果后端未配置，前端可以 fallback 到直连模式（runpod.js）
 */
export async function checkBackendReady() {
  try {
    // 用一个不存在的 jobId 试探接口是否可达
    const resp = await backendClient.get('/api/image/status/probe_check', { timeout: 5000 })
    // 如果 503 = 未配置，其他状态 = 已配置（404 jobId not found 也算通）
    const d = resp.data?.data ?? resp.data
    return d?.error !== '未配置' && !d?.error?.includes('未配置')
  } catch (e) {
    // 网络不通 = 后端不可达，fallback 直连
    return false
  }
}

function sleep(ms) { return new Promise(r => setTimeout(r, ms)) }
