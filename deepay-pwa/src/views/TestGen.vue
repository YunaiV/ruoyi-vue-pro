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

      <!-- Mode Tabs -->
      <div class="tg-mode-tabs">
        <button class="tg-mode-tab" :class="{ active: mode === 'serverless' }" @click="mode = 'serverless'">
          ☁️ RunPod 无服务器 API
        </button>
        <button class="tg-mode-tab" :class="{ active: mode === 'comfyui' }" @click="mode = 'comfyui'">
          🖥️ ComfyUI Pod 直连
        </button>
      </div>

      <!-- ══════════════════════════════════════════════════════════
           TAB A: RunPod Serverless
           ══════════════════════════════════════════════════════════ -->
      <template v-if="mode === 'serverless'">

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

        <!-- Step 3: Prompt -->
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

        <!-- Step 4: Ref Image -->
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
          <summary class="tg-summary">⚙️ 高级 · 直接编辑 JSON input</summary>
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
          @click="runServerless"
        >
          <span v-if="running" class="tg-spinner">⏳</span>
          <span v-else>🚀</span>
          {{ running ? statusText : '提交出图 (Serverless)' }}
        </button>

      </template>

      <!-- ══════════════════════════════════════════════════════════
           TAB B: ComfyUI Direct
           ══════════════════════════════════════════════════════════ -->
      <template v-if="mode === 'comfyui'">

        <!-- Pod URL + Auth Key -->
        <div class="tg-section">
          <label class="tg-label">① Pod 地址 + Auth Key</label>
          <p class="tg-info-box">
            📍 格式：<code>https://[pod-id]-8188.proxy.runpod.net</code><br/>
            🔒 你的镜像启用了 Built-in Auth，必须填写 Auth Key，否则返回 401/403
          </p>
          <input
            v-model="comfyUrl"
            class="tg-input"
            placeholder="https://x1ik3wb0zvch3v-64410f01-8188.proxy.runpod.net"
            spellcheck="false"
            autocomplete="off"
            :disabled="cRunning"
          />
          <div class="tg-row" style="margin-top:8px">
            <input
              v-model="comfyKey"
              :type="showComfyKey ? 'text' : 'password'"
              class="tg-input mono"
              placeholder="Auth Key（ComfyUI 启动时设置的密钥）"
              spellcheck="false"
              autocomplete="off"
              :disabled="cRunning"
            />
            <button class="tg-btn-sm" @click="showComfyKey = !showComfyKey">{{ showComfyKey ? '🙈' : '👁️' }}</button>
          </div>
          <div class="tg-row" style="margin-top:8px;gap:8px">
            <button class="tg-btn-sm green" @click="saveComfy">💾 保存</button>
            <button class="tg-btn-sm" :disabled="!comfyUrl.trim() || cPinging" @click="doPing">
              {{ cPinging ? '检测中...' : '🔗 测试连通性' }}
            </button>
            <span v-if="pingResult" class="tg-ping-badge" :class="pingResult.ok ? 'ok' : 'err'">
              {{ pingResult.ok ? `✅ ${pingResult.version}` : `❌ ${pingResult.error}` }}
            </span>
          </div>
        </div>

        <!-- Workflow mode -->
        <div class="tg-section">
          <label class="tg-label">② 工作流模式</label>
          <div class="tg-model-grid" style="grid-template-columns:1fr 1fr">
            <button
              class="tg-model-btn"
              :class="{ active: wfMode === 'template' }"
              @click="wfMode = 'template'"
            >
              <span class="tg-mbadge" style="background:#10a37f">模板</span>
              <span class="tg-mname">使用内置模板</span>
              <span class="tg-mendpoint">自动注入 Prompt + 参数</span>
            </button>
            <button
              class="tg-model-btn"
              :class="{ active: wfMode === 'custom' }"
              @click="wfMode = 'custom'"
            >
              <span class="tg-mbadge" style="background:#7c3aed">自定义</span>
              <span class="tg-mname">粘贴 workflow_api.json</span>
              <span class="tg-mendpoint">从 ComfyUI 界面导出</span>
            </button>
          </div>
        </div>

        <!-- Template: select + params -->
        <template v-if="wfMode === 'template'">
          <div class="tg-section">
            <label class="tg-label">③ 选择工作流模板</label>
            <div class="tg-model-grid" style="grid-template-columns:1fr 1fr">
              <button
                v-for="wf in builtinWorkflows"
                :key="wf.id"
                class="tg-model-btn"
                :class="{ active: selectedWf === wf.id }"
                @click="selectedWf = wf.id"
              >
                <span class="tg-mbadge" :style="{ background: wf.color }">{{ wf.badge }}</span>
                <span class="tg-mname">{{ wf.label }}</span>
                <span class="tg-mendpoint">{{ wf.desc }}</span>
              </button>
            </div>
          </div>

          <div class="tg-section">
            <label class="tg-label">④ Prompt</label>
            <textarea
              v-model="cPrompt"
              class="tg-input tg-textarea"
              rows="4"
              placeholder="描述要生成或修改的内容。例如：一件深海蓝真丝晚礼服，V领设计，白色摄影棚背景，超清商业摄影"
              :disabled="cRunning"
            ></textarea>
          </div>

          <div class="tg-section" v-if="selectedWfObj?.needsImage">
            <label class="tg-label">⑤ 参考图片 URL <span class="tg-opt">（此工作流必填）</span></label>
            <input v-model="cRefImage" class="tg-input" placeholder="https://..." :disabled="cRunning" />
            <div v-if="cRefImage" class="tg-ref-preview">
              <img :src="cRefImage" @error="cRefImage = ''" alt="参考图" />
            </div>
          </div>

          <!-- Node IDs customisation -->
          <details class="tg-details">
            <summary class="tg-summary">⚙️ 节点 ID 映射（默认自动检测，通常不需要修改）</summary>
            <div class="tg-params-grid">
              <div>
                <label class="tg-label" style="text-transform:none;font-size:11px">正向 Prompt 节点 ID</label>
                <input v-model="nodePrompt" class="tg-input mono" style="padding:6px 10px;font-size:12px" />
              </div>
              <div>
                <label class="tg-label" style="text-transform:none;font-size:11px">KSampler 节点 ID</label>
                <input v-model="nodeSampler" class="tg-input mono" style="padding:6px 10px;font-size:12px" />
              </div>
              <div>
                <label class="tg-label" style="text-transform:none;font-size:11px">Latent 节点 ID</label>
                <input v-model="nodeLatent" class="tg-input mono" style="padding:6px 10px;font-size:12px" />
              </div>
              <div>
                <label class="tg-label" style="text-transform:none;font-size:11px">参考图节点 ID</label>
                <input v-model="nodeImage" class="tg-input mono" style="padding:6px 10px;font-size:12px" />
              </div>
            </div>
          </details>
        </template>

        <!-- Custom JSON workflow -->
        <template v-if="wfMode === 'custom'">
          <div class="tg-section">
            <label class="tg-label">③ 粘贴 workflow_api.json</label>
            <p class="tg-info-box">
              在 ComfyUI 界面 → ⚙️ Settings → Enable Dev Mode → 保存按钮旁边选择 <strong>Save (API Format)</strong>，复制 JSON 粘贴到下方
            </p>
            <textarea
              v-model="customWfJson"
              class="tg-input tg-textarea mono"
              rows="14"
              spellcheck="false"
              placeholder='{"6":{"class_type":"CLIPTextEncode","inputs":{"text":"your prompt here",...}},...}'
              :disabled="cRunning"
            ></textarea>
            <div v-if="detectedNodes" class="tg-detected">
              <span class="tg-detected-label">🔍 自动检测到的节点：</span>
              <span v-if="detectedNodes.promptNodes.length">Prompt: {{ detectedNodes.promptNodes.map(n=>n.id).join(', ') }}</span>
              <span v-if="detectedNodes.samplerNodes.length">· Sampler: {{ detectedNodes.samplerNodes.map(n=>n.id).join(', ') }}</span>
              <span v-if="detectedNodes.saveNodes.length">· Output: {{ detectedNodes.saveNodes.map(n=>n.id).join(', ') }}</span>
            </div>
          </div>

          <div class="tg-section">
            <label class="tg-label">④ 注入 Prompt（可选）</label>
            <p class="tg-hint" style="margin:0 0 8px">如果你已经在 JSON 里写好了 Prompt，这里可以留空；填写则会覆盖 JSON 里的文本</p>
            <div class="tg-row">
              <input v-model="cPromptNode" class="tg-input mono" style="max-width:80px" placeholder="节点ID" />
              <textarea
                v-model="cPrompt"
                class="tg-input tg-textarea"
                rows="2"
                placeholder="要注入的提示词（留空则不修改 JSON）"
                :disabled="cRunning"
                style="flex:1"
              ></textarea>
            </div>
          </div>
        </template>

        <!-- Run -->
        <button
          class="tg-run-btn"
          :class="{ purple: true }"
          :disabled="cRunning || !comfyUrl.trim() || (wfMode === 'custom' && !customWfJson.trim())"
          @click="runComfy"
        >
          <span v-if="cRunning" class="tg-spinner">⏳</span>
          <span v-else>🎨</span>
          {{ cRunning ? cStatusText : '提交到 ComfyUI' }}
        </button>

      </template>

      <!-- ── ComfyUI：实时进度条 ────────────────── -->
      <div v-if="mode === 'comfyui' && (cRunning || wsProgress.step > 0)" class="tg-progress-wrap">
        <div class="tg-progress-header">
          <span class="tg-progress-label">{{ wsProgress.msg || '生成中...' }}</span>
          <span class="tg-progress-pct" v-if="wsProgress.max > 0">{{ wsProgress.step }}/{{ wsProgress.max }}</span>
        </div>
        <div class="tg-progress-bar">
          <div
            class="tg-progress-fill"
            :style="{ width: wsProgress.max > 0 ? (wsProgress.step / wsProgress.max * 100) + '%' : '0%' }"
          ></div>
        </div>
        <div v-if="wsProgress.activeNode" class="tg-node-pills">
          <span
            v-for="n in wsProgress.executedNodes"
            :key="n"
            class="tg-node-pill done"
          >✓ {{ n }}</span>
          <span class="tg-node-pill active">⚡ {{ wsProgress.activeNode }}</span>
        </div>
      </div>

      <!-- ── 共用：执行日志 ──────────────────────────── -->
      <div v-if="running || cRunning || log.length" class="tg-log-wrap">
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

      <!-- ── 共用：结果 ──────────────────────────────── -->
      <div v-if="resultImages.length || resultVideo" class="tg-result">
        <div class="tg-result-head">
          <span class="tg-result-title">✨ 生成结果</span>
          <div class="tg-result-actions">
            <span class="tg-result-meta">seed: {{ resultSeed }}</span>
            <button
              v-if="mode === 'comfyui' && cRefImage && resultImages.length"
              class="tg-btn-sm"
              :class="{ active: showCompare }"
              @click="showCompare = !showCompare"
            >{{ showCompare ? '关闭对比' : '⇔ 前后对比' }}</button>
            <button class="tg-btn-sm" @click="copyUrl">📋 复制 URL</button>
          </div>
        </div>

        <!-- Before/After Compare -->
        <div v-if="showCompare && cRefImage && resultImages[0]" class="tg-compare-wrap">
          <ImageCompare
            :before="cRefImage"
            :after="toSrc(resultImages[0])"
            before-label="原图"
            after-label="生成后"
          />
        </div>

        <video
          v-if="resultVideo"
          :src="resultVideo"
          class="tg-result-video"
          controls autoplay loop muted playsinline
        ></video>

        <div v-for="(img, i) in resultImages" :key="i" class="tg-result-img-wrap">
          <img :src="toSrc(img)" class="tg-result-img" :alt="`生成图 ${i+1}`" />
          <div class="tg-result-img-actions">
            <a :href="toSrc(img)" :download="`deepay-${Date.now()}-${i}.png`" class="tg-btn-sm green">⬇ 下载</a>
            <button class="tg-btn-sm" @click="copyToClipboard(toSrc(img))">📋 复制链接</button>
          </div>
        </div>

        <details class="tg-details" style="margin-top:12px">
          <summary class="tg-summary">📦 完整 API Response (JSON)</summary>
          <pre class="tg-json">{{ JSON.stringify(rawResponse, null, 2) }}</pre>
        </details>
      </div>

      <!-- ── 错误 ──────────────────────────────────────── -->
      <div v-if="errorMsg" class="tg-error">
        <span>❌ {{ errorMsg }}</span>
        <button class="tg-btn-sm" @click="errorMsg = ''">✕</button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import axios from 'axios'
