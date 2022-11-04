<script setup lang="ts">
import { propTypes } from '@/utils/propTypes'
import { computed, useAttrs, useSlots } from 'vue'

const slots = useSlots()

const props = defineProps({
  id: propTypes.string.def('model_1'),
  modelValue: propTypes.bool.def(false),
  fullscreen: propTypes.bool.def(false),
  loading: propTypes.bool.def(false),
  title: propTypes.string.def('弹窗'),
  width: propTypes.string.def('800'),
  height: propTypes.string.def('480'),
  minWidth: propTypes.string.def('460'),
  minHeight: propTypes.string.def('320'),
  showFooter: propTypes.bool.def(true)
})

const getBindValue = computed(() => {
  const delArr: string[] = ['title']
  const attrs = useAttrs()
  const obj = { ...attrs, ...props }
  for (const key in obj) {
    if (delArr.indexOf(key) !== -1) {
      delete obj[key]
    }
  }
  return obj
})
</script>

<template>
  <vxe-modal
    v-bind="getBindValue"
    :width="width"
    :height="height"
    :title="title"
    min-width="460"
    min-height="320"
    :loading="loading"
    :fullscreen="fullscreen"
    destroy-on-close
    show-zoom
    resize
    transfer
    :show-footer="showFooter"
  >
    <template v-if="slots.header" #header>
      <slot name="header"></slot>
    </template>
    <template v-if="slots.default" #default>
      <slot name="default"></slot>
    </template>
    <template v-if="slots.corner" #corner>
      <slot name="corner"></slot>
    </template>
    <template v-if="slots.footer" #footer>
      <slot name="footer"></slot>
    </template>
  </vxe-modal>
</template>
