<template>
  <div class="tg-page">
    <div class="tg-card">

      <!-- Header -->
      <div class="tg-header">
        <div class="tg-title-row">
          <span class="tg-dot"></span>
          <h1 class="tg-title">API 出图测试台</h1>
          <span class="tg-badge">DEV ONLY</span>
        </div>
        <p class="tg-sub">直连 RunPod · 完整 response 打印到控制台 · 验证 API 链路</p>
      </div>

      <!-- Step 1: API Key -->
      <div class="tg-section">
        <label class="tg-label">① RunPod API Key</label>
        <div class="tg-row">
          <input
            v-model="apiKey"
            :type="showKey ? 'text' : 'password'"
            class="tg-input mono"
            placeholder="Bearer Key（仅存本地，不提交代码）"
            spellcheck="false"
            autocomplete="off"
          />
          <button class="tg-btn-sm" @click="showKey = !showKey">{{ showKey ? '🙈' : '👁️' }}</button>
          <button class="tg-btn-sm green" @click="saveKey">保存</button>
        </div>
        <p v-if="keySaved" class="tg-hint green">✓ 已保存到 localStorage</p>
      </div>

      <!-- Step 2: Model -->
      <div class="tg-section">
        <label class="tg-label">② 选择模型端点</label>
        <div class="tg-model-grid">
          <button
            v-for="m in quickModels"
            :key="m.id"
            class="tg-model-btn"
            :class="{ active: selectedModelId === m.id }"
            @click="selectModel(m)"
          >
            <span class="tg-mbadge" :style="{ background: m.color }">{{ m.badge }}</span>
            <span class="tg-mname">{{ m.label }}</span>
            <span class="tg-mendpoint">{{ m.endpointId }}</span>
          </button>
        </div>
      </div>

      <!-- Step 3: Inputs -->
      <div class="tg-section">
        <label class="tg-label">③ Prompt</label>
        <textarea
          v-model="prompt"
          class="tg-input tg-textarea"
          rows="4"
          placeholder="描述要生成的内容，支持中英文。例如：一件深海蓝丝绒晚礼服，V领，模特正面，白背景商业摄影"
          :disabled="running"
        ></textarea>
      </div>

      <div class="tg-section">
        <label class="tg-label">④ 参考图片 URL <span class="tg-opt">（编辑/视频模型必填，其余可选）</span></label>
        <input
          v-model="refImage"
          class="tg-input"
          placeholder="https://..."
          :disabled="running"
        />
        <div v-if="refImage" class="tg-ref-preview">
          <img :src="refImage" @error="refImage = ''" alt="参考图" />
        </div>
      </div>

      <!-- Raw JSON override -->
      <details class="tg-details">
        <summary class="tg-summary">⚙️ 高级 · 直接编辑 JSON input（会覆盖上面的设置）</summary>
        <textarea
          v-model="rawJson"
          class="tg-input tg-textarea mono"
          rows="8"
          spellcheck="false"
          placeholder='{"prompt":"...","num_inference_steps":28,"guidance":2,"seed":-1,"size":"1024*1024","output_format":"png","enable_safety_checker":true}'
        ></textarea>
        <p class="tg-hint">留空则使用上方表单参数自动构建</p>
      </details>

      <!-- Run Button -->
      <button
        class="tg-run-btn"
        :disabled="running || !apiKey.trim() || !prompt.trim()"
        @click="runTest"
      >
        <span v-if="running" class="tg-spinner">⏳</span>
        <span v-else>🚀</span>
        {{ running ? statusText : '提交出图' }}
      </button>

      <!-- Progress -->
      <div v-if="running || log.length" class="tg-log-wrap">
        <div class="tg-log-head">
          <span class="tg-log-title">📋 执行日志</span>
          <button class="tg-btn-sm" @click="log = []">清空</button>
        </div>
        <div class="tg-log-body" ref="logEl">
          <div
            v-for="(entry, i) in log"
            :key="i"
            class="tg-log-line"
            :class="entry.type"
          >
            <span class="tg-log-time">{{ entry.time }}</span>
            <span class="tg-log-msg">{{ entry.msg }}</span>
          </div>
        </div>
      </div>

      <!-- Result -->
      <div v-if="resultImages.length || resultVideo" class="tg-result">
        <div class="tg-result-head">
          <span class="tg-result-title">✨ 生成结果</span>
          <div class="tg-result-actions">
            <span class="tg-result-meta">seed: {{ resultSeed }}</span>
            <button class="tg-btn-sm" @click="copyUrl">📋 复制 URL</button>
          </div>
        </div>

        <!-- Video -->
        <video
          v-if="resultVideo"
          :src="resultVideo"
          class="tg-result-video"
          controls
          autoplay
          loop
          muted
          playsinline
        ></video>

        <!-- Images -->
        <div v-for="(img, i) in resultImages" :key="i" class="tg-result-img-wrap">
          <img :src="toSrc(img)" class="tg-result-img" :alt="`生成图 ${i+1}`" />
          <div class="tg-result-img-actions">
            <a :href="toSrc(img)" :download="`deepay-${Date.now()}-${i}.png`" class="tg-btn-sm green">⬇ 下载</a>
            <button class="tg-btn-sm" @click="copyToClipboard(toSrc(img))">📋 复制图链接</button>
          </div>
        </div>

        <!-- Raw JSON -->
        <details class="tg-details" style="margin-top:12px">
          <summary class="tg-summary">📦 完整 API Response (JSON)</summary>
          <pre class="tg-json">{{ JSON.stringify(rawResponse, null, 2) }}</pre>
        </details>
      </div>

      <!-- Error -->
      <div v-if="errorMsg" class="tg-error">
        <span>❌ {{ errorMsg }}</span>
        <button class="tg-btn-sm" @click="errorMsg = ''">✕</button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import axios from 'axios'