import {
  pingComfy as pingComfyApi,
  submitWorkflow,
  pollUntilDone,
  buildImageUrl,
  injectParams,
  detectNodes,
  generateWithWs,
  WORKFLOW_FLUX_TEXT2IMG,
  WORKFLOW_FLUX_KONTEXT,
} from '@/api/comfyui.js'
import { useComfyStore } from '@/store/index.js'
import ImageCompare from '@/components/ImageCompare.vue'

const comfyStore = useComfyStore()

// ── Mode ─────────────────────────────────────────────────────────
const mode = ref('comfyui')  // 'serverless' | 'comfyui'

// ════════════════════════════════════════════════════════════════
//  A) RunPod Serverless state
// ════════════════════════════════════════════════════════════════
const quickModels = [
  { id: 'flux-kontext',  label: 'Flux.1 Kontext',  endpointId: 'black-forest-labs-flux-1-kontext-dev', badge: '编辑', color: '#7c3aed', defaults: { num_inference_steps: 28, guidance: 2 } },
  { id: 'flux-schnell',  label: 'Flux.1 Schnell',  endpointId: 'black-forest-labs-flux-1-schnell',     badge: '极速', color: '#f59e0b', defaults: { num_inference_steps: 4,  guidance: 0 } },
  { id: 'flux-dev',      label: 'Flux.1 Dev',      endpointId: 'black-forest-labs-flux-1-dev',         badge: '旗舰', color: '#10a37f', defaults: { num_inference_steps: 28, guidance: 3.5 } },
  { id: 'qwen-edit-2511',label: 'Qwen Edit 2511',  endpointId: 'qwen-image-edit-2511',                 badge: '中文', color: '#ef4444', defaults: { num_inference_steps: 20, guidance: 7.5 } },
  { id: 'wan-2.5-i2v',   label: 'WAN 2.5 i2v',    endpointId: 'alibaba-wan-2-5-i2v',                  badge: '视频', color: '#0ea5e9', defaults: { num_inference_steps: 25, guidance: 7.5, num_frames: 81, fps: 16 } },
  { id: 'kling-v2.1-i2v',label: 'Kling v2.1 Pro', endpointId: 'kwaivgi-kling-v2-1-i2v-pro',           badge: 'Pro',  color: '#ec4899', defaults: { num_inference_steps: 30, guidance: 7.5, num_frames: 97, fps: 24 } },
]

