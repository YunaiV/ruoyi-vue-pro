<template>
  <div class="settings-page">

    <div class="settings-inner">
      <h1 class="page-title">设置</h1>

      <!-- ── 外观 ──────────────────────── -->
      <div class="settings-section">
        <h2 class="section-label">外观</h2>
        <div class="settings-group">

          <div class="settings-row">
            <div class="row-left">
              <span class="row-icon">🌓</span>
              <div class="row-text">
                <span class="row-title">主题模式</span>
                <span class="row-sub">{{ themeStore.isDark ? '深色' : '浅色' }}模式已启用</span>
              </div>
            </div>
            <button
              class="toggle-switch"
              :class="{ on: themeStore.isDark }"
              @click="themeStore.toggle()"
              :aria-label="themeStore.isDark ? '切换到浅色' : '切换到深色'"
            >
              <span class="toggle-thumb"></span>
            </button>
          </div>

          <div class="settings-divider"></div>

          <div class="settings-row">
            <div class="row-left">
              <span class="row-icon">🔤</span>
              <div class="row-text">
                <span class="row-title">字体大小</span>
                <span class="row-sub">{{ fontSizeLabels[fontSize] }}</span>
              </div>
            </div>
            <div class="slider-wrap">
              <span class="slider-label">A</span>
              <input
                type="range"
                min="0"
                max="2"
                step="1"
                v-model="fontSize"
                class="font-slider"
              />
              <span class="slider-label" style="font-size:18px">A</span>
            </div>
          </div>

        </div>
      </div>

      <!-- ── AI 接口配置 ──────────────────── -->
      <div class="settings-section">
        <h2 class="section-label">AI 接口配置</h2>
        <div class="settings-group">

          <div class="settings-row" style="flex-direction:column;align-items:flex-start;gap:10px">
            <div class="row-left" style="width:100%">
              <span class="row-icon">🔑</span>
              <div class="row-text">
                <span class="row-title">RunPod API Key</span>
                <span class="row-sub">用于 AI 图像生成、换装、视频生成。<a href="https://runpod.io/console/user/settings" target="_blank" rel="noopener" style="color:#10a37f">获取 Key →</a></span>
              </div>
            </div>
            <div class="api-key-input-row">
              <input
                :type="showApiKey ? 'text' : 'password'"
                v-model="runpodKeyInput"
                class="field-input api-key-input"
                placeholder="xxxxxxxxxx...（不会上传到服务器，仅存本地）"
                autocomplete="off"
                spellcheck="false"
              />
              <button class="key-toggle-btn" @click="showApiKey = !showApiKey" :title="showApiKey ? '隐藏' : '显示'">
                {{ showApiKey ? '🙈' : '👁️' }}
              </button>
              <button class="key-save-btn" @click="saveRunpodKey" :disabled="!runpodKeyInput.trim()">
                保存
              </button>
            </div>
            <p v-if="keyStatus" class="key-status" :class="keyStatusType">{{ keyStatus }}</p>
          </div>

          <div class="settings-divider"></div>

          <div class="settings-row">
            <div class="row-left">
              <span class="row-icon">🤖</span>
              <div class="row-text">
                <span class="row-title">可用模型</span>
                <span class="row-sub">16 个端点 · 图像生成 / 图像编辑 / 图生视频</span>
              </div>
            </div>
            <button class="edit-btn" @click="router.push('/image-library')">去生成</button>
          </div>

          <div class="settings-divider"></div>

          <!-- ComfyUI Pod 地址 -->
          <div class="settings-row" style="flex-direction:column;align-items:flex-start;gap:10px">
            <div class="row-left" style="width:100%">
              <span class="row-icon">🖥️</span>
              <div class="row-text">
                <span class="row-title">ComfyUI Pod 地址</span>
                <span class="row-sub">你的 RunPod Pod 公网地址（内置 Auth，需同时填写 Key）</span>
              </div>
            </div>
            <input
              v-model="comfyUrlInput"
              class="field-input"
              placeholder="https://x1ik3wb0zvch3v-64410f01-8188.proxy.runpod.net"
              autocomplete="off"
              spellcheck="false"
              :disabled="comfyPinging"
            />
            <div class="api-key-input-row">
              <input
                :type="showComfyKey ? 'text' : 'password'"
                v-model="comfyKeyInput"
                class="field-input api-key-input"
                placeholder="ComfyUI 内置 Auth Key（--enable-api-access-token 对应的值）"
                autocomplete="off"
                spellcheck="false"
              />
              <button class="key-toggle-btn" @click="showComfyKey = !showComfyKey">{{ showComfyKey ? '🙈' : '👁️' }}</button>
              <button class="key-save-btn" @click="saveComfyConfig" :disabled="!comfyUrlInput.trim()">保存</button>
            </div>
            <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap">
              <button
                class="edit-btn"
                style="border-color:rgba(16,163,127,0.4);color:#10a37f"
                :disabled="!comfyUrlInput.trim() || comfyPinging"
                @click="pingComfy"
              >
                {{ comfyPinging ? '检测中...' : '🔗 测试连通性' }}
              </button>
              <button class="edit-btn" @click="router.push('/test-gen')">🚀 去测试出图</button>
            </div>
            <p v-if="comfyStatus" class="key-status" :class="comfyStatusType">{{ comfyStatus }}</p>
          </div>

        </div>
      </div>

      <!-- ── 账号 ──────────────────────── -->
      <div class="settings-section">
        <h2 class="section-label">账号</h2>
        <div class="settings-group">

          <div class="settings-row">
            <div class="row-left">
              <div class="avatar-circle">{{ userStore.avatarLetter }}</div>
              <div class="row-text">
                <span class="row-title">{{ userStore.displayName }}</span>
                <span class="row-sub">{{ userStore.profile?.email || 'designer@deepay.ai' }}</span>
              </div>
            </div>
            <button class="edit-btn" @click="showEditModal = true">编辑</button>
          </div>

          <div class="settings-divider"></div>

          <div class="settings-row">
            <div class="row-left">
              <span class="row-icon">👑</span>
              <div class="row-text">
                <span class="row-title">订阅计划</span>
                <span class="row-sub">Free Plan · 每日 3 次免费生成</span>
              </div>
            </div>
            <button class="upgrade-btn">升级 Pro</button>
          </div>

        </div>
      </div>

      <!-- ── 通知 ──────────────────────── -->
      <div class="settings-section">
        <h2 class="section-label">通知</h2>
        <div class="settings-group">

          <div class="settings-row">
            <div class="row-left">
              <span class="row-icon">🔔</span>
              <div class="row-text">
                <span class="row-title">推送通知</span>
                <span class="row-sub">接收设计完成、销售提醒等通知</span>
              </div>
            </div>
            <button
              class="toggle-switch"
              :class="{ on: pushNotif }"
              @click="pushNotif = !pushNotif"
            >
              <span class="toggle-thumb"></span>
            </button>
          </div>

          <div class="settings-divider"></div>

          <div class="settings-row">
            <div class="row-left">
              <span class="row-icon">📧</span>
              <div class="row-text">
                <span class="row-title">邮件通知</span>
                <span class="row-sub">接收每周数据报告和营销建议</span>
              </div>
            </div>
            <button
              class="toggle-switch"
              :class="{ on: emailNotif }"
              @click="emailNotif = !emailNotif"
            >
              <span class="toggle-thumb"></span>
            </button>
          </div>

        </div>
      </div>

      <!-- ── 数据 ──────────────────────── -->
      <div class="settings-section">
        <h2 class="section-label">数据</h2>
        <div class="settings-group">

          <div class="settings-row clickable" @click="exportChats">
            <div class="row-left">
              <span class="row-icon">📥</span>
              <div class="row-text">
                <span class="row-title">导出对话记录</span>
                <span class="row-sub">导出所有对话为 JSON 文件</span>
              </div>
            </div>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color:var(--gpt-text-muted)"><path d="M9 18l6-6-6-6"/></svg>
          </div>

          <div class="settings-divider"></div>

          <div class="settings-row clickable" @click="showClearConfirm = true">
            <div class="row-left">
              <span class="row-icon">🗑️</span>
              <div class="row-text">
                <span class="row-title danger-text">清空对话记录</span>
                <span class="row-sub">此操作不可撤销</span>
              </div>
            </div>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color:var(--gpt-text-muted)"><path d="M9 18l6-6-6-6"/></svg>
          </div>

        </div>
      </div>

      <!-- ── 关于 ──────────────────────── -->
      <div class="settings-section">
        <h2 class="section-label">关于</h2>
        <div class="settings-group">

          <div class="settings-row">
            <div class="row-left">
              <span class="row-icon">📱</span>
              <div class="row-text">
                <span class="row-title">版本</span>
                <span class="row-sub">Deepay PWA</span>
              </div>
            </div>
            <span class="version-badge">v1.0.0</span>
          </div>

          <div class="settings-divider"></div>

          <div class="settings-row clickable">
            <div class="row-left">
              <span class="row-icon">💬</span>
              <div class="row-text">
                <span class="row-title">意见反馈</span>
                <span class="row-sub">帮助我们改进产品</span>
              </div>
            </div>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color:var(--gpt-text-muted)"><path d="M9 18l6-6-6-6"/></svg>
          </div>

          <div class="settings-divider"></div>

          <div class="settings-row clickable">
            <div class="row-left">
              <span class="row-icon">🔒</span>
              <div class="row-text">
                <span class="row-title">隐私政策</span>
                <span class="row-sub">了解我们如何保护你的数据</span>
              </div>
            </div>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color:var(--gpt-text-muted)"><path d="M9 18l6-6-6-6"/></svg>
          </div>

        </div>
      </div>

      <!-- ── 快速导航 ──────────────────────── -->
      <div class="settings-section">
        <h2 class="section-label">快速导航</h2>
        <div class="quick-nav-grid">
          <button class="qng-btn" @click="router.push('/')">
            <span class="qng-icon">💬</span>
            <span class="qng-label">AI 对话</span>
          </button>
          <button class="qng-btn" @click="router.push('/image-library')">
            <span class="qng-icon">🖼️</span>
            <span class="qng-label">图库</span>
          </button>
          <button class="qng-btn" @click="router.push('/ai-sales')">
            <span class="qng-icon">🏪</span>
            <span class="qng-label">AI 开店</span>
          </button>
          <button class="qng-btn" @click="router.push('/template-library')">
            <span class="qng-icon">📋</span>
            <span class="qng-label">模板库</span>
          </button>
        </div>
      </div>

      <!-- Logout -->
      <button class="logout-btn" @click="showLogoutConfirm = true">退出登录</button>

    </div>

    <!-- Clear confirm dialog -->
    <div v-if="showClearConfirm" class="dialog-backdrop" @click.self="showClearConfirm = false">
      <div class="dialog">
        <h3 class="dialog-title">确认清空？</h3>
        <p class="dialog-body">所有对话记录将被永久删除，此操作无法撤销。</p>
        <div class="dialog-actions">
          <button class="dialog-cancel" @click="showClearConfirm = false">取消</button>
          <button class="dialog-danger" @click="clearChats">清空</button>
        </div>
      </div>
    </div>

    <!-- Logout confirm dialog -->
    <div v-if="showLogoutConfirm" class="dialog-backdrop" @click.self="showLogoutConfirm = false">
      <div class="dialog">
        <h3 class="dialog-title">退出登录</h3>
        <p class="dialog-body">确认退出当前账号？</p>
        <div class="dialog-actions">
          <button class="dialog-cancel" @click="showLogoutConfirm = false">取消</button>
          <button class="dialog-danger" @click="logout">退出</button>
        </div>
      </div>
    </div>

    <!-- Edit profile modal -->
    <div v-if="showEditModal" class="dialog-backdrop" @click.self="showEditModal = false">
      <div class="dialog">
        <h3 class="dialog-title">编辑资料</h3>
        <div class="edit-fields">
          <label class="field-label">昵称</label>
          <input v-model="editName" class="field-input" placeholder="输入昵称" />
        </div>
        <div class="dialog-actions">
          <button class="dialog-cancel" @click="showEditModal = false">取消</button>
          <button class="dialog-confirm" @click="saveProfile">保存</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useThemeStore, useUserStore, useChatStore, useImageGenStore, useComfyStore } from '@/store/index.js'
