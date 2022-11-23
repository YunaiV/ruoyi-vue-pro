<script setup lang="ts">
import { computed } from 'vue'
import { isDark } from '@/utils/is'
import { useAppStore } from '@/store/modules/app'
import { useDesign } from '@/hooks/web/useDesign'
import { ConfigGlobal } from '@/components/ConfigGlobal'
import { useCache } from '@/hooks/web/useCache'

const { getPrefixCls } = useDesign()
const prefixCls = getPrefixCls('app')
const appStore = useAppStore()
const currentSize = computed(() => appStore.getCurrentSize)
const greyMode = computed(() => appStore.getGreyMode)
const { wsCache } = useCache()

// 根据浏览器当前主题设置系统主题色
const setDefaultTheme = () => {
  if (wsCache.get('isDark')) {
    if (wsCache.get('isDark') || wsCache.get('isDark') === 'true') {
      appStore.setIsDark(true)
    } else {
      appStore.setIsDark(false)
    }
    return
  }
  const isDarkTheme = isDark()
  appStore.setIsDark(isDarkTheme)
}
setDefaultTheme()
</script>
<template>
  <ConfigGlobal :size="currentSize">
    <RouterView :class="greyMode ? `${prefixCls}-grey-mode` : ''" />
  </ConfigGlobal>
</template>
<style lang="scss">
$prefix-cls: #{$namespace}-app;
.size {
  width: 100%;
  height: 100%;
}
html,
body {
  padding: 0 !important;
  margin: 0;
  overflow: hidden;
  @extend .size;

  #app {
    @extend .size;
  }
}
.#{$prefix-cls}-grey-mode {
  filter: grayscale(100%);
}
</style>