// ── Quick models (the most useful ones for testing) ─────────────
const quickModels = [
  {
    id: 'flux-kontext',
    label: 'Flux.1 Kontext',
    endpointId: 'black-forest-labs-flux-1-kontext-dev',
    badge: '编辑',
    color: '#7c3aed',
    defaults: { num_inference_steps: 28, guidance: 2 },
  },
  {
    id: 'flux-schnell',
    label: 'Flux.1 Schnell',
    endpointId: 'black-forest-labs-flux-1-schnell',
    badge: '极速',
    color: '#f59e0b',
    defaults: { num_inference_steps: 4, guidance: 0 },
  },
  {
    id: 'flux-dev',
    label: 'Flux.1 Dev',
    endpointId: 'black-forest-labs-flux-1-dev',
    badge: '旗舰',
    color: '#10a37f',
    defaults: { num_inference_steps: 28, guidance: 3.5 },
  },
  {
    id: 'qwen-edit-2511',
    label: 'Qwen Edit 2511',
    endpointId: 'qwen-image-edit-2511',
    badge: '中文',
    color: '#ef4444',
    defaults: { num_inference_steps: 20, guidance: 7.5 },
  },
  {
    id: 'wan-2.5-i2v',
    label: 'WAN 2.5 i2v',
    endpointId: 'alibaba-wan-2-5-i2v',
    badge: '视频',
    color: '#0ea5e9',
    defaults: { num_inference_steps: 25, guidance: 7.5, num_frames: 81, fps: 16 },
  },
  {
    id: 'kling-v2.1-i2v',
    label: 'Kling v2.1 Pro',
    endpointId: 'kwaivgi-kling-v2-1-i2v-pro',
    badge: 'Pro',
    color: '#ec4899',
    defaults: { num_inference_steps: 30, guidance: 7.5, num_frames: 97, fps: 24 },
  },
]

const BASE = 'https://api.runpod.ai/v2'
const POLL_MS = 2000
const TIMEOUT_MS = 600000

// ── State ────────────────────────────────────────────────────────
const apiKey         = ref(localStorage.getItem('deepay_runpod_key') || '')
const showKey        = ref(false)
const keySaved       = ref(false)

const selectedModelId = ref('flux-kontext')
const selectedModel   = ref(quickModels[0])
const prompt          = ref('一件深海蓝真丝晚礼服，V领设计，肩部裸露，模特正面站立，白色摄影棚背景，超清商业摄影')
const refImage        = ref('https://image.runpod.ai/asset/black-forest-labs/black-forest-labs-flux-1-kontext-dev.png')
const rawJson         = ref('')

const running     = ref(false)
const statusText  = ref('')
const log         = ref([])
const logEl       = ref(null)

const resultImages  = ref([])
const resultVideo   = ref('')
const resultSeed    = ref(-1)
const rawResponse   = ref(null)
const errorMsg      = ref('')

