<!--
  HeaderBar.vue — 通用顶部导航
  Props:
    title:       String  — 中间标题（可选）
    showBack:    Boolean — 显示返回箭头
    quota:       Number  — 剩余次数（-1 = 不显示）
    quotaStatus: String  — 'ok' | 'low' | 'empty'
  Emits:
    back
    quotaClick
-->
<script setup>
defineProps({
  title:       { type: String,  default: '' },
  showBack:    { type: Boolean, default: false },
  quota:       { type: Number,  default: -1 },
  quotaStatus: { type: String,  default: 'ok' },
})
defineEmits(['back', 'quotaClick'])
</script>

<template>
  <header class="sticky top-0 z-10 bg-bg/90 backdrop-blur-md
                 border-b border-border px-4 py-3
                 flex items-center justify-between gap-3">

    <!-- 左侧：返回 or 品牌名 -->
    <div class="flex items-center gap-2 min-w-0">
      <button v-if="showBack"
              class="text-muted active:text-white transition-colors shrink-0"
              @click="$emit('back')">
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24"
             stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7"/>
        </svg>
      </button>
      <span v-if="title" class="font-semibold text-sm truncate">{{ title }}</span>
      <span v-else class="font-semibold text-sm">Deepay</span>
    </div>

    <!-- 右侧：配额徽章 -->
    <button
      v-if="quota >= 0"
      :class="[
        'shrink-0 text-xs font-semibold px-3 py-1.5 rounded-full',
        quotaStatus === 'empty' ? 'badge-empty' :
        quotaStatus === 'low'   ? 'badge-low'   : 'badge-ok',
      ]"
      @click="$emit('quotaClick')"
    >
      {{ quota === 0 ? '次数已用完 👇' : `剩余 ${quota} 次` }}
    </button>

    <!-- 无配额时显示 AI 标签 -->
    <span v-else class="text-accent text-xs font-semibold tracking-wide shrink-0">AI</span>

  </header>
</template>