import { pingComfy as pingComfyApi } from '@/api/comfyui.js'

const router     = useRouter()
const themeStore = useThemeStore()
const userStore  = useUserStore()
const chatStore  = useChatStore()
const genStore   = useImageGenStore()
const comfyStore = useComfyStore()

const fontSize = ref(1)
const fontSizeLabels = ['小', '中（默认）', '大']
const pushNotif = ref(true)
const emailNotif = ref(false)
const showClearConfirm = ref(false)
const showLogoutConfirm = ref(false)
const showEditModal = ref(false)
const editName = ref(userStore.displayName)

// ── RunPod Serverless API Key ──────────────────────────
const runpodKeyInput = ref(genStore.apiKey)
const showApiKey     = ref(false)
const keyStatus      = ref('')
const keyStatusType  = ref('success')

function saveRunpodKey() {
  const k = runpodKeyInput.value.trim()
  if (!k) return
  genStore.setApiKey(k)
  keyStatus.value = '✓ API Key 已保存到本地'
  keyStatusType.value = 'success'
  setTimeout(() => { keyStatus.value = '' }, 3000)
}

// ── ComfyUI Pod 配置 ───────────────────────────────────
const comfyUrlInput  = ref(comfyStore.podUrl)
const comfyKeyInput  = ref(comfyStore.apiKey)
const showComfyKey   = ref(false)
const comfyStatus    = ref('')
const comfyStatusType = ref('success')
const comfyPinging   = ref(false)

