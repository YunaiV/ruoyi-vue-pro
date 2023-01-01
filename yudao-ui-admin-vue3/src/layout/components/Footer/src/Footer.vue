<script setup lang="ts">
import { useAppStore } from '@/store/modules/app'
import { computed } from 'vue'
import { useDesign } from '@/hooks/web/useDesign'

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('footer')

const appStore = useAppStore()

const title = computed(() => appStore.getTitle)
</script>

<template>
  <div
    :class="prefixCls"
    class="text-center text-[var(--el-text-color-placeholder)] bg-[var(--app-contnet-bg-color)] h-[var(--app-footer-height)] leading-[var(--app-footer-height)] dark:bg-[var(--el-bg-color)]"
  >
    <p style="font-size: 14px">Copyright ©2022-{{ title }}</p>
  </div>
</template>
