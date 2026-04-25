import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { storage } from '@/utils/storage'

/* ── Theme Store ─────────────────────────────────── */
export const useThemeStore = defineStore('theme', () => {
  const saved  = storage.get('deepay_theme', null)
  const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches
  const isDark = ref(saved !== null ? saved === 'dark' : systemDark)

  function applyTheme() {
    document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
    document.documentElement.classList.toggle('dark', isDark.value)
    const meta = document.getElementById('theme-color-meta')
    if (meta) meta.content = isDark.value ? '#0a0a0a' : '#f2f2f7'
    storage.set('deepay_theme', isDark.value ? 'dark' : 'light')
  }

  function toggle() { isDark.value = !isDark.value; applyTheme() }
  function setDark(v) { isDark.value = v; applyTheme() }

  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
    if (!storage.get('deepay_theme')) setDark(e.matches)
  })

  applyTheme()
  return { isDark, toggle, setDark, applyTheme }
})

/* ── User Store ──────────────────────────────────── */
export const useUserStore = defineStore('user', () => {
  const token    = ref(storage.get('deepay_token', ''))
  const profile  = ref(storage.get('deepay_profile', null))
  const quota    = ref({ remainFree: 3, remainPaid: 0 })

  const isLoggedIn   = computed(() => !!token.value)
  const totalQuota   = computed(() => quota.value.remainFree + quota.value.remainPaid)
  const displayName  = computed(() => profile.value?.nickname || profile.value?.email?.split('@')[0] || '设计师')
  const avatarLetter = computed(() => displayName.value?.[0]?.toUpperCase() || 'D')

  function setToken(t)   { token.value = t; storage.set('deepay_token', t) }
  function setProfile(p) { profile.value = p; storage.set('deepay_profile', p) }
  function setQuota(q)   { quota.value = { ...quota.value, ...q } }
  function logout()      { token.value = ''; profile.value = null; storage.remove('deepay_token'); storage.remove('deepay_profile') }

  return { token, profile, quota, isLoggedIn, totalQuota, displayName, avatarLetter, setToken, setProfile, setQuota, logout }
})

/* ── Chat Store ──────────────────────────────────── */
export const useChatStore = defineStore('chat', () => {
  const sessions  = ref(storage.get('deepay_sessions', []))
  const activeId  = ref(null)
  const isOpen    = ref(false)

  const activeSession = computed(() => sessions.value.find(s => s.id === activeId.value))
  const messages      = computed(() => activeSession.value?.messages || [])

  function newSession() {
    const session = {
      id:       Date.now().toString(),
      title:    '新对话',
      messages: [{ id: 1, role: 'assistant', content: '你好！我是 Deepay AI 设计助手 ✨\n告诉我你想要什么款式，我来帮你出款！', time: new Date().toISOString() }],
      createdAt: new Date().toISOString(),
    }
    sessions.value.unshift(session)
    activeId.value = session.id
    save()
    return session
  }

  function addMessage(role, content) {
    if (!activeId.value) newSession()
    const msg = { id: Date.now(), role, content, time: new Date().toISOString() }
    activeSession.value.messages.push(msg)
    if (role === 'user' && activeSession.value.title === '新对话') {
      activeSession.value.title = content.slice(0, 20) + (content.length > 20 ? '…' : '')
    }
    save()
    return msg
  }

  function deleteSession(id) {
    sessions.value = sessions.value.filter(s => s.id !== id)
    if (activeId.value === id) activeId.value = sessions.value[0]?.id || null
    save()
  }

  function clearSessions() {
    sessions.value = []
    activeId.value = null
    save()
  }

  function save() { storage.set('deepay_sessions', sessions.value.slice(0, 50)) }

  return { sessions, activeId, isOpen, activeSession, messages, newSession, addMessage, deleteSession, clearSessions }
})

/* ── Image Generation Store ──────────────────────── */
export const useImageGenStore = defineStore('imageGen', () => {
  // RunPod API Key（localStorage 持久化）
  const apiKey = ref(storage.get('deepay_runpod_key', ''))

  // 当前生成任务状态
  const status   = ref('idle')   // idle | generating | done | error
  const progress = ref('')       // 进度文案
  const jobId    = ref('')       // 当前 jobId（用于取消）
  const modelId  = ref('flux-kontext')  // 当前选中模型

  // 生成历史（最多 50 张，持久化到 localStorage）
  const history = ref(storage.get('deepay_gen_history', []))

  const isGenerating = computed(() => status.value === 'generating')

  function setApiKey(k) {
    apiKey.value = k.trim()
    storage.set('deepay_runpod_key', apiKey.value)
  }

  function setModel(m) { modelId.value = m }

  function startJob(id) {
    status.value   = 'generating'
    progress.value = '正在提交任务...'
    jobId.value    = id || ''
  }

  function updateProgress(s, msg) {
    status.value   = s === 'COMPLETED' ? 'done' : s === 'FAILED' ? 'error' : 'generating'
    progress.value = msg
  }

  function addResult({ images, seed, prompt, model, params }) {
    const entries = images.map(src => ({
      id:        `gen-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
      src,
      prompt,
      model,
      seed,
      params,
      createdAt: new Date().toISOString(),
    }))
    history.value = [...entries, ...history.value].slice(0, 50)
    storage.set('deepay_gen_history', history.value)
    status.value   = 'done'
    progress.value = '生成完成 ✓'
  }

  function setError(msg) {
    status.value   = 'error'
    progress.value = msg
  }

  function reset() {
    status.value   = 'idle'
    progress.value = ''
    jobId.value    = ''
  }

  function clearHistory() {
    history.value = []
    storage.remove('deepay_gen_history')
  }

  return {
    apiKey, status, progress, jobId, modelId, history, isGenerating,
    setApiKey, setModel, startJob, updateProgress, addResult, setError, reset, clearHistory,
  }
})
