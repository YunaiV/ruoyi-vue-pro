<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView />
</template>

<style>
/* ── CSS design tokens (used by inline styles where Tailwind can't reach) ── */
:root {
  --bg-main:   #000000;
  --bg-card:   #0d0d0d;
  --bg-soft:   #141414;
  --bg-soft2:  #1a1a1a;
  --text-main: #e7e9ea;
  --text-sub:  #8b95a1;
  --accent:    #1abc9c;
  --accent-dk: #16a085;
  --border:    #222222;
  --danger:    #e74c3c;
}
</style>