// ── Methods ───────────────────────────────────────────────────────
function saveKey() {
  localStorage.setItem('deepay_runpod_key', apiKey.value.trim())
  keySaved.value = true
  setTimeout(() => { keySaved.value = false }, 3000)
}

function selectModel(m) {
  selectedModelId.value = m.id
  selectedModel.value   = m
}

function addLog(type, msg) {
  const time = new Date().toLocaleTimeString('zh-CN', { hour12: false })
  log.value.push({ type, msg, time })
  nextTick(() => {
    if (logEl.value) logEl.value.scrollTop = logEl.value.scrollHeight
  })
}

function buildInput() {
  // If user provided raw JSON, parse and use it
  if (rawJson.value.trim()) {
    try {
      return JSON.parse(rawJson.value)
    } catch {
      throw new Error('JSON 格式错误，请检查高级输入框')
    }
  }

  const m = selectedModel.value
  const isVideo = ['wan-2.5-i2v', 'wan-2.2-i2v', 'wan-2.2-i2v-lora', 'wan-2.1-i2v', 'kling-v2.1-i2v'].includes(m.id)

  const input = {
    prompt:                prompt.value.trim(),
    negative_prompt:       '',
    seed:                  -1,
    num_inference_steps:   m.defaults.num_inference_steps,
    output_format:         'png',
    enable_safety_checker: true,
  }

  if (isVideo) {
    input.guidance_scale = m.defaults.guidance
    input.num_frames     = m.defaults.num_frames
    input.fps            = m.defaults.fps
    if (refImage.value.trim()) input.image = refImage.value.trim()
  } else {
    input.guidance = m.defaults.guidance
    input.size     = '1024*1024'
    if (refImage.value.trim()) input.image = refImage.value.trim()
  }

  return input
}

async function runTest() {
  if (running.value) return

  running.value   = true
  errorMsg.value  = ''
  resultImages.value = []
  resultVideo.value  = ''
  rawResponse.value  = null

  const key = apiKey.value.trim()
  const m   = selectedModel.value

  const client = axios.create({
    baseURL: BASE,
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${key}`,
    },
    timeout: 30000,
  })

  try {
    // ── Step 1: Submit ─────────────────────────────────────────
    let input
    try {
      input = buildInput()
    } catch (e) {
      throw e
    }

    addLog('info', `提交到端点: ${m.endpointId}`)
    addLog('info', `Input: ${JSON.stringify(input).slice(0, 200)}...`)

    statusText.value = '提交中...'
    const submitResp = await client.post(`/${m.endpointId}/run`, { input })
    const jobId = submitResp.data?.id
    if (!jobId) throw new Error(`提交失败，未返回 jobId。Response: ${JSON.stringify(submitResp.data)}`)

    addLog('success', `✓ 任务已提交，jobId: ${jobId}`)
    console.log('[TestGen] Submit response:', submitResp.data)

    // ── Step 2: Poll ────────────────────────────────────────────
    const start = Date.now()
    let pollCount = 0

    while (true) {
      if (Date.now() - start > TIMEOUT_MS) throw new Error('轮询超时（10分钟）')

      await sleep(POLL_MS)
      pollCount++

      const pollResp = await client.get(`/${m.endpointId}/status/${jobId}`)
      const { status, output, error } = pollResp.data
      const elapsed = Math.round((Date.now() - start) / 1000)

      statusText.value = `${statusLabel(status)} (${elapsed}s · ${pollCount}次轮询)`
      addLog(status === 'FAILED' ? 'error' : 'info', `[${pollCount}] ${status} (${elapsed}s)`)

      console.log(`[TestGen] Poll #${pollCount}:`, pollResp.data)

      if (status === 'COMPLETED') {
        rawResponse.value = pollResp.data
        addLog('success', `✅ 完成！耗时 ${elapsed}s`)
        console.log('[TestGen] ✅ Final output:', output)
        parseOutput(output)
        break
      }

      if (['FAILED', 'CANCELLED', 'TIMED_OUT'].includes(status)) {
        rawResponse.value = pollResp.data
        throw new Error(error || `任务${statusLabel(status)}`)
      }
    }

  } catch (err) {
    errorMsg.value = err.message || String(err)
    addLog('error', `❌ ${errorMsg.value}`)
    console.error('[TestGen] Error:', err)
  } finally {
    running.value = false
    statusText.value = ''
  }
}

