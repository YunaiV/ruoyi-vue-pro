<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView />
</template>

<style>
/* ── CSS design tokens (used by inline styles where Tailwind can't reach) ── */
:root {
  --c-bg:           #0a0a0a;
  --c-surface:      #1a1a1a;
  --c-card:         #2a2a2a;
  --c-text:         #e0e0e0;
  --c-text-bright:  #ffffff;
  --c-text-sub:     #a0a0a0;
  --c-accent:       #1abc9c;
  --c-accent-hover: #16a085;
  --c-border:       #2a2a2a;
  --c-border2:      #3a3a3a;
  --c-danger:       #ef4444;
  /* Legacy aliases */
  --bg-main:   #0a0a0a;
  --bg-card:   #1a1a1a;
  --bg-soft:   #222222;
  --bg-soft2:  #2a2a2a;
  --text-main: #e0e0e0;
  --text-sub:  #a0a0a0;
  --accent:    #1abc9c;
  --accent-dk: #16a085;
  --border:    #2a2a2a;
  --danger:    #ef4444;
}
</style>
