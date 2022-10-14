<script setup lang="ts">
import { ElDropdown, ElDropdownMenu, ElDropdownItem } from 'element-plus'
import { PropType, ref, defineEmits } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useDesign } from '@/hooks/web/useDesign'
import type { RouteLocationNormalizedLoaded } from 'vue-router'
import { contextMenuSchema } from '../../../types/contextMenu'

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('context-menu')

const { t } = useI18n()

const emit = defineEmits(['visibleChange'])

const props = defineProps({
  schema: {
    type: Array as PropType<contextMenuSchema[]>,
    default: () => []
  },
  trigger: {
    type: String as PropType<'click' | 'hover' | 'focus' | 'contextmenu'>,
    default: 'contextmenu'
  },
  tagItem: {
    type: Object as PropType<RouteLocationNormalizedLoaded>,
    default: () => ({})
  }
})

const command = (item: contextMenuSchema) => {
  item.command && item.command(item)
}

const visibleChange = (visible: boolean) => {
  emit('visibleChange', visible, props.tagItem)
}

const elDropdownMenuRef = ref<ComponentRef<typeof ElDropdown>>()

defineExpose({
  elDropdownMenuRef,
  tagItem: props.tagItem
})
</script>

<template>
  <ElDropdown
    ref="elDropdownMenuRef"
    :class="prefixCls"
    :trigger="trigger"
    placement="bottom-start"
    @command="command"
    @visible-change="visibleChange"
    popper-class="v-context-menu-popper"
  >
    <slot></slot>
    <template #dropdown>
      <ElDropdownMenu>
        <ElDropdownItem
          v-for="(item, index) in schema"
          :key="`dropdown${index}`"
          :divided="item.divided"
          :disabled="item.disabled"
          :command="item"
        >
          <Icon :icon="item.icon" /> {{ t(item.label) }}
        </ElDropdownItem>
      </ElDropdownMenu>
    </template>
  </ElDropdown>
</template>
