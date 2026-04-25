<!--
  RoleAvatar.vue — 每个 AI 角色的 SVG 矢量头像

  用法:
    <RoleAvatar module="design" :size="40" :animated="true" />

  当前使用 SVG + CSS 动画作为占位方案，
  后续可直接替换为 Lottie JSON 动画或自定义 SVG 资源。
-->
<template>
  <div
    class="role-avatar"
    :class="[`role-avatar--${module}`, { 'role-avatar--animated': animated }]"
    :style="{ width: size + 'px', height: size + 'px' }"
    :aria-label="config.roleName"
  >
    <!-- ── 购物顾问 (selection) ─── 购物袋 + 微笑 -->
    <svg v-if="module==='selection'" viewBox="0 0 48 48" fill="none">
      <circle cx="24" cy="24" r="22" :fill="bg" />
      <!-- bag body -->
      <rect x="13" y="22" width="22" height="16" rx="3" fill="white" opacity="0.95"/>
      <!-- bag handle -->
      <path d="M18 22 C18 16 30 16 30 22" stroke="white" stroke-width="2.5" fill="none" stroke-linecap="round"/>
      <!-- smile face on bag -->
      <circle cx="24" cy="30" r="4" fill="#fbbf24" />
      <path d="M22 31 Q24 33 26 31" stroke="#1f2937" stroke-width="1.2" fill="none" stroke-linecap="round"/>
      <circle cx="22.5" cy="29.5" r="0.7" fill="#1f2937"/>
      <circle cx="25.5" cy="29.5" r="0.7" fill="#1f2937"/>
    </svg>

    <!-- ── AI 设计师 (design) ─── 调色板 + 画笔 -->
    <svg v-else-if="module==='design'" viewBox="0 0 48 48" fill="none">
      <circle cx="24" cy="24" r="22" :fill="bg" />
      <!-- palette -->
      <ellipse cx="24" cy="26" rx="11" ry="9" fill="white" opacity="0.95"/>
      <!-- color dots -->
      <circle cx="18" cy="24" r="2.2" fill="#ef4444"/>
      <circle cx="24" cy="21" r="2.2" fill="#fbbf24"/>
      <circle cx="30" cy="24" r="2.2" fill="#34d399"/>
      <circle cx="27" cy="29" r="2.2" fill="#60a5fa"/>
      <!-- thumb hole -->
      <circle cx="22" cy="29" r="2.5" :fill="bg"/>
      <!-- paintbrush -->
      <line x1="30" y1="16" x2="36" y2="10" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
      <path d="M28 18 L32 14 L35 17 L31 21 Z" fill="#fcd34d"/>
    </svg>

    <!-- ── 产品经理 (product) ─── 包裹/文件夹 -->
    <svg v-else-if="module==='product'" viewBox="0 0 48 48" fill="none">
      <circle cx="24" cy="24" r="22" :fill="bg" />
      <!-- box -->
      <rect x="12" y="20" width="24" height="16" rx="2" fill="white" opacity="0.95"/>
      <!-- box lid -->
      <rect x="10" y="16" width="28" height="6" rx="2" fill="white" opacity="0.75"/>
      <!-- ribbon -->
      <line x1="24" y1="16" x2="24" y2="36" stroke="#60a5fa" stroke-width="2.5"/>
      <line x1="12" y1="23" x2="36" y2="23" stroke="#60a5fa" stroke-width="2.5"/>
      <!-- checkmark -->
      <path d="M19 29 L22 32 L29 25" stroke="#10b981" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>

    <!-- ── 库存专员 (inventory) ─── 仓库/货架 -->
    <svg v-else-if="module==='inventory'" viewBox="0 0 48 48" fill="none">
      <circle cx="24" cy="24" r="22" :fill="bg" />
      <!-- shelves -->
      <rect x="11" y="14" width="26" height="3" rx="1.5" fill="white" opacity="0.9"/>
      <rect x="11" y="22" width="26" height="3" rx="1.5" fill="white" opacity="0.9"/>
      <rect x="11" y="30" width="26" height="3" rx="1.5" fill="white" opacity="0.9"/>
      <!-- support poles -->
      <rect x="12" y="14" width="2.5" height="22" rx="1.2" fill="white" opacity="0.6"/>
      <rect x="33.5" y="14" width="2.5" height="22" rx="1.2" fill="white" opacity="0.6"/>
      <!-- boxes on shelves -->
      <rect x="15" y="17" width="5" height="5" rx="1" fill="#fbbf24" opacity="0.9"/>
      <rect x="21" y="17" width="5" height="5" rx="1" fill="#60a5fa" opacity="0.9"/>
      <rect x="15" y="25" width="8" height="5" rx="1" fill="#a78bfa" opacity="0.9"/>
      <rect x="25" y="25" width="5" height="5" rx="1" fill="#34d399" opacity="0.9"/>
    </svg>

    <!-- ── 财务总监 (finance) ─── 西装 + 领带 + 图表 -->
    <svg v-else-if="module==='finance'" viewBox="0 0 48 48" fill="none">
      <circle cx="24" cy="24" r="22" :fill="bg" />
      <!-- head -->
      <circle cx="24" cy="17" r="7" fill="white" opacity="0.95"/>
      <!-- body/suit -->
      <path d="M12 40 C12 32 18 30 24 30 C30 30 36 32 36 40" fill="white" opacity="0.9"/>
      <!-- tie -->
      <path d="M22 30 L24 38 L26 30 L24 28 Z" fill="#60a5fa" opacity="0.9"/>
      <!-- chart bars -->
      <rect x="15" y="35" width="3" height="5" rx="1" fill="#34d399" opacity="0.8"/>
      <rect x="19" y="33" width="3" height="7" rx="1" fill="#34d399" opacity="0.9"/>
      <rect x="23" y="31" width="3" height="9" rx="1" fill="#34d399"/>
    </svg>

    <!-- ── 趋势分析师 (trend) ─── 折线图 + 放大镜 -->
    <svg v-else-if="module==='trend'" viewBox="0 0 48 48" fill="none">
      <circle cx="24" cy="24" r="22" :fill="bg" />
      <!-- chart background -->
      <rect x="10" y="14" width="28" height="20" rx="3" fill="white" opacity="0.15"/>
      <!-- trend line (going up) -->
      <polyline points="12,30 18,24 24,26 30,18 36,14"
                stroke="white" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
      <!-- dots on line -->
      <circle cx="12" cy="30" r="2" fill="white"/>
      <circle cx="18" cy="24" r="2" fill="#fbbf24"/>
      <circle cx="24" cy="26" r="2" fill="white"/>
      <circle cx="30" cy="18" r="2" fill="#fbbf24"/>
      <circle cx="36" cy="14" r="2" fill="white"/>
      <!-- up arrow -->
      <path d="M36 10 L39 14 L33 14 Z" fill="#fbbf24"/>
      <!-- bottom axis -->
      <line x1="10" y1="36" x2="38" y2="36" stroke="white" stroke-width="1.5" opacity="0.5"/>
    </svg>

    <!-- ── 客服专员 (order) ─── 耳机 + 对话气泡 -->
    <svg v-else-if="module==='order'" viewBox="0 0 48 48" fill="none">
      <circle cx="24" cy="24" r="22" :fill="bg" />
      <!-- headset arc -->
      <path d="M13 24 C13 16 35 16 35 24" stroke="white" stroke-width="3" fill="none" stroke-linecap="round"/>
      <!-- ear cups -->
      <rect x="10" y="22" width="6" height="8" rx="3" fill="white" opacity="0.9"/>
      <rect x="32" y="22" width="6" height="8" rx="3" fill="white" opacity="0.9"/>
      <!-- mic boom -->
      <path d="M35 27 L37 32 L34 33" stroke="white" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
      <!-- chat bubble -->
      <rect x="16" y="32" width="16" height="9" rx="4" fill="white" opacity="0.9"/>
      <path d="M22 41 L20 44 L26 41" fill="white" opacity="0.9"/>
      <!-- dots in bubble -->
      <circle cx="20" cy="36.5" r="1.3" fill="#6366f1"/>
      <circle cx="24" cy="36.5" r="1.3" fill="#6366f1"/>
      <circle cx="28" cy="36.5" r="1.3" fill="#6366f1"/>
    </svg>

    <!-- ── Default robot ─── 通用机器人 -->
    <svg v-else viewBox="0 0 48 48" fill="none">
      <circle cx="24" cy="24" r="22" :fill="bg" />
      <!-- antenna -->
      <line x1="24" y1="8" x2="24" y2="14" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
      <circle cx="24" cy="7" r="2.5" fill="white"/>
      <!-- head -->
      <rect x="13" y="14" width="22" height="16" rx="5" fill="white" opacity="0.95"/>
      <!-- eyes -->
      <circle cx="19.5" cy="20" r="3" fill="#6366f1"/>
      <circle cx="28.5" cy="20" r="3" fill="#6366f1"/>
      <circle cx="20.5" cy="19" r="1" fill="white"/>
      <circle cx="29.5" cy="19" r="1" fill="white"/>
      <!-- mouth -->
      <rect x="17" y="25" width="14" height="2.5" rx="1.2" fill="#e0e7ff"/>
      <!-- body -->
      <rect x="16" y="32" width="16" height="10" rx="3" fill="white" opacity="0.85"/>
      <circle cx="20" cy="37" r="2" fill="#6366f1" opacity="0.7"/>
      <circle cx="28" cy="37" r="2" fill="#6366f1" opacity="0.7"/>
    </svg>

    <!-- Animated shimmer overlay -->
    <div v-if="animated" class="role-avatar-shimmer" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getRoleConfig } from '@/config/aiRoles'