const apiKey        = ref(localStorage.getItem('deepay_runpod_key') || '')
const showKey       = ref(false)
const keySaved      = ref(false)
const selectedModelId = ref('flux-kontext')
const selectedModel   = ref(quickModels[0])
const prompt          = ref('一件深海蓝真丝晚礼服，V领设计，肩部裸露，模特正面站立，白色摄影棚背景，超清商业摄影')
const refImage        = ref('https://image.runpod.ai/asset/black-forest-labs/black-forest-labs-flux-1-kontext-dev.png')
const rawJson         = ref('')
const running         = ref(false)
const statusText      = ref('')

// ════════════════════════════════════════════════════════════════
//  B) ComfyUI Direct state
// ════════════════════════════════════════════════════════════════
const comfyUrl      = ref(comfyStore.podUrl)
const comfyKey      = ref(comfyStore.apiKey)
const showComfyKey  = ref(false)
const cPinging      = ref(false)
const pingResult    = ref(null)

const wfMode        = ref('template')  // 'template' | 'custom'
const selectedWf    = ref('flux-t2i')
const cPrompt       = ref('一件深海蓝真丝晚礼服，V领设计，模特正面，白色摄影棚背景，超清商业摄影，8k')
const cRefImage     = ref('')
const customWfJson  = ref('')
const cPromptNode   = ref('6')     // node ID to inject prompt into (custom mode)
const nodePrompt    = ref('2')
const nodeSampler   = ref('5')
const nodeLatent    = ref('4')
const nodeImage     = ref('10')
const cRunning      = ref(false)
const cStatusText   = ref('')

