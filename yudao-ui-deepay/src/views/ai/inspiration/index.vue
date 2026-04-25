<!--
  views/ai/inspiration/index.vue — 灵感库管理页（admin）
  路径：/admin/inspiration  或  /ai/inspiration

  ✔ 调用 /api/inspiration 获取数据（兜底本地数据，100%不白屏）
  ✔ 搜索 + 多维度筛选（风格/来源/季节）
  ✔ 瀑布流卡片：趋势分、标签、来源
  ✔ 多选 → 底部浮条 → 跳转改款/出款
  ✔ 控制台打印 API 响应，方便调试
-->
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/utils/request'

const router = useRouter()

// ── 数据 ────────────────────────────────────────────────────────────
const allItems   = ref([])
const loading    = ref(true)
const apiOk      = ref(false)

// 本地兜底数据（后端没跑时 100% 可显示）
const SEED = [
  { id: 1,  name: 'Paris FW — 极简大衣',    imageUrl: 'https://picsum.photos/300/420?random=11', source: '时装周', style: '极简', season: 'AW 2024', trendScore: 91, tags: ['minimal','coat'],    brand: 'Paris FW'   },
  { id: 2,  name: 'Milan FW — 廓形礼服',    imageUrl: 'https://picsum.photos/300/500?random=12', source: '时装周', style: '奢华', season: 'SS 2024', trendScore: 88, tags: ['luxury','gown'],    brand: 'Milan FW'   },
  { id: 3,  name: 'NYFM — 街头套装',        imageUrl: 'https://picsum.photos/300/390?random=13', source: '时装周', style: '街头', season: 'SS 2025', trendScore: 85, tags: ['street','set'],     brand: 'New York FW'},
  { id: 4,  name: 'COS — 极简连衣裙',       imageUrl: 'https://picsum.photos/300/440?random=14', source: '品牌',   style: '极简', season: 'SS 2024', trendScore: 80, tags: ['minimal','dress'],   brand: 'COS'        },
  { id: 5,  name: 'Acne — 标志性大衣',      imageUrl: 'https://picsum.photos/300/460?random=15', source: '品牌',   style: '极简', season: 'AW 2024', trendScore: 87, tags: ['minimal','coat'],    brand: 'Acne'       },
  { id: 6,  name: 'ARKET — 北欧针织',       imageUrl: 'https://picsum.photos/300/410?random=16', source: '品牌',   style: '极简', season: 'AW 2024', trendScore: 78, tags: ['nordic','knit'],     brand: 'ARKET'      },
  { id: 7,  name: 'SSENSE — 设计师大衣',    imageUrl: 'https://picsum.photos/300/480?random=17', source: '精选',   style: '奢华', season: 'AW 2024', trendScore: 84, tags: ['luxury','coat'],     brand: 'SSENSE'     },
  { id: 8,  name: 'Farfetch — 精品西装',    imageUrl: 'https://picsum.photos/300/430?random=18', source: '精选',   style: '奢华', season: 'SS 2025', trendScore: 82, tags: ['luxury','suit'],     brand: 'Farfetch'   },
  { id: 9,  name: 'ZARA — 时尚廓形大衣',    imageUrl: 'https://picsum.photos/300/400?random=19', source: '品牌',   style: '潮流', season: 'AW 2024', trendScore: 76, tags: ['trendy','coat'],     brand: 'ZARA'       },
  { id: 10, name: 'Tokyo FW — 解构夹克',    imageUrl: 'https://picsum.photos/300/450?random=20', source: '时装周', style: '前卫', season: 'SS 2025', trendScore: 93, tags: ['avant','jacket'],    brand: 'Tokyo FW'   },
]

