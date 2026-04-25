<template>
  <div class="feature-row scrollbar-hide">
    <button
      v-for="feat in features"
      :key="feat.label"
      class="dp-chip feature-chip"
      @click="$emit('navigate', feat.path)"
    >
      <span>{{ feat.emoji }}</span>
      <span>{{ feat.label }}</span>
    </button>
  </div>
</template>

<script setup>
defineEmits(['navigate'])

const features = [
  { emoji: '🎯', label: 'AI出款',  path: '/' },
  { emoji: '🎭', label: '灵感库',  path: '/image-library' },
  { emoji: '🌿', label: '整季系列',path: '/template-library' },
  { emoji: '🔧', label: '改款工具',path: '/design-library' },
  { emoji: '📐', label: '设计稿',  path: '/design-library' },
  { emoji: '📊', label: 'AI销售',  path: '/ai-sales' },
]
</script>

<style scoped>
.feature-row {
  display: flex; gap: 8px;
  overflow-x: auto;
  padding: 4px 2px;
}
.feature-chip {
  flex-shrink: 0;
  font-size: 13px;
}
</style>
