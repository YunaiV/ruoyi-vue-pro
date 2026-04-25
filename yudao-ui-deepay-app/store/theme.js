import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const saved  = uni.getStorageSync('deepay_theme')
  const isDark = ref(saved !== 'light')

  function applyTheme() {
    uni.setStorageSync('deepay_theme', isDark.value ? 'dark' : 'light')
    // Apply class to page root for CSS variable switching
    const pages = getCurrentPages()
    if (pages.length) {
      const page = pages[pages.length - 1]
      const el   = page?.$el || page?.querySelector?.('page')
      if (el) {
        el.className = isDark.value ? 'dp-dark' : 'dp-light'
      }
    }
  }

  function toggle() {
    isDark.value = !isDark.value
    applyTheme()
  }

  return { isDark, toggle, applyTheme }
})
