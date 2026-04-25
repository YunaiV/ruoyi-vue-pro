<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView />
</template>

<style>
/* ── CSS design tokens (used by inline styles where Tailwind can't reach) ── */
:root {
  --c-bg:      #000000;
  --c-surface: #080e0c;
  --c-card:    #0d1512;
  --c-text:    #e7e9ea;
  --c-text2:   #a8b5b0;
  --c-muted:   #627870;
  --c-accent:  #1abc9c;
  --c-accent-dk: #16a085;
  --c-border:  #1e2e28;
  --c-danger:  #ef4444;
  /* Legacy aliases for components using old vars */
  --bg-main:   #000000;
  --bg-card:   #0d1512;
  --bg-soft:   #121c18;
  --bg-soft2:  #182620;
  --text-main: #e7e9ea;
  --text-sub:  #a8b5b0;
  --accent:    #1abc9c;
  --accent-dk: #16a085;
  --border:    #1e2e28;
  --danger:    #ef4444;
}
</style>