function saveComfyConfig() {
  comfyStore.setPodUrl(comfyUrlInput.value)
  comfyStore.setApiKey(comfyKeyInput.value)
  comfyStatus.value = '✓ ComfyUI 配置已保存'
  comfyStatusType.value = 'success'
  setTimeout(() => { comfyStatus.value = '' }, 3000)
}

async function pingComfy() {
  const url = comfyUrlInput.value.trim()
  if (!url) return
  comfyPinging.value = true
  comfyStatus.value  = '正在连接...'
  comfyStatusType.value = 'success'
  try {
    const result = await pingComfyApi(url, comfyKeyInput.value)
    if (result.ok) {
      comfyStore.setPingResult(true, result.version)
      comfyStatus.value = `✅ 连通正常 · ComfyUI ${result.version}`
      comfyStatusType.value = 'success'
    } else {
      comfyStore.setPingResult(false, result.error)
      comfyStatus.value = `❌ ${result.error}`
      comfyStatusType.value = 'error'
    }
  } catch (e) {
    comfyStatus.value = `❌ ${e.message}`
    comfyStatusType.value = 'error'
  } finally {
    comfyPinging.value = false
    setTimeout(() => { comfyStatus.value = '' }, 6000)
  }
}

// ── Profile & Misc ────────────────────────────────────
function clearChats() {
  chatStore.clearSessions()
  showClearConfirm.value = false
}