function parseOutput(output) {
  if (!output) { errorMsg.value = '生成结果为空'; return }

  // Video
  if (output.video)       { resultVideo.value = output.video;       resultSeed.value = output.seed ?? -1; return }
  if (output.video_url)   { resultVideo.value = output.video_url;   resultSeed.value = output.seed ?? -1; return }

  // Image array
  if (Array.isArray(output)) {
    const imgs = output.map(i => typeof i === 'string' ? i : (i.url || i.image || '')).filter(Boolean)
    if (imgs.length) { resultImages.value = imgs; resultSeed.value = output[0]?.seed ?? -1; return }
  }
  if (output.images) { resultImages.value = output.images; resultSeed.value = output.seed ?? -1; return }
  if (output.image)  { resultImages.value = [output.image]; resultSeed.value = output.seed ?? -1; return }
  if (typeof output === 'string') { resultImages.value = [output]; return }

  errorMsg.value = `无法解析输出，请查看控制台。Output keys: ${Object.keys(output || {}).join(', ')}`
}

function toSrc(raw) {
  if (!raw) return ''
  if (raw.startsWith('http') || raw.startsWith('blob:')) return raw
  if (!raw.startsWith('data:')) return `data:image/png;base64,${raw}`
  return raw
}

function statusLabel(s) {
  return { IN_QUEUE: '排队中', IN_PROGRESS: '生成中', COMPLETED: '完成', FAILED: '失败', CANCELLED: '取消', TIMED_OUT: '超时' }[s] || s
}

function sleep(ms) { return new Promise(r => setTimeout(r, ms)) }

function copyUrl() {
  const url = resultImages.value[0] ? toSrc(resultImages.value[0]) : resultVideo.value
  if (url) navigator.clipboard?.writeText(url)
}

function copyToClipboard(text) {
  navigator.clipboard?.writeText(text)
}
</script>

<style scoped>
.tg-page {
  min-height: 100vh;
  background: #0a0a0a;
  display: flex;
  justify-content: center;
  padding: 32px 16px 80px;
  color: #e5e5e5;
}

.tg-card {
  width: 100%;
  max-width: 760px;
}

