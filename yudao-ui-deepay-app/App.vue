<script setup>
import { onLaunch, onShow } from '@dcloudio/uni-app'
import { useThemeStore } from '@/store/theme'

onLaunch(() => {
  const theme = useThemeStore()
  theme.applyTheme()
})
</script>

<template>
  <page-meta :page-style="'background-color:' + bgColor" />
</template>

<script>
import { useThemeStore } from '@/store/theme'
import { computed } from 'vue'
export default {
  setup() {
    const theme = useThemeStore()
    const bgColor = computed(() => theme.isDark ? '#0a0a0a' : '#f2f2f7')
    return { bgColor }
  }
}
</script>

<style lang="scss">
/* Import Wot Design Uni base styles */
@import 'wot-design-uni/components/common/style/wd-common.scss';
@import './styles/theme.scss';
@import './styles/global.scss';
</style>