function exportChats() {
  const data = JSON.stringify(chatStore.sessions, null, 2)
  const blob = new Blob([data], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'deepay-chats.json'
  a.click()
  URL.revokeObjectURL(url)
}

function saveProfile() {
  if (editName.value.trim()) {
    const profile = { ...(userStore.profile || {}), nickname: editName.value.trim() }
    userStore.setProfile(profile)
  }
  showEditModal.value = false
}

function logout() {
  userStore.logout()
  showLogoutConfirm.value = false
  router.push('/')
}
</script>

<style scoped>
.settings-page {
  min-height: 100%;
  background: var(--gpt-main);
  padding: 24px 20px 60px;
}
.settings-inner {
  max-width: 620px;
  margin: 0 auto;
}
.page-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--gpt-text);
  margin: 0 0 28px;
  letter-spacing: -0.02em;
}

/* Section */
.settings-section { margin-bottom: 28px; }
.section-label {
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: var(--gpt-text-muted);
  margin: 0 0 8px 4px;
}

.settings-group {
  background: var(--dp-card);
  border: 1px solid var(--gpt-border);
  border-radius: 14px;
  overflow: hidden;
}

.settings-row {
  display: flex;
  align-items: center;
  padding: 14px 18px;
  gap: 12px;
}
.settings-row.clickable { cursor: pointer; transition: background 0.15s; }
.settings-row.clickable:hover { background: var(--gpt-sidebar-hover); }

.settings-divider {
  height: 1px;
  background: var(--gpt-border);
  margin: 0 18px;
}

.row-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.row-icon {
  font-size: 20px;
  width: 32px;
  text-align: center;
  flex-shrink: 0;
}

.avatar-circle {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #10a37f, #0d8b6e);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
}

