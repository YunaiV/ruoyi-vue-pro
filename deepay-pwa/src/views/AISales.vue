<template>
  <div class="sales-page">

    <!-- Header -->
    <div class="sales-header">
      <div>
        <h1 class="page-title">AI 销售助手</h1>
        <p class="page-sub">智能分析销售数据，优化运营策略</p>
      </div>
      <button class="refresh-btn">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 102.13-9.36L1 10"/></svg>
        刷新数据
      </button>
    </div>

    <!-- Stat cards -->
    <div class="stat-grid">
      <div
        v-for="(stat, i) in stats"
        :key="stat.label"
        class="stat-card"
        :class="{ visible: statsVisible }"
        :style="{ transitionDelay: (i * 80) + 'ms' }"
      >
        <div class="stat-icon" :style="{ background: stat.iconBg }">
          <span>{{ stat.icon }}</span>
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ stat.prefix }}{{ displayedStats[i] }}{{ stat.suffix }}</div>
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-change" :class="stat.trend > 0 ? 'up' : 'down'">
            {{ stat.trend > 0 ? '↑' : '↓' }} {{ Math.abs(stat.trend) }}% 较昨日
          </div>
        </div>
      </div>
    </div>

    <!-- Chart + Table row -->
    <div class="content-row">

      <!-- Line chart (SVG) -->
      <div class="chart-card">
        <div class="card-header">
          <h3 class="card-title">销售趋势</h3>
          <div class="period-tabs">
            <button
              v-for="p in periods"
              :key="p"
              class="period-tab"
              :class="{ active: activePeriod === p }"
              @click="activePeriod = p"
            >{{ p }}</button>
          </div>
        </div>
        <div class="chart-wrap">
          <svg class="line-chart" viewBox="0 0 600 180" preserveAspectRatio="none">
            <!-- Grid lines -->
            <line v-for="y in [36, 72, 108, 144]" :key="y" x1="0" :y1="y" x2="600" :y2="y" stroke="var(--gpt-border)" stroke-width="1"/>
            <!-- Area fill -->
            <path :d="areaPath" fill="rgba(16,163,127,0.08)" />
            <!-- Line -->
            <path :d="linePath" fill="none" stroke="#10a37f" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            <!-- Dots -->
            <circle
              v-for="(pt, i) in chartPoints"
              :key="i"
              :cx="pt.x"
              :cy="pt.y"
              r="4"
              fill="#10a37f"
              stroke="var(--gpt-main)"
              stroke-width="2"
            />
          </svg>
          <!-- X labels -->
          <div class="chart-labels">
            <span v-for="l in chartLabels" :key="l">{{ l }}</span>
          </div>
        </div>
      </div>

      <!-- Product table -->
      <div class="table-card">
        <div class="card-header">
          <h3 class="card-title">商品排行</h3>
        </div>
        <table class="product-table">
          <thead>
            <tr>
              <th>商品</th>
              <th>浏览</th>
              <th>销售</th>
              <th>转化率</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(p, i) in products" :key="p.name">
              <td>
                <div class="product-cell">
                  <span class="rank" :class="'rank-' + (i + 1)">{{ i + 1 }}</span>
                  <span class="product-name">{{ p.name }}</span>
                </div>
              </td>
              <td class="num-cell">{{ p.views.toLocaleString() }}</td>
              <td class="num-cell">{{ p.sales }}</td>
              <td>
                <div class="conv-cell">
                  <div class="conv-bar">
                    <div class="conv-fill" :style="{ width: p.conv + '%', background: convColor(p.conv) }"></div>
                  </div>
                  <span class="conv-text">{{ p.conv }}%</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- AI Recommendations -->
    <div class="rec-section">
      <h3 class="section-title">AI 推荐策略</h3>
      <div class="rec-grid">
        <div
          v-for="(rec, i) in recommendations"
          :key="rec.title"
          class="rec-card"
          :class="{ visible: statsVisible }"
          :style="{ transitionDelay: (400 + i * 100) + 'ms' }"
        >
          <div class="rec-accent"></div>
          <div class="rec-body">
            <div class="rec-tag">{{ rec.tag }}</div>
            <h4 class="rec-title">{{ rec.title }}</h4>
            <p class="rec-desc">{{ rec.desc }}</p>
            <button class="rec-btn">查看详情 →</button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const statsVisible = ref(false)
const activePeriod = ref('7天')
const periods = ['7天', '30天', '90天']

