<template>
  <div class="admin-page">

    <!-- ── Top Nav ── -->
    <header class="admin-nav">
      <div class="nav-left">
        <div class="nav-logo">
          <span class="logo-dot"></span>
          Deepay <span class="logo-badge">管理后台</span>
        </div>
      </div>
      <div class="nav-right">
        <span class="nav-env">Production</span>
        <div class="nav-avatar" :title="userStore.displayName">{{ userStore.avatarLetter }}</div>
        <button class="nav-back-btn" @click="$router.push('/')" title="返回前台">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
          返回前台
        </button>
      </div>
    </header>

    <!-- ── Body ── -->
    <div class="admin-body">

      <!-- ── Sidebar ── -->
      <nav class="admin-sidebar">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          class="asb-btn"
          :class="{ active: activeTab === tab.id }"
          @click="activeTab = tab.id"
        >
          <span class="asb-icon" v-html="tab.icon"></span>
          <span class="asb-label">{{ tab.label }}</span>
          <span v-if="tab.badge" class="asb-badge">{{ tab.badge }}</span>
        </button>
      </nav>

      <!-- ── Main Area ── -->
      <main class="admin-main">

        <!-- ══ 概览 ══ -->
        <section v-if="activeTab === 'overview'" class="tab-content">
          <div class="section-head">
            <h2 class="section-title">数据概览</h2>
            <span class="section-sub">今日数据实时更新</span>
          </div>

          <!-- Stat Cards -->
          <div class="stat-grid">
            <div v-for="s in stats" :key="s.label" class="stat-card">
              <div class="sc-icon" :style="{ background: s.bg }">
                <span v-html="s.icon"></span>
              </div>
              <div class="sc-info">
                <div class="sc-num">{{ s.value }}</div>
                <div class="sc-label">{{ s.label }}</div>
              </div>
              <div class="sc-trend" :class="s.up ? 'up' : 'down'">
                <svg v-if="s.up" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>
                <svg v-else width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="23 18 13.5 8.5 8.5 13.5 1 6"/><polyline points="17 18 23 18 23 12"/></svg>
                {{ s.trend }}
              </div>
            </div>
          </div>

          <!-- Charts Row -->
          <div class="charts-row">
            <!-- Activity Chart (CSS bar chart) -->
            <div class="chart-card">
              <div class="chart-head">
                <span class="chart-title">近 7 日活跃用户</span>
                <span class="chart-legend"><span class="dot green"></span>活跃</span>
              </div>
              <div class="bar-chart">
                <div v-for="(d, i) in chartData" :key="i" class="bar-col">
                  <div class="bar-fill" :style="{ height: d.pct + '%' }"></div>
                  <span class="bar-lbl">{{ d.day }}</span>
                </div>
              </div>
            </div>

            <!-- Revenue Donut -->
            <div class="chart-card">
              <div class="chart-head">
                <span class="chart-title">收入来源分布</span>
              </div>
              <div class="donut-wrap">
                <svg viewBox="0 0 100 100" class="donut-svg">
                  <circle cx="50" cy="50" r="38" fill="none" stroke="var(--adm-surface2)" stroke-width="14"/>
                  <circle cx="50" cy="50" r="38" fill="none" stroke="#10a37f" stroke-width="14"
                    stroke-dasharray="95.8 143.7" stroke-dashoffset="0" stroke-linecap="round"/>
                  <circle cx="50" cy="50" r="38" fill="none" stroke="#3b82f6" stroke-width="14"
                    stroke-dasharray="35.9 203.6" stroke-dashoffset="-95.8" stroke-linecap="round"/>
                  <circle cx="50" cy="50" r="38" fill="none" stroke="#8b5cf6" stroke-width="14"
                    stroke-dasharray="11.95 227.6" stroke-dashoffset="-131.7" stroke-linecap="round"/>
                </svg>
                <div class="donut-center">
                  <span class="donut-num">¥{{ revenueTotal }}</span>
                  <span class="donut-sub">总收入</span>
                </div>
              </div>
              <div class="donut-legend">
                <div class="dl-item"><span class="dl-dot" style="background:#10a37f"></span>会员订阅 67%</div>
                <div class="dl-item"><span class="dl-dot" style="background:#3b82f6"></span>API 调用 25%</div>
                <div class="dl-item"><span class="dl-dot" style="background:#8b5cf6"></span>模板销售 8%</div>
              </div>
            </div>
          </div>

          <!-- Recent Activity -->
          <div class="table-card">
            <div class="table-head">
              <span class="table-title">最新动态</span>
            </div>
            <div class="activity-list">
              <div v-for="act in recentActivity" :key="act.id" class="act-row">
                <div class="act-dot" :class="act.type"></div>
                <div class="act-info">
                  <span class="act-text">{{ act.text }}</span>
                  <span class="act-time">{{ act.time }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- ══ 用户管理 ══ -->
        <section v-if="activeTab === 'users'" class="tab-content">
          <div class="section-head">
            <h2 class="section-title">用户管理</h2>
            <div class="head-actions">
              <div class="search-box">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
                <input v-model="userSearch" class="search-input" placeholder="搜索用户..." />
              </div>
              <button class="act-btn primary">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M12 5v14M5 12h14"/></svg>
                添加用户
              </button>
            </div>
          </div>

          <div class="table-card">
            <table class="data-table">
              <thead>
                <tr>
                  <th>用户</th>
                  <th>邮箱</th>
                  <th>计划</th>
                  <th>注册时间</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="u in filteredUsers" :key="u.id">
                  <td>
                    <div class="cell-user">
                      <div class="cell-avatar" :style="{ background: u.color }">{{ u.name[0] }}</div>
                      <span>{{ u.name }}</span>
                    </div>
                  </td>
                  <td class="cell-muted">{{ u.email }}</td>
                  <td><span class="plan-tag" :class="u.plan">{{ u.plan === 'pro' ? 'Pro' : 'Free' }}</span></td>
                  <td class="cell-muted">{{ u.joined }}</td>
                  <td><span class="status-dot" :class="u.status"></span> {{ u.status === 'active' ? '正常' : '已禁用' }}</td>
                  <td>
                    <div class="row-actions">
                      <button class="ra-btn" title="编辑">
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                      </button>
                      <button class="ra-btn danger" title="禁用">
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><path d="M4.93 4.93l14.14 14.14"/></svg>
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <!-- ══ 内容管理 ══ -->
        <section v-if="activeTab === 'content'" class="tab-content">
          <div class="section-head">
            <h2 class="section-title">内容管理</h2>
            <div class="head-actions">
              <div class="filter-tabs">
                <button v-for="f in contentFilters" :key="f" class="ftab" :class="{ active: contentFilter === f }" @click="contentFilter = f">{{ f }}</button>
              </div>
              <button class="act-btn primary">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M12 5v14M5 12h14"/></svg>
                上传内容
              </button>
            </div>
          </div>

          <div class="content-grid">
            <div v-for="item in filteredContent" :key="item.id" class="content-card">
              <div class="cc-preview" :style="{ background: item.gradient }">
                <span class="cc-emoji">{{ item.emoji }}</span>
                <span class="cc-type">{{ item.type }}</span>
              </div>
              <div class="cc-info">
                <span class="cc-name">{{ item.name }}</span>
                <span class="cc-meta">{{ item.uses }} 次使用 · {{ item.date }}</span>
                <div class="cc-actions">
                  <button class="ra-btn">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  </button>
                  <button class="ra-btn danger">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/></svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- ══ 订单管理 ══ -->
        <section v-if="activeTab === 'orders'" class="tab-content">
          <div class="section-head">
            <h2 class="section-title">订单管理</h2>
            <div class="head-actions">
              <div class="search-box">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
                <input v-model="orderSearch" class="search-input" placeholder="搜索订单号..." />
              </div>
              <button class="act-btn secondary">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                导出 CSV
              </button>
            </div>
          </div>

          <div class="table-card">
            <table class="data-table">
              <thead>
                <tr>
                  <th>订单号</th>
                  <th>用户</th>
                  <th>产品</th>
                  <th>金额</th>
                  <th>时间</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="o in filteredOrders" :key="o.id">
                  <td class="cell-mono">#{{ o.id }}</td>
                  <td>{{ o.user }}</td>
                  <td class="cell-muted">{{ o.product }}</td>
                  <td class="cell-amount">¥{{ o.amount }}</td>
                  <td class="cell-muted">{{ o.time }}</td>
                  <td>
                    <span class="order-status" :class="o.status">
                      {{ { paid: '已支付', pending: '待支付', refund: '已退款' }[o.status] }}
                    </span>
                  </td>
                  <td>
                    <button class="ra-btn">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <!-- ══ 系统设置 ══ -->
        <section v-if="activeTab === 'system'" class="tab-content">
          <div class="section-head">
            <h2 class="section-title">系统设置</h2>
          </div>

          <div class="sys-grid">
            <div class="sys-card">
              <h3 class="sys-card-title">API 配置</h3>
              <div class="sys-field">
                <label>AI 主模型</label>
                <input class="sys-input" value="deepseek-chat" />
              </div>
              <div class="sys-field">
                <label>API Endpoint</label>
                <input class="sys-input" value="https://api.deepseek.com/v1/chat/completions" />
              </div>
              <div class="sys-field">
                <label>API Key</label>
                <input class="sys-input" type="password" placeholder="sk-••••••••••••••••" />
              </div>
              <button class="act-btn primary" style="margin-top:16px">保存配置</button>
            </div>

            <div class="sys-card">
              <h3 class="sys-card-title">运行状态</h3>
              <div class="sys-status-list">
                <div class="ssl-row" v-for="svc in services" :key="svc.name">
                  <div class="ssl-left">
                    <span class="ssl-dot" :class="svc.ok ? 'green' : 'red'"></span>
                    <span class="ssl-name">{{ svc.name }}</span>
                  </div>
                  <span class="ssl-latency">{{ svc.latency }}</span>
                </div>
              </div>
            </div>

            <div class="sys-card">
              <h3 class="sys-card-title">数据库</h3>
              <div class="sys-field">
                <label>MySQL Host</label>
                <input class="sys-input" value="127.0.0.1:3306" readonly />
              </div>
              <div class="sys-field">
                <label>数据库名</label>
                <input class="sys-input" value="deepay" readonly />
              </div>
              <div class="sys-field">
                <label>Redis Host</label>
                <input class="sys-input" value="127.0.0.1:6379" readonly />
              </div>
              <div class="db-stats">
                <div class="db-stat"><span>当前连接</span><strong>12</strong></div>
                <div class="db-stat"><span>缓存命中</span><strong>98.4%</strong></div>
              </div>
            </div>

            <div class="sys-card">
              <h3 class="sys-card-title">安全设置</h3>
              <div class="sys-toggle-row">
                <div>
                  <div class="st-label">双因素认证</div>
                  <div class="st-sub">管理员账号强制开启</div>
                </div>
                <div class="toggle-switch on"><span class="toggle-thumb"></span></div>
              </div>
              <div class="sys-toggle-row">
                <div>
                  <div class="st-label">操作日志</div>
                  <div class="st-sub">记录所有管理操作</div>
                </div>
                <div class="toggle-switch on"><span class="toggle-thumb"></span></div>
              </div>
              <div class="sys-toggle-row">
                <div>
                  <div class="st-label">IP 白名单</div>
                  <div class="st-sub">限制后台访问 IP</div>
                </div>
                <div class="toggle-switch" @click="ipWhitelist = !ipWhitelist" :class="{ on: ipWhitelist }"><span class="toggle-thumb"></span></div>
              </div>
            </div>
          </div>
        </section>

      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/store/index.js'

const userStore = useUserStore()
const activeTab = ref('overview')
const ipWhitelist = ref(false)

const tabs = [
  { id: 'overview', label: '数据概览', icon: '<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>' },
  { id: 'users',    label: '用户管理', icon: '<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>', badge: '128' },
  { id: 'content',  label: '内容管理', icon: '<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/></svg>' },
  { id: 'orders',   label: '订单管理', icon: '<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 01-8 0"/></svg>', badge: '3' },
  { id: 'system',   label: '系统设置', icon: '<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 010 14.14M4.93 4.93a10 10 0 000 14.14"/></svg>' },
]

const stats = [
  { label: '总用户数',   value: '1,284', trend: '+12%', up: true,  bg: 'rgba(16,163,127,0.15)',  icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#10a37f" stroke-width="2" stroke-linecap="round"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>' },
  { label: '今日活跃',   value: '342',   trend: '+8%',  up: true,  bg: 'rgba(59,130,246,0.15)', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" stroke-width="2" stroke-linecap="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>' },
  { label: '本月订单',   value: '89',    trend: '+23%', up: true,  bg: 'rgba(245,158,11,0.15)', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2" stroke-linecap="round"><path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/></svg>' },
  { label: '本月收入',   value: '¥6,840', trend: '+31%', up: true, bg: 'rgba(139,92,246,0.15)', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#8b5cf6" stroke-width="2" stroke-linecap="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/></svg>' },
]

const revenueTotal = '6,840'

const chartData = [
  { day: '周一', pct: 45 }, { day: '周二', pct: 62 }, { day: '周三', pct: 78 },
  { day: '周四', pct: 55 }, { day: '周五', pct: 91 }, { day: '周六', pct: 84 }, { day: '周日', pct: 70 },
]

const recentActivity = [
  { id: 1, type: 'success', text: '用户 @liming 升级为 Pro 计划', time: '2 分钟前' },
  { id: 2, type: 'info',    text: '新订单 #20240425-089 已创建 ¥68', time: '15 分钟前' },
  { id: 3, type: 'success', text: '新用户 @xiaomei 注册成功', time: '32 分钟前' },
  { id: 4, type: 'warning', text: 'API 调用量达到今日预警阈值 80%', time: '1 小时前' },
  { id: 5, type: 'info',    text: '模板「时尚服装店」被使用 23 次', time: '2 小时前' },
  { id: 6, type: 'success', text: '系统数据库备份完成', time: '6 小时前' },
]

const services = [
  { name: 'Spring Boot API',  ok: true,  latency: '12ms' },
  { name: 'MySQL 数据库',      ok: true,  latency: '3ms' },
  { name: 'Redis 缓存',        ok: true,  latency: '1ms' },
  { name: 'AI Gateway',       ok: true,  latency: '180ms' },
  { name: 'MongoDB',          ok: false, latency: '超时' },
]

// Users
const userSearch = ref('')
const users = [
  { id:1, name:'李明',   email:'liming@qq.com',    plan:'pro',  joined:'2024-01-12', status:'active', color:'#10a37f' },
  { id:2, name:'小美',   email:'xiaomei@163.com',  plan:'free', joined:'2024-02-18', status:'active', color:'#3b82f6' },
  { id:3, name:'张伟',   email:'zhangwei@gmail.com',plan:'pro', joined:'2024-03-05', status:'active', color:'#8b5cf6' },
  { id:4, name:'王芳',   email:'wangfang@qq.com',  plan:'free', joined:'2024-03-21', status:'disabled', color:'#f59e0b' },
  { id:5, name:'陈刚',   email:'chengang@163.com', plan:'pro',  joined:'2024-04-02', status:'active', color:'#ec4899' },
  { id:6, name:'刘洋',   email:'liuyang@sina.com', plan:'free', joined:'2024-04-10', status:'active', color:'#06b6d4' },
]
const filteredUsers = computed(() => {
  const q = userSearch.value.toLowerCase()
  return q ? users.filter(u => u.name.includes(q) || u.email.includes(q)) : users
})

// Content
const contentFilter = ref('全部')
const contentFilters = ['全部', '模板', '图库', '素材']
const contentItems = [
  { id:1, emoji:'👗', name:'时尚服装店',  type:'模板', uses:'2.3k', date:'2024-03', gradient:'linear-gradient(135deg,#10a37f,#065f46)' },
  { id:2, emoji:'☕', name:'精品咖啡馆',  type:'模板', uses:'1.8k', date:'2024-03', gradient:'linear-gradient(135deg,#d97706,#92400e)' },
  { id:3, emoji:'🌸', name:'浪漫花艺店',  type:'模板', uses:'3.1k', date:'2024-02', gradient:'linear-gradient(135deg,#ec4899,#9d174d)' },
  { id:4, emoji:'🖼️', name:'春季外套',    type:'图库', uses:'892',  date:'2024-04', gradient:'linear-gradient(135deg,#667eea,#764ba2)' },
  { id:5, emoji:'👟', name:'运动鞋展示',  type:'图库', uses:'445',  date:'2024-04', gradient:'linear-gradient(135deg,#43e97b,#38f9d7)' },
  { id:6, emoji:'🎨', name:'渐变背景素材',type:'素材', uses:'1.2k', date:'2024-01', gradient:'linear-gradient(135deg,#f093fb,#f5576c)' },
]
const filteredContent = computed(() => {
  return contentFilter.value === '全部' ? contentItems : contentItems.filter(c => c.type === contentFilter.value)
})

// Orders
const orderSearch = ref('')
const orders = [
  { id:'20240425089', user:'李明',  product:'Pro 月度订阅', amount:'68.00', time:'2024-04-25 14:32', status:'paid' },
  { id:'20240425088', user:'张伟',  product:'Pro 月度订阅', amount:'68.00', time:'2024-04-25 11:18', status:'paid' },
  { id:'20240424087', user:'陈刚',  product:'Pro 年度订阅', amount:'588.00', time:'2024-04-24 19:05', status:'paid' },
  { id:'20240424086', user:'王芳',  product:'Pro 月度订阅', amount:'68.00', time:'2024-04-24 09:22', status:'refund' },
  { id:'20240423085', user:'小美',  product:'模板包',       amount:'28.00', time:'2024-04-23 16:47', status:'pending' },
  { id:'20240423084', user:'刘洋',  product:'Pro 月度订阅', amount:'68.00', time:'2024-04-23 10:11', status:'paid' },
]
const filteredOrders = computed(() => {
  const q = orderSearch.value.toLowerCase()
  return q ? orders.filter(o => o.id.includes(q) || o.user.includes(q)) : orders
})
</script>

<style scoped>
/* ── CSS Tokens ── */
.admin-page {
  --adm-bg:       var(--gpt-main);
  --adm-sidebar:  var(--gpt-sidebar);
  --adm-surface:  var(--gpt-input-bg);
  --adm-surface2: var(--dp-surface2);
  --adm-border:   var(--gpt-border);
  --adm-text:     var(--gpt-text);
  --adm-sub:      var(--gpt-text-sub);
  --adm-muted:    var(--gpt-text-muted);
  --adm-accent:   #10a37f;
  --adm-shadow:   var(--dp-shadow);
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--adm-bg);
  color: var(--adm-text);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif;
}

/* ── Top Nav ── */
.admin-nav {
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: var(--adm-sidebar);
  border-bottom: 1px solid var(--adm-border);
  flex-shrink: 0;
  z-index: 10;
}
.nav-left { display: flex; align-items: center; }
.nav-logo { display: flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 700; color: var(--adm-text); }
.logo-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--adm-accent); }
.logo-badge {
  font-size: 10px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;
  background: rgba(16,163,127,0.15); color: var(--adm-accent);
  border: 1px solid rgba(16,163,127,0.25);
  padding: 2px 8px; border-radius: 20px; margin-left: 2px;
}
.nav-right { display: flex; align-items: center; gap: 12px; }
.nav-env {
  font-size: 11px; font-weight: 600; letter-spacing: 0.5px; text-transform: uppercase;
  color: #10a37f; background: rgba(16,163,127,0.1); border: 1px solid rgba(16,163,127,0.2);
  padding: 3px 10px; border-radius: 20px;
}
.nav-avatar {
  width: 30px; height: 30px; background: linear-gradient(135deg,#10a37f,#0d8b6e);
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700; color: #fff; cursor: pointer;
}
.nav-back-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 6px 14px; background: transparent; border: 1px solid var(--adm-border);
  border-radius: 8px; color: var(--adm-sub); font-size: 13px; cursor: pointer;
  transition: all 0.15s;
}
.nav-back-btn:hover { border-color: var(--adm-accent); color: var(--adm-accent); }

/* ── Body Layout ── */
.admin-body { display: flex; flex: 1; overflow: hidden; }

/* ── Admin Sidebar ── */
.admin-sidebar {
  width: 200px;
  min-width: 200px;
  background: var(--adm-sidebar);
  border-right: 1px solid var(--adm-border);
  padding: 16px 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow-y: auto;
  flex-shrink: 0;
}
.asb-btn {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 12px; border-radius: 10px;
  background: transparent; border: none;
  color: var(--adm-sub); cursor: pointer; font-size: 13.5px; font-weight: 500;
  transition: all 0.15s; text-align: left; width: 100%;
  position: relative;
}
.asb-btn:hover { background: var(--gpt-sidebar-hover); color: var(--adm-text); }
.asb-btn.active { background: var(--gpt-sidebar-active); color: var(--adm-text); }
.asb-icon { flex-shrink: 0; display: flex; align-items: center; }
.asb-label { flex: 1; }
.asb-badge {
  font-size: 10px; font-weight: 700; background: var(--adm-accent);
  color: #fff; padding: 1px 6px; border-radius: 20px; margin-left: auto;
}

/* ── Main ── */
.admin-main { flex: 1; overflow-y: auto; padding: 28px; background: var(--adm-bg); }

.tab-content { max-width: 1100px; }

.section-head {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 24px; flex-wrap: wrap; gap: 12px;
}
.section-title { font-size: 20px; font-weight: 700; color: var(--adm-text); margin: 0; }
.section-sub { font-size: 13px; color: var(--adm-muted); margin-left: 10px; }
.head-actions { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }

/* ── Stat Cards ── */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
@media (max-width: 900px) { .stat-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 560px) { .stat-grid { grid-template-columns: 1fr; } }

.stat-card {
  background: var(--adm-surface);
  border: 1px solid var(--adm-border);
  border-radius: 14px;
  padding: 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  transition: box-shadow 0.2s;
}
.stat-card:hover { box-shadow: var(--adm-shadow); }
.sc-icon {
  width: 44px; height: 44px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.sc-info { flex: 1; }
.sc-num { font-size: 22px; font-weight: 700; color: var(--adm-text); line-height: 1.2; }
.sc-label { font-size: 12px; color: var(--adm-sub); margin-top: 2px; }
.sc-trend { font-size: 11px; font-weight: 600; display: flex; align-items: center; gap: 3px; flex-shrink: 0; }
.sc-trend.up { color: #10a37f; }
.sc-trend.down { color: #ef4444; }

/* ── Charts Row ── */
.charts-row { display: grid; grid-template-columns: 2fr 1fr; gap: 16px; margin-bottom: 24px; }
@media (max-width: 768px) { .charts-row { grid-template-columns: 1fr; } }

.chart-card {
  background: var(--adm-surface);
  border: 1px solid var(--adm-border);
  border-radius: 14px;
  padding: 20px;
}
.chart-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.chart-title { font-size: 14px; font-weight: 600; color: var(--adm-text); }
.chart-legend { display: flex; align-items: center; gap: 6px; font-size: 12px; color: var(--adm-sub); }
.dot { width: 8px; height: 8px; border-radius: 50%; }
.dot.green { background: #10a37f; }

.bar-chart {
  display: flex; align-items: flex-end; gap: 8px; height: 100px;
}
.bar-col { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 6px; height: 100%; }
.bar-fill {
  width: 100%; background: linear-gradient(180deg, #10a37f, #0d8b6e);
  border-radius: 4px 4px 0 0; transition: height 0.6s ease;
  min-height: 4px;
}
.bar-lbl { font-size: 10px; color: var(--adm-muted); white-space: nowrap; }

/* Donut */
.donut-wrap { position: relative; width: 120px; height: 120px; margin: 0 auto 14px; }
.donut-svg { width: 100%; height: 100%; transform: rotate(-90deg); }
.donut-center {
  position: absolute; inset: 0; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
}
.donut-num { font-size: 13px; font-weight: 700; color: var(--adm-text); }
.donut-sub { font-size: 10px; color: var(--adm-muted); }
.donut-legend { display: flex; flex-direction: column; gap: 6px; }
.dl-item { display: flex; align-items: center; gap: 8px; font-size: 12px; color: var(--adm-sub); }
.dl-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }

/* ── Table Card ── */
.table-card {
  background: var(--adm-surface);
  border: 1px solid var(--adm-border);
  border-radius: 14px;
  overflow: hidden;
}
.table-head {
  padding: 16px 20px;
  border-bottom: 1px solid var(--adm-border);
}
.table-title { font-size: 14px; font-weight: 600; color: var(--adm-text); }

.data-table { width: 100%; border-collapse: collapse; }
.data-table th {
  padding: 12px 16px; text-align: left;
  font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em;
  color: var(--adm-muted); border-bottom: 1px solid var(--adm-border);
  background: var(--adm-sidebar);
  white-space: nowrap;
}
.data-table td {
  padding: 13px 16px; font-size: 13.5px; color: var(--adm-text);
  border-bottom: 1px solid var(--adm-border);
}
.data-table tr:last-child td { border-bottom: none; }
.data-table tr:hover td { background: var(--gpt-sidebar-hover); }

.cell-user { display: flex; align-items: center; gap: 10px; }
.cell-avatar {
  width: 28px; height: 28px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 700; color: #fff; flex-shrink: 0;
}
.cell-muted { color: var(--adm-sub) !important; }
.cell-mono { font-family: 'JetBrains Mono', 'Courier New', monospace; font-size: 12px !important; color: var(--adm-sub) !important; }
.cell-amount { font-weight: 600; color: #10a37f !important; }

.plan-tag {
  font-size: 11px; font-weight: 700; padding: 3px 9px; border-radius: 20px;
}
.plan-tag.pro { background: rgba(139,92,246,0.15); color: #8b5cf6; }
.plan-tag.free { background: var(--adm-surface2); color: var(--adm-sub); }

.status-dot { display: inline-block; width: 7px; height: 7px; border-radius: 50%; margin-right: 5px; }
.status-dot.active { background: #10a37f; }
.status-dot.disabled { background: #ef4444; }

.order-status { font-size: 11px; font-weight: 600; padding: 3px 9px; border-radius: 20px; }
.order-status.paid    { background: rgba(16,163,127,0.15); color: #10a37f; }
.order-status.pending { background: rgba(245,158,11,0.15); color: #f59e0b; }
.order-status.refund  { background: rgba(239,68,68,0.12);  color: #ef4444; }

.row-actions { display: flex; gap: 6px; }
.ra-btn {
  width: 28px; height: 28px; display: flex; align-items: center; justify-content: center;
  background: transparent; border: 1px solid var(--adm-border);
  border-radius: 7px; cursor: pointer; color: var(--adm-sub);
  transition: all 0.15s;
}
.ra-btn:hover { border-color: var(--adm-accent); color: var(--adm-accent); }
.ra-btn.danger:hover { border-color: #ef4444; color: #ef4444; }

/* ── Activity List ── */
.activity-list { padding: 8px 0; }
.act-row { display: flex; align-items: flex-start; gap: 12px; padding: 12px 20px; }
.act-row:hover { background: var(--gpt-sidebar-hover); }
.act-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; margin-top: 5px; }
.act-dot.success { background: #10a37f; }
.act-dot.info    { background: #3b82f6; }
.act-dot.warning { background: #f59e0b; }
.act-info { flex: 1; display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; }
.act-text { font-size: 13.5px; color: var(--adm-text); }
.act-time { font-size: 11px; color: var(--adm-muted); white-space: nowrap; flex-shrink: 0; }

/* ── Search Box ── */
.search-box {
  display: flex; align-items: center; gap: 8px;
  background: var(--adm-sidebar); border: 1px solid var(--adm-border);
  border-radius: 9px; padding: 7px 12px; min-width: 200px;
  transition: border-color 0.15s;
}
.search-box:focus-within { border-color: var(--adm-accent); }
.search-input {
  background: transparent; border: none; outline: none;
  font-size: 13px; color: var(--adm-text); flex: 1;
}
.search-input::placeholder { color: var(--adm-muted); }

/* ── Filter Tabs ── */
.filter-tabs { display: flex; gap: 4px; }
.ftab {
  padding: 6px 14px; font-size: 12px; font-weight: 500;
  background: transparent; border: 1px solid var(--adm-border);
  border-radius: 20px; color: var(--adm-sub); cursor: pointer;
  transition: all 0.15s;
}
.ftab:hover { border-color: var(--adm-accent); color: var(--adm-text); }
.ftab.active { background: var(--adm-accent); border-color: var(--adm-accent); color: #fff; }

/* ── Action Buttons ── */
.act-btn {
  display: flex; align-items: center; gap: 7px;
  padding: 8px 16px; border-radius: 9px; font-size: 13px; font-weight: 500;
  border: none; cursor: pointer; transition: all 0.15s;
}
.act-btn.primary { background: var(--adm-accent); color: #fff; }
.act-btn.primary:hover { background: #0d8b6e; }
.act-btn.secondary { background: var(--adm-surface2); color: var(--adm-text); border: 1px solid var(--adm-border); }
.act-btn.secondary:hover { border-color: var(--adm-accent); }

/* ── Content Grid ── */
.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 14px;
}
.content-card {
  background: var(--adm-surface);
  border: 1px solid var(--adm-border);
  border-radius: 12px; overflow: hidden;
  transition: transform 0.18s, box-shadow 0.18s;
}
.content-card:hover { transform: translateY(-3px); box-shadow: var(--adm-shadow); }
.cc-preview {
  height: 100px; display: flex; align-items: center; justify-content: center;
  position: relative;
}
.cc-emoji { font-size: 32px; }
.cc-type {
  position: absolute; top: 8px; left: 8px;
  font-size: 10px; font-weight: 600; text-transform: uppercase;
  background: rgba(0,0,0,0.35); color: rgba(255,255,255,0.9);
  padding: 2px 8px; border-radius: 20px; backdrop-filter: blur(4px);
}
.cc-info { padding: 10px 12px; }
.cc-name { display: block; font-size: 13px; font-weight: 600; color: var(--adm-text); margin-bottom: 2px; }
.cc-meta { display: block; font-size: 11px; color: var(--adm-muted); margin-bottom: 8px; }
.cc-actions { display: flex; gap: 6px; }

/* ── System ── */
.sys-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
@media (max-width: 768px) { .sys-grid { grid-template-columns: 1fr; } }

.sys-card {
  background: var(--adm-surface);
  border: 1px solid var(--adm-border);
  border-radius: 14px;
  padding: 20px;
}
.sys-card-title { font-size: 14px; font-weight: 700; color: var(--adm-text); margin: 0 0 16px; }
.sys-field { margin-bottom: 12px; }
.sys-field label { display: block; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: var(--adm-muted); margin-bottom: 5px; }
.sys-input {
  width: 100%; background: var(--adm-bg); border: 1px solid var(--adm-border);
  border-radius: 8px; padding: 9px 12px; font-size: 13px; color: var(--adm-text);
  outline: none; transition: border-color 0.15s; box-sizing: border-box; font-family: inherit;
}
.sys-input:focus { border-color: var(--adm-accent); }
.sys-input[readonly] { opacity: 0.6; cursor: not-allowed; }

.sys-status-list { display: flex; flex-direction: column; gap: 8px; }
.ssl-row { display: flex; align-items: center; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid var(--adm-border); }
.ssl-row:last-child { border-bottom: none; }
.ssl-left { display: flex; align-items: center; gap: 10px; }
.ssl-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.ssl-dot.green { background: #10a37f; box-shadow: 0 0 6px rgba(16,163,127,0.5); }
.ssl-dot.red   { background: #ef4444; }
.ssl-name { font-size: 13.5px; color: var(--adm-text); }
.ssl-latency { font-size: 12px; color: var(--adm-muted); font-family: monospace; }

.db-stats { display: flex; gap: 16px; margin-top: 12px; padding-top: 12px; border-top: 1px solid var(--adm-border); }
.db-stat { display: flex; flex-direction: column; gap: 2px; }
.db-stat span { font-size: 11px; color: var(--adm-muted); }
.db-stat strong { font-size: 16px; font-weight: 700; color: var(--adm-text); }

/* Toggle switch (reuse from Settings) */
.sys-toggle-row { display: flex; align-items: center; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid var(--adm-border); }
.sys-toggle-row:last-child { border-bottom: none; }
.st-label { font-size: 13.5px; font-weight: 500; color: var(--adm-text); }
.st-sub { font-size: 11px; color: var(--adm-muted); margin-top: 2px; }
.toggle-switch {
  width: 42px; height: 24px; background: var(--adm-surface2);
  border: 2px solid var(--adm-border); border-radius: 12px;
  cursor: pointer; position: relative; transition: all 0.25s; flex-shrink: 0;
}
.toggle-switch.on { background: #10a37f; border-color: #10a37f; }
.toggle-thumb {
  position: absolute; top: 2px; left: 2px;
  width: 16px; height: 16px; background: white; border-radius: 50%;
  transition: transform 0.25s cubic-bezier(0.4,0,0.2,1);
  box-shadow: 0 1px 3px rgba(0,0,0,0.3);
}
.toggle-switch.on .toggle-thumb { transform: translateX(18px); }

/* Mobile sidebar collapse */
@media (max-width: 700px) {
  .admin-sidebar { width: 56px; min-width: 56px; }
  .asb-label, .asb-badge { display: none; }
  .asb-btn { justify-content: center; }
  .admin-main { padding: 16px; }
}
</style>
