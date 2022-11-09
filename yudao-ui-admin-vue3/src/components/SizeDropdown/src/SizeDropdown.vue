<script setup lang="ts">
import { computed } from 'vue'
import { ElDropdown, ElDropdownMenu, ElDropdownItem } from 'element-plus'
import { useAppStore } from '@/store/modules/app'
import { useI18n } from '@/hooks/web/useI18n'
import { propTypes } from '@/utils/propTypes'
import { useDesign } from '@/hooks/web/useDesign'
import { ElementPlusSize } from '@/types/elementPlus'

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('size-dropdown')

defineProps({
  color: propTypes.string.def('')
})

const { t } = useI18n()

const appStore = useAppStore()

const sizeMap = computed(() => appStore.sizeMap)

const setCurrentSize = (size: ElementPlusSize) => {
  appStore.setCurrentSize(size)
}
</script>

<template>
  <ElDropdown :class="prefixCls" trigger="click" @command="setCurrentSize">
    <Icon :size="18" icon="mdi:format-size" :color="color" class="cursor-pointer" />
    <template #dropdown>
      <ElDropdownMenu>
        <ElDropdownItem v-for="item in sizeMap" :key="item" :command="item">
          {{ t(`size.${item}`) }}
        </ElDropdownItem>
      </ElDropdownMenu>
    </template>
  </ElDropdown>
</template>