const stats = [
  { label: '今日销售', icon: '💰', iconBg: 'rgba(16,163,127,0.15)', prefix: '¥', suffix: '', rawValue: 12480, trend: 12.3 },
  { label: '转化率', icon: '📈', iconBg: 'rgba(99,102,241,0.15)', prefix: '', suffix: '%', rawValue: 86, trend: 2.1 },
  { label: '活跃用户', icon: '👥', iconBg: 'rgba(245,158,11,0.15)', prefix: '', suffix: '', rawValue: 1240, trend: -3.5 },
  { label: 'AI推荐转化', icon: '🤖', iconBg: 'rgba(239,68,68,0.15)', prefix: '', suffix: '%', rawValue: 34, trend: 8.7 },
]

const displayedStats = ref([0, 0, 0, 0])

const products = [
  { name: '春季宽松外套', views: 8420, sales: 312, conv: 13.2 },
  { name: '丹宁牛仔套装', views: 6180, sales: 248, conv: 10.8 },
  { name: '极简连衣裙', views: 5930, sales: 189, conv: 8.6 },
  { name: '运动休闲套装', views: 4760, sales: 143, conv: 7.4 },
  { name: '秋冬针织毛衣', views: 3890, sales: 97, conv: 5.2 },
]

const recommendations = [
  {
    tag: '📊 数据洞察',
    title: '周末促销窗口',
    desc: 'AI 分析显示周六下午 2-5 点转化率比平均高 34%。建议在此时段推出限时优惠，预计可提升销售额 18-25%。',
  },
  {
    tag: '🎯 精准营销',
    title: '复购用户激活',
    desc: '有 420 名 90 天未购买的用户，AI 模型预测通过专属优惠券触达，可激活其中 28% 的用户，带来约 ¥3.2万 增量收入。',
  },
]

const chartData = [42, 68, 55, 78, 92, 71, 88]
const chartLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const chartPoints = computed(() => {
  const maxVal = Math.max(...chartData)
  const minVal = Math.min(...chartData)
  const range = maxVal - minVal || 1
  return chartData.map((v, i) => ({
    x: 20 + (i / (chartData.length - 1)) * 560,
    y: 160 - ((v - minVal) / range) * 120,
  }))
})

const linePath = computed(() => {
  return chartPoints.value.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ')
})

const areaPath = computed(() => {
  const pts = chartPoints.value
  const line = pts.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ')
  return `${line} L ${pts[pts.length - 1].x} 165 L ${pts[0].x} 165 Z`
})

function convColor(val) {
  if (val >= 12) return '#10a37f'
  if (val >= 8) return '#6366f1'
  if (val >= 5) return '#f59e0b'
  return '#ef4444'
}

function animateCount(index, target, duration) {
  const start = Date.now()
  const tick = () => {
    const elapsed = Date.now() - start
    const progress = Math.min(elapsed / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    const val = Math.round(target * eased)
    displayedStats.value[index] = index === 1 || index === 3
      ? (val / 10).toFixed(1)
      : val.toLocaleString()
    if (progress < 1) requestAnimationFrame(tick)
  }
  tick()
}

onMounted(() => {
  setTimeout(() => {
    statsVisible.value = true
    stats.forEach((s, i) => {
      animateCount(i, s.rawValue, 1200)
    })
  }, 200)
})
</script>

<style scoped>
.sales-page {
  min-height: 100%;
  padding: 24px 28px 60px;
  background: var(--gpt-main);
  color: var(--gpt-text);
}

.sales-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 28px;
  flex-wrap: wrap;
  gap: 12px;
}
.page-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--gpt-text);
  margin: 0 0 6px;
  letter-spacing: -0.02em;
}
.page-sub {
  font-size: 14px;
  color: var(--gpt-text-sub);
  margin: 0;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 9px 16px;
  background: var(--gpt-input-bg);
  border: 1px solid var(--gpt-border);
  border-radius: 8px;
  color: var(--gpt-text-sub);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
}
.refresh-btn:hover { color: var(--gpt-text); border-color: #10a37f; }

/* Stats */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
@media (max-width: 900px) { .stat-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 480px) { .stat-grid { grid-template-columns: 1fr; } }

.stat-card {
  background: var(--dp-card);
  border: 1px solid var(--gpt-border);
  border-radius: 14px;
  padding: 18px;
  display: flex;
  gap: 14px;
  align-items: center;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s ease, transform 0.4s ease;
}
.stat-card.visible { opacity: 1; transform: translateY(0); }

.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}
.stat-body { flex: 1; min-width: 0; }
.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--gpt-text);
  letter-spacing: -0.02em;
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  color: var(--gpt-text-sub);
  margin: 3px 0 4px;
}
.stat-change {
  font-size: 11.5px;
  font-weight: 500;
}
.stat-change.up { color: #10a37f; }
.stat-change.down { color: #ef4444; }

/* Content row */
.content-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 28px;
}
@media (max-width: 800px) { .content-row { grid-template-columns: 1fr; } }

/* Chart card */
.chart-card,
.table-card {
  background: var(--dp-card);
  border: 1px solid var(--gpt-border);
  border-radius: 14px;
  padding: 20px;
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--gpt-text);
  margin: 0;
}

