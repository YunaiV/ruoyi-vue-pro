<script setup>
import { RouterView } from 'vue-router'
import { useThemeStore } from '@/store/modules/theme'

// Initialize theme (applies data-theme attribute to <html>)
useThemeStore()
</script>

<template>
  <RouterView />
</template>

<style>
/* ── CSS design tokens — overridden per theme via [data-theme] ── */
:root, [data-theme="dark"] {
  --c-bg:           #0a0a0a;
  --c-surface:      #141414;
  --c-surface2:     #1e1e1e;
  --c-card:         rgba(255,255,255,0.035);
  --c-card-border:  rgba(255,255,255,0.08);
  --c-text:         #e8e8e8;
  --c-text-bright:  #ffffff;
  --c-text-sub:     #999;
  --c-text-muted:   #555;
  --c-accent:       #1abc9c;
  --c-accent-hover: #16a085;
  --c-accent-bg:    rgba(26,188,156,0.12);
  --c-border:       rgba(255,255,255,0.07);
  --c-border2:      rgba(255,255,255,0.12);
  --c-nav-bg:       rgba(10,10,10,0.95);
  --c-header-bg:    rgba(10,10,10,0.88);
  --c-input-bg:     rgba(20,20,20,0.98);
  --c-chip-bg:      rgba(255,255,255,0.06);
  --c-chip-border:  rgba(255,255,255,0.1);
  --c-shadow:       0 4px 24px rgba(0,0,0,0.5);
  --c-shadow-lg:    0 8px 40px rgba(0,0,0,0.7);
  --c-danger:       #ef4444;
  /* legacy */
  --bg-main:   #0a0a0a;
  --bg-card:   #1a1a1a;
  --accent:    #1abc9c;
  --accent-dk: #16a085;
  --border:    rgba(255,255,255,0.07);
}

[data-theme="light"] {
  --c-bg:           #f2f2f7;
  --c-surface:      #ffffff;
  --c-surface2:     #f8f8f8;
  --c-card:         rgba(255,255,255,0.9);
  --c-card-border:  rgba(0,0,0,0.08);
  --c-text:         #1a1a1a;
  --c-text-bright:  #000000;
  --c-text-sub:     #6e6e73;
  --c-text-muted:   #ababab;
  --c-accent:       #1abc9c;
  --c-accent-hover: #16a085;
  --c-accent-bg:    rgba(26,188,156,0.1);
  --c-border:       rgba(0,0,0,0.08);
  --c-border2:      rgba(0,0,0,0.14);
  --c-nav-bg:       rgba(242,242,247,0.95);
  --c-header-bg:    rgba(242,242,247,0.92);
  --c-input-bg:     rgba(255,255,255,0.98);
  --c-chip-bg:      rgba(0,0,0,0.05);
  --c-chip-border:  rgba(0,0,0,0.1);
  --c-shadow:       0 2px 16px rgba(0,0,0,0.08);
  --c-shadow-lg:    0 8px 32px rgba(0,0,0,0.12);
  --c-danger:       #ef4444;
  /* legacy */
  --bg-main:   #f2f2f7;
  --bg-card:   #ffffff;
  --accent:    #1abc9c;
  --accent-dk: #16a085;
  --border:    rgba(0,0,0,0.08);
}
</style>