const builtinWorkflows = [
  { id: 'flux-t2i',      label: 'Flux.1 文生图',    desc: 'flux1-dev.safetensors',         badge: 'T2I',  color: '#10a37f', needsImage: false, template: WORKFLOW_FLUX_TEXT2IMG, promptNode:'2', samplerNode:'5', latentNode:'4', imageNode:'' },
  { id: 'flux-kontext',  label: 'Flux.1 Kontext',   desc: 'flux1-kontext-dev.safetensors', badge: '编辑', color: '#7c3aed', needsImage: true,  template: WORKFLOW_FLUX_KONTEXT,   promptNode:'2', samplerNode:'4', latentNode:'',  imageNode:'10' },
]

const selectedWfObj = computed(() => builtinWorkflows.find(w => w.id === selectedWf.value))

// Auto-detect nodes when custom JSON changes
const detectedNodes = computed(() => {
  if (!customWfJson.value.trim()) return null
  try {
    return detectNodes(JSON.parse(customWfJson.value))
  } catch { return null }
})

// ── Shared output state ─────────────────────────────────────────
const log          = ref([])
const logEl        = ref(null)
const resultImages = ref([])
const resultVideo  = ref('')
const resultSeed   = ref(-1)
const rawResponse  = ref(null)
const errorMsg     = ref('')
const showCompare  = ref(false)

