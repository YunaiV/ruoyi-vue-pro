<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView />
</template>

<style>
/* ── CSS design tokens (used by inline styles where Tailwind can't reach) ── */
:root {
  --bg-main:  #0B0B0B;
  --bg-card:  #111111;
  --bg-soft:  #1A1A1A;
  --text-main:#FFFFFF;
  --text-sub: #9CA3AF;
  --accent:   #00FF88;
}
</style>