const props = withDefaults(defineProps<{
  module:    string
  size?:     number
  animated?: boolean
}>(), {
  size:      48,
  animated:  true,
})

const config = computed(() => getRoleConfig(props.module))

/** Extract the first color from the role gradient for the circle fill */
const bg = computed(() => {
  const g = config.value.gradient
  const match = g.match(/#[0-9a-f]{6}/i)
  return match ? match[0] : '#6366f1'
})
</script>

<style scoped>
.role-avatar {
  position: relative;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}
.role-avatar svg { width: 100%; height: 100%; display: block; }

/* Shimmer sweep animation */
.role-avatar-shimmer {
  position: absolute; inset: 0; border-radius: 50%;
  background: linear-gradient(
    105deg,
    transparent 40%,
    rgba(255,255,255,0.28) 50%,
    transparent 60%
  );
  background-size: 200% 100%;
  animation: shimmer 3.5s ease-in-out infinite;
}
@keyframes shimmer {
  0%   { background-position: 200% center; }
  50%  { background-position: -200% center; }
  100% { background-position: 200% center; }
}

/* Module-specific subtle pulse */
.role-avatar--selection.role-avatar--animated { animation: avatar-bounce 3s ease-in-out infinite; }
.role-avatar--design.role-avatar--animated    { animation: avatar-rotate 8s linear infinite; }
.role-avatar--trend.role-avatar--animated     { animation: avatar-bounce 2.5s ease-in-out infinite; }

@keyframes avatar-bounce {
  0%,100% { transform: translateY(0); }
  50%     { transform: translateY(-3px); }
}
@keyframes avatar-rotate {
  0%   { transform: rotate(0deg); }
  25%  { transform: rotate(3deg); }
  75%  { transform: rotate(-3deg); }
  100% { transform: rotate(0deg); }
}
</style>