.row-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.row-title {
  font-size: 14.5px;
  font-weight: 500;
  color: var(--gpt-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.row-sub {
  font-size: 12px;
  color: var(--gpt-text-sub);
}
.danger-text { color: #ef4444; }

/* Toggle switch */
.toggle-switch {
  width: 46px;
  height: 26px;
  background: var(--gpt-input-bg);
  border: 2px solid var(--gpt-border);
  border-radius: 13px;
  cursor: pointer;
  position: relative;
  transition: background 0.25s, border-color 0.25s;
  flex-shrink: 0;
}
.toggle-switch.on {
  background: #10a37f;
  border-color: #10a37f;
}
.toggle-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 18px;
  height: 18px;
  background: white;
  border-radius: 50%;
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
}
.toggle-switch.on .toggle-thumb { transform: translateX(20px); }

/* Slider */
.slider-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}
.slider-label {
  font-size: 12px;
  color: var(--gpt-text-muted);
  user-select: none;
}
.font-slider {
  width: 100px;
  accent-color: #10a37f;
  cursor: pointer;
}

/* Edit / Upgrade buttons */
.edit-btn {
  padding: 6px 14px;
  background: transparent;
  border: 1px solid var(--gpt-border);
  border-radius: 8px;
  color: var(--gpt-text-sub);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
  flex-shrink: 0;
}
.edit-btn:hover { border-color: #10a37f; color: #10a37f; }

.upgrade-btn {
  padding: 6px 14px;
  background: linear-gradient(135deg, #10a37f, #0d8b6e);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  flex-shrink: 0;
  transition: opacity 0.15s, transform 0.15s;
}
.upgrade-btn:hover { opacity: 0.9; transform: scale(1.02); }

.version-badge {
  font-size: 12px;
  color: var(--gpt-text-muted);
  background: var(--gpt-input-bg);
  padding: 4px 10px;
  border-radius: 6px;
}

/* Quick Nav Grid */
.quick-nav-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}
@media (max-width: 480px) { .quick-nav-grid { grid-template-columns: repeat(2, 1fr); } }
.qng-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 8px;
  background: var(--gpt-input-bg);
  border: 1px solid var(--gpt-border);
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.34,1.56,0.64,1);
}
.qng-btn:hover {
  border-color: #10a37f;
  background: rgba(16,163,127,0.08);
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(16,163,127,0.12);
}
.qng-icon  { font-size: 22px; }
.qng-label { font-size: 12px; font-weight: 600; color: var(--gpt-text); }

/* Logout */
.logout-btn {
  width: 100%;
  padding: 14px;
  background: transparent;
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 12px;
  color: #ef4444;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  margin-top: 8px;
  transition: background 0.15s, border-color 0.15s;
}
.logout-btn:hover {
  background: rgba(239, 68, 68, 0.06);
  border-color: #ef4444;
}

/* Dialogs */
.dialog-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 24px;
}
.dialog {
  background: var(--gpt-input-bg);
  border: 1px solid var(--gpt-border);
  border-radius: 16px;
  padding: 24px;
  width: 100%;
  max-width: 380px;
}
.dialog-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--gpt-text);
  margin: 0 0 8px;
}
.dialog-body {
  font-size: 14px;
  color: var(--gpt-text-sub);
  margin: 0 0 20px;
  line-height: 1.5;
}
.dialog-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
.dialog-cancel {
  padding: 9px 18px;
  background: transparent;
  border: 1px solid var(--gpt-border);
  border-radius: 8px;
  color: var(--gpt-text-sub);
  font-size: 14px;
  cursor: pointer;
  transition: background 0.15s;
}
.dialog-cancel:hover { background: var(--gpt-sidebar-hover); }
.dialog-danger {
  padding: 9px 18px;
  background: #ef4444;
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}
.dialog-danger:hover { background: #dc2626; }
.dialog-confirm {
  padding: 9px 18px;
  background: #10a37f;
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}
.dialog-confirm:hover { background: #0d8b6e; }

/* Edit fields */
.edit-fields { margin-bottom: 16px; }
.field-label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: var(--gpt-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 6px;
}
.field-input {
  width: 100%;
  background: var(--gpt-main);
  border: 1px solid var(--gpt-border);
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 14px;
  color: var(--gpt-text);
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
}
.field-input:focus { border-color: #10a37f; }
.field-input::placeholder { color: var(--gpt-text-muted); }

/* API Key input row */
.api-key-input-row {
  display: flex;
  gap: 8px;
  width: 100%;
}
.api-key-input {
  flex: 1;
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 13px;
  letter-spacing: 0.04em;
}
.key-toggle-btn {
  padding: 8px 10px;
  background: var(--gpt-input-bg);
  border: 1px solid var(--gpt-border);
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.15s;
  flex-shrink: 0;
}
.key-toggle-btn:hover { background: var(--gpt-sidebar-hover); }
.key-save-btn {
  padding: 8px 16px;
  background: #10a37f;
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.15s;
}
.key-save-btn:hover:not(:disabled) { background: #0d8b6e; }
.key-save-btn:disabled { opacity: 0.45; cursor: not-allowed; }
.key-status {
  font-size: 12px;
  margin: 0;
  padding: 0;
}
.key-status.success { color: #10a37f; }
.key-status.error   { color: #ef4444; }

@media (max-width: 640px) {
  .settings-page { padding: 16px 16px 60px; }
}
</style>
