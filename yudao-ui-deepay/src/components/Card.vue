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
  <div :class="['bg-surface rounded-2xl overflow-hidden', pad ? 'p-4' : '']">
    <slot />
  </div>
</template>
