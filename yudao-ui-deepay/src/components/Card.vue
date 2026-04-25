<!--
  Card.vue — dark surface card
  Props:
    pad: apply inner padding (default true)
-->
<script setup>
defineProps({
  pad: { type: Boolean, default: true },
})
</script>

<template>
  <div :class="['bg-surface2 rounded-2xl overflow-hidden border border-border transition-all duration-200', pad ? 'p-4' : '']">
    <slot />
  </div>
</template>