/* Header */
.tg-header { margin-bottom: 32px; }
.tg-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}
.tg-dot {
  width: 10px;
  height: 10px;
  background: #10a37f;
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50%       { opacity: 0.6; transform: scale(1.3); }
}
.tg-title {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin: 0;
}
.tg-badge {
  padding: 2px 8px;
  background: rgba(239,68,68,0.15);
  border: 1px solid rgba(239,68,68,0.3);
  border-radius: 6px;
  font-size: 11px;
  font-weight: 700;
  color: #ef4444;
}
.tg-sub { font-size: 13px; color: #666; margin: 0; }

/* Sections */
.tg-section { margin-bottom: 24px; }
.tg-label {
  display: block;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: #888;
  margin-bottom: 8px;
}
.tg-opt { font-weight: 400; text-transform: none; letter-spacing: 0; color: #555; }

/* Inputs */
.tg-input {
  width: 100%;
  background: #141414;
  border: 1px solid #262626;
  border-radius: 10px;
  padding: 10px 14px;
  font-size: 14px;
  color: #e5e5e5;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s;
  font-family: inherit;
}
.tg-input:focus { border-color: #10a37f; }
.tg-input::placeholder { color: #444; }
.tg-input:disabled { opacity: 0.5; cursor: not-allowed; }
.tg-textarea { resize: vertical; line-height: 1.6; }
.mono { font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace; font-size: 13px; }

.tg-row {
  display: flex;
  gap: 8px;
  align-items: center;
}
.tg-row .tg-input { flex: 1; }

/* Buttons */
.tg-btn-sm {
  padding: 8px 12px;
  background: #1a1a1a;
  border: 1px solid #2a2a2a;
  border-radius: 8px;
  color: #aaa;
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.15s;
  flex-shrink: 0;
}
.tg-btn-sm:hover { border-color: #404040; color: #e5e5e5; }
.tg-btn-sm.green { border-color: rgba(16,163,127,0.4); color: #10a37f; }
.tg-btn-sm.green:hover { background: rgba(16,163,127,0.1); }

/* Model grid */
.tg-model-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
@media (max-width: 600px) { .tg-model-grid { grid-template-columns: 1fr 1fr; } }

.tg-model-btn {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 12px;
  background: #111;
  border: 1px solid #222;
  border-radius: 10px;
  cursor: pointer;
  text-align: left;
  transition: all 0.15s;
}
.tg-model-btn:hover { border-color: #333; background: #181818; }
.tg-model-btn.active {
  border-color: #10a37f;
  background: rgba(16,163,127,0.08);
}
.tg-mbadge {
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 10px;
  font-weight: 700;
  color: white;
}
.tg-mname { font-size: 13px; font-weight: 600; color: #e5e5e5; }
.tg-mendpoint { font-size: 10px; color: #555; font-family: monospace; }

/* Details */
.tg-details {
  margin-bottom: 20px;
  border: 1px solid #1e1e1e;
  border-radius: 10px;
  overflow: hidden;
}
.tg-summary {
  padding: 10px 14px;
  font-size: 12px;
  font-weight: 600;
  color: #666;
  cursor: pointer;
  user-select: none;
  list-style: none;
  background: #111;
}
.tg-summary::-webkit-details-marker { display: none; }
.tg-details[open] .tg-summary { border-bottom: 1px solid #1e1e1e; }
.tg-details .tg-input { border-radius: 0; border: none; border-top: none; }
.tg-hint { font-size: 11px; color: #555; margin: 6px 14px; }
.tg-hint.green { color: #10a37f; }

/* Run button */
.tg-run-btn {
  width: 100%;
  padding: 14px;
  background: #10a37f;
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: background 0.15s, transform 0.1s;
  margin-bottom: 24px;
}
.tg-run-btn:hover:not(:disabled) { background: #0d8b6e; transform: translateY(-1px); }
.tg-run-btn:disabled { opacity: 0.4; cursor: not-allowed; transform: none; }
.tg-spinner { animation: spin 1s linear infinite; display: inline-block; }
@keyframes spin { to { transform: rotate(360deg); } }

/* Log */
.tg-log-wrap {
  border: 1px solid #1a1a1a;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 24px;
}
.tg-log-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  background: #111;
  border-bottom: 1px solid #1a1a1a;
}
.tg-log-title { font-size: 12px; font-weight: 600; color: #888; }
.tg-log-body {
  background: #0d0d0d;
  max-height: 200px;
  overflow-y: auto;
  padding: 10px 0;
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 12px;
}
.tg-log-line {
  display: flex;
  gap: 12px;
  padding: 3px 14px;
  transition: background 0.1s;
}
.tg-log-line:hover { background: rgba(255,255,255,0.03); }
.tg-log-time { color: #444; flex-shrink: 0; }
.tg-log-msg { color: #888; word-break: break-all; }
.tg-log-line.success .tg-log-msg { color: #10a37f; }
.tg-log-line.error   .tg-log-msg { color: #ef4444; }
.tg-log-line.info    .tg-log-msg { color: #888; }

/* Result */
.tg-result {
  border: 1px solid #1a2a1a;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 24px;
  background: #0d160d;
}
.tg-result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #1a2a1a;
  gap: 12px;
  flex-wrap: wrap;
}
.tg-result-title { font-size: 13px; font-weight: 700; color: #10a37f; }
.tg-result-actions { display: flex; gap: 8px; align-items: center; }
.tg-result-meta { font-size: 11px; color: #555; font-family: monospace; }

.tg-result-video {
  width: 100%;
  max-height: 480px;
  display: block;
  background: #000;
}
.tg-result-img-wrap { position: relative; }
.tg-result-img {
  width: 100%;
  max-height: 600px;
  object-fit: contain;
  display: block;
  background: #000;
}
.tg-result-img-actions {
  display: flex;
  gap: 8px;
  padding: 10px 14px;
  background: rgba(0,0,0,0.4);
  border-top: 1px solid #1a2a1a;
}

/* JSON */
.tg-json {
  background: #0a0a0a;
  padding: 14px;
  font-size: 11px;
  font-family: 'SF Mono', 'Fira Code', monospace;
  color: #6b7280;
  overflow: auto;
  max-height: 360px;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

/* Reference image preview */
.tg-ref-preview {
  margin-top: 8px;
  border-radius: 8px;
  overflow: hidden;
  max-height: 150px;
  border: 1px solid #222;
}
.tg-ref-preview img {
  width: 100%;
  height: 150px;
  object-fit: cover;
  display: block;
}

/* Error */
.tg-error {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  background: rgba(239,68,68,0.08);
  border: 1px solid rgba(239,68,68,0.25);
  border-radius: 10px;
  font-size: 13px;
  color: #f87171;
  margin-bottom: 16px;
  word-break: break-word;
}
</style>
