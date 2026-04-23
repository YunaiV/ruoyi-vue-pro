<!--
  Loading.vue — flexible loading indicator
  Props:
    type: 'grid'   → 10-card shimmer skeleton (for image grid)
          'spinner' → centered spinning ring (for full-page wait)
          'bar'    → slim top progress bar
    count: number of skeleton cards (grid mode only, default 10)
-->
<script setup>
defineProps({
  type:  { type: String, default: 'grid' },
  count: { type: Number, default: 10 },
})
</script>

<template>
  <!-- Grid skeleton -->
  <div v-if="type === 'grid'" class="grid grid-cols-2 gap-3">
    <div
      v-for="n in count" :key="n"
      class="skeleton aspect-[3/4]"
    />
  </div>

  <!-- Full-page spinner -->
  <div v-else-if="type === 'spinner'"
       class="flex flex-col items-center justify-center gap-4 py-20">
    <svg class="animate-spin h-10 w-10 text-accent" viewBox="0 0 24 24" fill="none">
      <circle cx="12" cy="12" r="10" stroke="currentColor"
              stroke-opacity=".2" stroke-width="3"/>
      <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor"
            stroke-width="3" stroke-linecap="round"/>
    </svg>
    <slot>
      <p class="text-muted text-sm">加载中…</p>
    </slot>
  </div>

  <!-- Slim progress bar -->
  <div v-else-if="type === 'bar'"
       class="fixed top-0 left-0 right-0 h-0.5 z-50 overflow-hidden bg-surface2">
    <div class="h-full bg-accent animate-bar" />
  </div>
</template>

<style scoped>
@keyframes bar {
  0%   { transform: translateX(-100%); }
  50%  { transform: translateX(-10%); }
  100% { transform: translateX(100%); }
}
.animate-bar {
  animation: bar 1.6s ease-in-out infinite;
}
</style>