async function loadData() {
  loading.value = true
  try {
    const res = await http.get('/api/inspiration/images')
    const data = Array.isArray(res) ? res : (res?.list ?? res?.data ?? [])
    if (data.length) {
      allItems.value = data
      apiOk.value = true
      console.log('[灵感库] API 返回:', data)
    } else {
      allItems.value = SEED
      console.warn('[灵感库] API 返回空，使用本地数据')
    }
  } catch (e) {
    allItems.value = SEED
    console.warn('[灵感库] 接口失败，使用本地兜底数据', e?.message)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

// ── 筛选状态 ─────────────────────────────────────────────────────────
const searchQ     = ref('')
const filterStyle  = ref('')
const filterSource = ref('')
const filterSeason = ref('')

const STYLES  = ['', '极简', '奢华', '潮流', '街头', '前卫']
const SOURCES = ['', '时装周', '品牌', '精选']

const seasons = computed(() => {
  const s = new Set(allItems.value.map(i => i.season).filter(Boolean))
  return ['', ...s]
})

const filtered = computed(() => {
  let list = allItems.value
  const q = searchQ.value.trim().toLowerCase()
  if (q)             list = list.filter(i => [i.name, i.brand, i.style, i.tags?.join(' ')].join(' ').toLowerCase().includes(q))
  if (filterStyle.value)  list = list.filter(i => i.style  === filterStyle.value)
  if (filterSource.value) list = list.filter(i => i.source === filterSource.value)
  if (filterSeason.value) list = list.filter(i => i.season === filterSeason.value)
  return list
})

// ── 多选 ─────────────────────────────────────────────────────────────
const selectedIds = ref(new Set())
const isSelected  = id => selectedIds.value.has(id)
const selectedItems = computed(() => filtered.value.filter(i => selectedIds.value.has(i.id)))

function toggle(item) {
  const s = new Set(selectedIds.value)
  if (s.has(item.id)) { s.delete(item.id) }
  else if (s.size < 5) { s.add(item.id) }
  selectedIds.value = s
}
function clearSel() { selectedIds.value = new Set() }

// ── 动作 ─────────────────────────────────────────────────────────────
function goRedesign() {
  const urls = selectedItems.value.map(i => encodeURIComponent(i.imageUrl)).join(',')
  router.push(`/redesign?refs=${urls}`)
}
function goGenerate() {
  const urls = selectedItems.value.map(i => encodeURIComponent(i.imageUrl)).join(',')
  router.push(`/ai/design?refs=${urls}`)
}

// ── 色彩工具 ─────────────────────────────────────────────────────────
function scoreColor(n) {
  if (n >= 90) return '#1abc9c'
  if (n >= 80) return '#F59E0B'
  return '#9CA3AF'
}
</script>

<template>
  <div class="page">

    <!-- 页头 -->
    <div class="header">
      <div>
        <h1 class="title">🎭 灵感库</h1>
        <p class="sub">
          {{ apiOk ? '✅ 数据来自后端 /api/inspiration/images' : '📦 本地种子数据（后端未连接）' }}
          · 共 {{ filtered.length }} 件
        </p>
      </div>
      <button
        v-if="selectedIds.size"
        class="btn-clear"
        @click="clearSel"
      >清除选择 ({{ selectedIds.size }})</button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <!-- 搜索 -->
      <div class="search-wrap">
        <span class="search-icon">🔍</span>
        <input
          v-model="searchQ"
          type="search"
          placeholder="搜索品牌、风格、款式…"
          class="search-input"
        />
      </div>

      <!-- 风格 -->
      <div class="chips">
        <button
          v-for="s in STYLES" :key="s"
          :class="['chip', filterStyle === s && 'chip-active']"
          @click="filterStyle = s"
        >{{ s || '全部风格' }}</button>
      </div>

      <!-- 来源 -->
      <div class="chips">
        <button
          v-for="src in SOURCES" :key="src"
          :class="['chip', filterSource === src && 'chip-active-purple']"
          @click="filterSource = src"
        >{{ src || '全部来源' }}</button>
      </div>

      <!-- 季节 -->
      <select v-model="filterSeason" class="sel-season">
        <option v-for="s in seasons" :key="s" :value="s">{{ s || '全部季节' }}</option>
      </select>
    </div>

    <!-- loading -->
    <div v-if="loading" class="loading">
      <span class="spin">⏳</span> 加载灵感数据中…
    </div>

    <!-- 瀑布流 -->
    <div v-else-if="filtered.length" class="masonry">
      <div
        v-for="item in filtered"
        :key="item.id"
        :class="['card', isSelected(item.id) && 'card-sel']"
        @click="toggle(item)"
      >
        <!-- 图片 -->
        <div class="img-wrap">
          <img :src="item.imageUrl || item.image" :alt="item.name" loading="lazy" class="img" />
          <div class="img-overlay" />

          <!-- 趋势分 -->
          <span
            class="score-badge"
            :style="{ color: scoreColor(item.trendScore ?? item.score ?? 75) }"
          >{{ item.trendScore ?? item.score ?? '—' }}</span>

          <!-- 来源标签 -->
          <span class="source-badge">{{ item.source ?? '' }}</span>

          <!-- 选中遮罩 -->
          <div v-if="isSelected(item.id)" class="check-mask">
            <span class="check-circle">✓</span>
          </div>
        </div>

        <!-- 卡片信息 -->
        <div class="card-info">
          <div class="card-name">{{ item.name }}</div>
          <div class="card-meta">{{ item.brand ?? '' }} {{ item.season ? '· ' + item.season : '' }}</div>

          <!-- 趋势进度条 -->
          <div class="trend-bar-wrap">
            <div
              class="trend-bar"
              :style="{
                width: (item.trendScore ?? item.score ?? 75) + '%',
                background: scoreColor(item.trendScore ?? item.score ?? 75),
              }"
            />
          </div>

          <!-- 标签 -->
          <div class="tags">
            <span v-for="tag in (item.tags ?? [])" :key="tag" class="tag">{{ tag }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty">
      <p>🔍</p>
      <p class="empty-label">没有符合条件的灵感</p>
      <button class="btn-reset" @click="searchQ='';filterStyle='';filterSource='';filterSeason=''">
        清空筛选
      </button>
    </div>

    <!-- 底部浮条 -->
    <transition name="slide-up">
      <div v-if="selectedIds.size" class="float-bar">
        <span class="float-info">
          已选 <strong>{{ selectedIds.size }}</strong> / 5 张
        </span>
        <div class="float-actions">
          <button class="btn-redesign" @click="goRedesign">🔧 改款</button>
          <button class="btn-generate" @click="goGenerate">🎯 出款</button>
        </div>
      </div>
    </transition>

  </div>
</template>

<style scoped>
.page  { padding-bottom: 100px; }
.header {
  display: flex; align-items: flex-start; justify-content: space-between;
  margin-bottom: 20px;
}
.title { font-size: 22px; font-weight: 800; color: #fff; margin: 0 0 4px; }
.sub   { font-size: 12px; color: #444; margin: 0; }
.btn-clear {
  padding: 7px 16px; border-radius: 8px;
  border: 1px solid #333; background: transparent; color: #888; font-size: 12px; cursor: pointer;
}
.btn-clear:hover { color: #fff; border-color: #555; }

/* 筛选栏 */
.filter-bar { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 20px; align-items: center; }
.search-wrap {
  position: relative; flex: 1; min-width: 180px;
}
.search-icon { position: absolute; left: 10px; top: 50%; transform: translateY(-50%); font-size: 13px; }
.search-input {
  width: 100%; padding: 8px 12px 8px 32px;
  background: #111; border: 1px solid #222; border-radius: 10px;
  color: #fff; font-size: 13px;
}
.search-input:focus { outline: none; border-color: #333; }
.chips { display: flex; gap: 6px; flex-wrap: wrap; }
.chip {
  padding: 5px 12px; border-radius: 20px; border: 1px solid #222;
  background: transparent; color: #555; font-size: 12px; cursor: pointer; white-space: nowrap;
}
.chip:hover { border-color: #444; color: #aaa; }
.chip-active { background: #0f2a1a; border-color: #1abc9c; color: #1abc9c; }
.chip-active-purple { background: rgba(26,188,156,0.08); border-color: #1abc9c; color: #1abc9c; }
.sel-season {
  padding: 6px 10px; background: #111; border: 1px solid #222;
  color: #666; border-radius: 8px; font-size: 12px; cursor: pointer;
}

/* Loading */
.loading { text-align: center; padding: 60px; color: #444; font-size: 14px; }
.spin { display: inline-block; animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* 瀑布流 */
.masonry {
  columns: 4 180px; gap: 14px;
}
@media (max-width: 900px) { .masonry { columns: 3 160px; } }
@media (max-width: 640px) { .masonry { columns: 2 140px; } }

.card {
  break-inside: avoid; margin-bottom: 14px;
  border-radius: 12px; overflow: hidden;
  background: #111; border: 2px solid transparent;
  cursor: pointer; transition: border-color 0.15s, transform 0.1s;
}
.card:hover { transform: translateY(-2px); }
.card-sel   { border-color: #1abc9c; }

.img-wrap { position: relative; }
.img      { width: 100%; display: block; object-fit: cover; }
.img-overlay {
  position: absolute; inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,.7) 0%, transparent 50%);
  pointer-events: none;
}
.score-badge {
  position: absolute; top: 8px; right: 8px;
  background: rgba(0,0,0,.65); backdrop-filter: blur(4px);
  padding: 2px 7px; border-radius: 6px;
  font-size: 11px; font-weight: 800;
}
.source-badge {
  position: absolute; bottom: 8px; left: 8px;
  background: rgba(0,0,0,.6); backdrop-filter: blur(4px);
  padding: 2px 8px; border-radius: 6px;
  font-size: 10px; color: #aaa;
}
.check-mask {
  position: absolute; inset: 0;
  background: rgba(26,188,156,.12);
  display: flex; align-items: center; justify-content: center;
}
.check-circle {
  width: 36px; height: 36px; border-radius: 50%;
  background: #1abc9c; color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: 900;
}

.card-info { padding: 10px 12px 12px; }
.card-name { font-size: 12px; font-weight: 600; color: #ddd; margin-bottom: 2px; }
.card-meta { font-size: 11px; color: #444; margin-bottom: 6px; }
.trend-bar-wrap { height: 3px; background: #1a1a1a; border-radius: 2px; margin-bottom: 6px; }
.trend-bar      { height: 100%; border-radius: 2px; transition: width 0.6s ease; }
.tags { display: flex; flex-wrap: wrap; gap: 4px; }
.tag  {
  padding: 2px 7px; border-radius: 4px;
  background: #1a1a1a; color: #555; font-size: 10px;
}

/* 空状态 */
.empty { text-align: center; padding: 60px; }
.empty p:first-child { font-size: 40px; margin-bottom: 12px; }
.empty-label { color: #555; font-size: 14px; margin-bottom: 16px; }
.btn-reset {
  padding: 8px 20px; border-radius: 8px;
  border: 1px solid #333; background: transparent; color: #888; font-size: 13px; cursor: pointer;
}

/* 底部浮条 */
.float-bar {
  position: fixed; bottom: 24px; left: 50%; transform: translateX(-50%);
  background: #111; border: 1px solid #2a2a2a; border-radius: 20px;
  padding: 12px 20px; display: flex; align-items: center; gap: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,.7); z-index: 100; white-space: nowrap;
}
.float-info { font-size: 13px; color: #666; }
.float-info strong { color: #1abc9c; }
.float-actions { display: flex; gap: 8px; }
.btn-redesign, .btn-generate {
  padding: 8px 18px; border-radius: 10px;
  font-size: 13px; font-weight: 700; border: none; cursor: pointer;
}
.btn-redesign { background: #1a1a1a; color: #F59E0B; border: 1px solid #F59E0B44; }
.btn-generate { background: #1abc9c; color: #fff; }
.btn-redesign:hover, .btn-generate:hover { opacity: .85; }

.slide-up-enter-active, .slide-up-leave-active { transition: all .25s ease; }
.slide-up-enter-from, .slide-up-leave-to {
  opacity: 0; transform: translateX(-50%) translateY(20px);
}
</style>
