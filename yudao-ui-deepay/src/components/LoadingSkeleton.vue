<!--
  LoadingSkeleton.vue — 纯骨架屏
  Props:
    count: Number — 卡片数量（default 10）
    cols:  Number — 列数（default 2）
-->
<script setup>
defineProps({
  count: { type: Number, default: 10 },
  cols:  { type: Number, default: 2 },
})
</script>

<template>
  <div
    :class="`grid gap-3 animate-pulse`"
    :style="{ gridTemplateColumns: `repeat(${cols}, minmax(0, 1fr))` }"
  >
    <div
      v-for="i in count" :key="i"
      class="aspect-[3/4] rounded-2xl bg-surface2"
    />
  </div>
</template>