// ComfyUI WebSocket real-time progress
const wsProgress = ref({ step: 0, max: 0, msg: '', activeNode: null, executedNodes: [] })

// Clear results when mode changes
watch(mode, () => {
  log.value = []
  resultImages.value = []
  resultVideo.value  = ''
  rawResponse.value  = null
  errorMsg.value     = ''
})

// ── Helpers ─────────────────────────────────────────────────────
function addLog(type, msg) {
  const time = new Date().toLocaleTimeString('zh-CN', { hour12: false })
  log.value.push({ type, msg, time })
  nextTick(() => { if (logEl.value) logEl.value.scrollTop = logEl.value.scrollHeight })
}

function sleep(ms) { return new Promise(r => setTimeout(r, ms)) }

function toSrc(raw) {
  if (!raw) return ''
  if (raw.startsWith('http') || raw.startsWith('blob:') || raw.startsWith('data:')) return raw
  return `data:image/png;base64,${raw}`
}

function statusLabel(s) {
  return { IN_QUEUE:'排队中', IN_PROGRESS:'生成中', COMPLETED:'完成', FAILED:'失败', CANCELLED:'取消', TIMED_OUT:'超时' }[s] || s
}

function copyUrl() {
  const url = resultImages.value[0] ? toSrc(resultImages.value[0]) : resultVideo.value
  if (url) navigator.clipboard?.writeText(url)
}

function copyToClipboard(text) { navigator.clipboard?.writeText(text) }

// ════════════════════════════════════════════════════════════════
//  A) RunPod Serverless methods
// ════════════════════════════════════════════════════════════════
function saveKey() {
  localStorage.setItem('deepay_runpod_key', apiKey.value.trim())
  keySaved.value = true
  setTimeout(() => { keySaved.value = false }, 3000)
}

function selectModel(m) { selectedModelId.value = m.id; selectedModel.value = m }

function buildServerlessInput() {
  if (rawJson.value.trim()) {
    try { return JSON.parse(rawJson.value) } catch { throw new Error('JSON 格式错误') }
  }
  const m = selectedModel.value
  const isVideo = ['wan-2.5-i2v','wan-2.2-i2v','wan-2.2-i2v-lora','wan-2.1-i2v','kling-v2.1-i2v'].includes(m.id)
  const input = {
    prompt: prompt.value.trim(), negative_prompt: '', seed: -1,
    num_inference_steps: m.defaults.num_inference_steps, output_format: 'png', enable_safety_checker: true,
  }
  if (isVideo) {
    input.guidance_scale = m.defaults.guidance; input.num_frames = m.defaults.num_frames; input.fps = m.defaults.fps
  } else {
    input.guidance = m.defaults.guidance; input.size = '1024*1024'
  }
  if (refImage.value.trim()) input.image = refImage.value.trim()
  return input
}

