<script setup>
defineProps({
  title:    { type: String,  default: 'Deepay' },
  showBack: { type: Boolean, default: false },
  isDark:   { type: Boolean, default: true },
})
defineEmits(['back', 'toggle-theme'])
</script>

<template>
  <header class="app-navbar">
    <div class="navbar-safe" />
    <div class="navbar-inner">
      <!-- Left: back button or logo -->
      <div class="navbar-left">
        <button v-if="showBack" class="nav-icon-btn" @click="$emit('back')">
          <svg width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7"/>
          </svg>
        </button>
        <div v-else class="nav-logo">
          <div class="nav-logo-mark">D</div>
        </div>
      </div>

      <!-- Center: title -->
      <h1 class="navbar-title">{{ title }}</h1>

      <!-- Right: theme toggle -->
      <div class="navbar-right">
        <button class="nav-icon-btn" @click="$emit('toggle-theme')">
          <span style="font-size:18px">{{ isDark ? '☀️' : '🌙' }}</span>
        </button>
      </div>
    </div>
  </header>
</template>

<style scoped>
.app-navbar {
  position: sticky; top: 0; z-index: 100;
  background: var(--c-header-bg);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-bottom: 1px solid var(--c-border);
}
.navbar-safe { height: env(safe-area-inset-top); }
.navbar-inner {
  height: 52px; display: flex; align-items: center;
  padding: 0 12px; position: relative;
}
.navbar-left  { width: 44px; display: flex; align-items: center; }
.navbar-right { width: 44px; display: flex; align-items: center; justify-content: flex-end; margin-left: auto; }
.navbar-title {
  position: absolute; left: 50%; transform: translateX(-50%);
  font-size: 16px; font-weight: 700; color: var(--c-text-bright);
  margin: 0; white-space: nowrap;
}
.nav-icon-btn {
  width: 36px; height: 36px; border-radius: 50%;
  background: var(--c-card); border: 1px solid var(--c-card-border);
  display: flex; align-items: center; justify-content: center;
  color: var(--c-text); cursor: pointer; transition: all 0.15s;
}
.nav-icon-btn:active { transform: scale(0.9); }
.nav-logo-mark {
  width: 32px; height: 32px; border-radius: 8px;
  background: linear-gradient(135deg, #1abc9c, #0d6e5a);
  display: flex; align-items: center; justify-content: center;
  font-weight: 900; font-size: 14px; color: #fff;
}
</style>