.period-tabs {
  display: flex;
  gap: 4px;
  background: var(--gpt-input-bg);
  border-radius: 8px;
  padding: 3px;
}
.period-tab {
  padding: 4px 10px;
  border: none;
  background: transparent;
  border-radius: 6px;
  font-size: 12px;
  color: var(--gpt-text-sub);
  cursor: pointer;
  transition: all 0.15s;
}
.period-tab.active { background: var(--gpt-main); color: var(--gpt-text); }

.chart-wrap { overflow: hidden; }
.line-chart { width: 100%; height: 180px; display: block; }
.chart-labels {
  display: flex;
  justify-content: space-between;
  padding: 6px 10px 0;
}
.chart-labels span { font-size: 11px; color: var(--gpt-text-muted); }

/* Table */
.product-table {
  width: 100%;
  border-collapse: collapse;
}
.product-table th {
  text-align: left;
  font-size: 11px;
  font-weight: 600;
  color: var(--gpt-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 0 0 10px;
  border-bottom: 1px solid var(--gpt-border);
}
.product-table td {
  padding: 11px 0;
  font-size: 13.5px;
  color: var(--gpt-text);
  border-bottom: 1px solid var(--gpt-border);
}
.product-table tr:last-child td { border-bottom: none; }

.product-cell { display: flex; align-items: center; gap: 8px; }
.rank {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: var(--gpt-input-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  color: var(--gpt-text-sub);
  flex-shrink: 0;
}
.rank-1 { background: rgba(245,158,11,0.2); color: #f59e0b; }
.rank-2 { background: rgba(148,163,184,0.2); color: #94a3b8; }
.rank-3 { background: rgba(180,120,74,0.2); color: #b4783a; }

.product-name {
  font-size: 13px;
  color: var(--gpt-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 120px;
}
.num-cell {
  color: var(--gpt-text-sub);
  font-size: 13px;
  font-variant-numeric: tabular-nums;
}
.conv-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.conv-bar {
  width: 60px;
  height: 6px;
  background: var(--gpt-input-bg);
  border-radius: 3px;
  overflow: hidden;
  flex-shrink: 0;
}
.conv-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 1s ease;
}
.conv-text { font-size: 12px; color: var(--gpt-text-sub); }

/* Recommendations */
.section-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--gpt-text);
  margin: 0 0 16px;
}
.rec-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
@media (max-width: 640px) { .rec-grid { grid-template-columns: 1fr; } }

.rec-card {
  background: var(--dp-card);
  border: 1px solid var(--gpt-border);
  border-radius: 14px;
  overflow: hidden;
  display: flex;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s ease, transform 0.4s ease;
}
.rec-card.visible { opacity: 1; transform: translateY(0); }
.rec-card:hover { border-color: #10a37f; }

.rec-accent {
  width: 4px;
  background: linear-gradient(180deg, #10a37f, #0d8b6e);
  flex-shrink: 0;
}
.rec-body { padding: 18px 20px; flex: 1; }
.rec-tag {
  font-size: 12px;
  color: #10a37f;
  font-weight: 600;
  margin-bottom: 8px;
}
.rec-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--gpt-text);
  margin: 0 0 8px;
}
.rec-desc {
  font-size: 13.5px;
  color: var(--gpt-text-sub);
  line-height: 1.6;
  margin: 0 0 14px;
}
.rec-btn {
  background: transparent;
  border: none;
  color: #10a37f;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  padding: 0;
  transition: opacity 0.15s;
}
.rec-btn:hover { opacity: 0.8; }

@media (max-width: 640px) {
  .sales-page { padding: 16px 16px 60px; }
}
</style>