async function runServerless() {
  if (running.value) return
  running.value = true; errorMsg.value = ''; resultImages.value = []; resultVideo.value = ''; rawResponse.value = null

  const BASE = 'https://api.runpod.ai/v2'
  const client = axios.create({
    baseURL: BASE,
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${apiKey.value.trim()}` },
    timeout: 30000,
  })
  const m = selectedModel.value

  try {
    const input = buildServerlessInput()
    addLog('info', `提交到: ${m.endpointId}`)
    addLog('info', `Input: ${JSON.stringify(input).slice(0,200)}...`)
    statusText.value = '提交中...'
    const submitResp = await client.post(`/${m.endpointId}/run`, { input })
    const jobId = submitResp.data?.id
    if (!jobId) throw new Error(`未返回 jobId。Response: ${JSON.stringify(submitResp.data)}`)
    addLog('success', `✓ 任务已提交，jobId: ${jobId}`)
    console.log('[Serverless] Submit:', submitResp.data)

    const start = Date.now(); let pollCount = 0
    while (true) {
      if (Date.now() - start > 600000) throw new Error('轮询超时')
      await sleep(2000); pollCount++
      const pollResp = await client.get(`/${m.endpointId}/status/${jobId}`)
      const { status, output, error } = pollResp.data
      const elapsed = Math.round((Date.now() - start) / 1000)
      statusText.value = `${statusLabel(status)} (${elapsed}s · #${pollCount})`
      addLog(status === 'FAILED' ? 'error' : 'info', `[${pollCount}] ${status} (${elapsed}s)`)
      console.log(`[Serverless] Poll #${pollCount}:`, pollResp.data)
      if (status === 'COMPLETED') {
        rawResponse.value = pollResp.data
        addLog('success', `✅ 完成！耗时 ${elapsed}s`)
        parseServerlessOutput(output); break
      }
      if (['FAILED','CANCELLED','TIMED_OUT'].includes(status)) {
        rawResponse.value = pollResp.data
        throw new Error(error || `任务${statusLabel(status)}`)
      }
    }
  } catch (err) {
    errorMsg.value = err.message
    addLog('error', `❌ ${err.message}`)
    console.error('[Serverless]', err)
  } finally { running.value = false; statusText.value = '' }
}

function parseServerlessOutput(output) {
  if (!output) { errorMsg.value = '生成结果为空'; return }
  if (output.video)     { resultVideo.value = output.video;     resultSeed.value = output.seed ?? -1; return }
  if (output.video_url) { resultVideo.value = output.video_url; resultSeed.value = output.seed ?? -1; return }
  if (Array.isArray(output)) {
    const imgs = output.map(i => typeof i === 'string' ? i : (i.url || i.image || '')).filter(Boolean)
    if (imgs.length) { resultImages.value = imgs; resultSeed.value = output[0]?.seed ?? -1; return }
  }
  if (output.images) { resultImages.value = output.images; resultSeed.value = output.seed ?? -1; return }
  if (output.image)  { resultImages.value = [output.image]; return }
  if (typeof output === 'string') { resultImages.value = [output]; return }
  errorMsg.value = `无法解析输出，查看控制台。Keys: ${Object.keys(output||{}).join(', ')}`
}

// ════════════════════════════════════════════════════════════════
//  B) ComfyUI Direct methods
// ════════════════════════════════════════════════════════════════
function saveComfy() {
  comfyStore.setPodUrl(comfyUrl.value)
  comfyStore.setApiKey(comfyKey.value)
  addLog('success', '✓ ComfyUI 配置已保存')
}

async function doPing() {
  if (!comfyUrl.value.trim()) return
  cPinging.value = true; pingResult.value = null
  addLog('info', `连接测试: ${comfyUrl.value}`)
  try {
    const r = await pingComfyApi(comfyUrl.value, comfyKey.value)
    pingResult.value = r
    addLog(r.ok ? 'success' : 'error', r.ok ? `✅ 连通正常 · ${r.version}` : `❌ ${r.error}`)
  } catch (e) {
    pingResult.value = { ok: false, error: e.message }
    addLog('error', `❌ ${e.message}`)
  } finally { cPinging.value = false }
}

async function runComfy() {
  if (cRunning.value) return
  cRunning.value = true
  errorMsg.value = ''; resultImages.value = []; resultVideo.value = ''; rawResponse.value = null
  showCompare.value = false
  wsProgress.value = { step: 0, max: 0, msg: '', activeNode: null, executedNodes: [] }

  const baseUrl = comfyUrl.value.trim()
  const authKey = comfyKey.value.trim()

  try {
    let workflowJson

    if (wfMode.value === 'custom') {
      if (!customWfJson.value.trim()) throw new Error('请粘贴 workflow_api.json')
      workflowJson = JSON.parse(customWfJson.value)
      if (cPromptNode.value.trim() && cPrompt.value.trim() && workflowJson[cPromptNode.value]) {
        workflowJson[cPromptNode.value].inputs = workflowJson[cPromptNode.value].inputs || {}
        workflowJson[cPromptNode.value].inputs.text = cPrompt.value.trim()
        addLog('info', `已将 Prompt 注入节点 ${cPromptNode.value}`)
      }
    } else {
      const wfDef = selectedWfObj.value
      if (!wfDef) throw new Error('未选择工作流模板')
      workflowJson = injectParams(wfDef.template, {
        prompt:      cPrompt.value.trim(),
        imageUrl:    cRefImage.value.trim() || undefined,
        promptNode:  nodePrompt.value  || wfDef.promptNode,
        samplerNode: nodeSampler.value || wfDef.samplerNode,
        latentNode:  nodeLatent.value  || wfDef.latentNode,
        imageNode:   nodeImage.value   || wfDef.imageNode,
      })
      addLog('info', `使用模板: ${wfDef.label}`)
    }

    addLog('info', `提交工作流到: ${baseUrl}（WebSocket 实时进度）`)

    const { images, promptId } = await generateWithWs(baseUrl, authKey, workflowJson, {
      onProgress: (step, max, nodeId, msg) => {
        wsProgress.value = {
          step: step ?? wsProgress.value.step,
          max:  max  ?? wsProgress.value.max,
          msg:  msg  || wsProgress.value.msg,
          activeNode:    nodeId || wsProgress.value.activeNode,
          executedNodes: wsProgress.value.executedNodes,
        }
        cStatusText.value = msg || cStatusText.value
        addLog('info', msg || `步进 ${step}/${max}`)
      },
      onNodeStart: (nodeId) => {
        wsProgress.value = {
          ...wsProgress.value,
          step: 0, max: 0,
          activeNode: nodeId,
        }
        addLog('info', `▶ 节点 ${nodeId} 开始执行`)
      },
      onNodeDone: (nodeId, imgs) => {
        const prev = wsProgress.value.executedNodes
        wsProgress.value = {
          ...wsProgress.value,
          executedNodes: prev.includes(nodeId) ? prev : [...prev, nodeId],
          activeNode: wsProgress.value.activeNode === nodeId ? null : wsProgress.value.activeNode,
        }
        if (imgs.length) addLog('success', `✓ 节点 ${nodeId} 完成 (${imgs.length} 张图)`)
      },
    })

    resultImages.value = images
    resultSeed.value   = -1
    rawResponse.value  = { promptId, images }
    wsProgress.value   = { ...wsProgress.value, step: 1, max: 1, msg: `✅ 完成！${images.length} 张图`, activeNode: null }
    addLog('success', `✅ 全部完成！共 ${images.length} 张图`)
    console.log('[ComfyUI] Output:', images)

    // Auto-show compare if we have a reference image
    if (cRefImage.value && images.length) showCompare.value = true

  } catch (err) {
    errorMsg.value = err.message
    addLog('error', `❌ ${err.message}`)
    console.error('[ComfyUI]', err)
  } finally { cRunning.value = false; cStatusText.value = '' }
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
