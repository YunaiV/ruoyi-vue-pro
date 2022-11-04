<script setup lang="ts">
import { propTypes } from '@/utils/propTypes'
import { computed, useAttrs, PropType } from 'vue'

const props = defineProps({
  modelValue: propTypes.bool.def(false),
  loading: propTypes.bool.def(false),
  preIcon: propTypes.string.def(''),
  postIcon: propTypes.string.def(''),
  title: propTypes.string.def('按钮'),
  type: propTypes.oneOf(['', 'primary', 'success', 'warning', 'danger', 'info']).def(''),
  link: propTypes.bool.def(false),
  circle: propTypes.bool.def(false),
  round: propTypes.bool.def(false),
  plain: propTypes.bool.def(false),
  onClick: { type: Function as PropType<(...args) => any>, default: null }
})
const getBindValue = computed(() => {
  const delArr: string[] = ['title', 'preIcon', 'postIcon', 'onClick']
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
  <el-button v-bind="getBindValue" @click="onClick">
    <Icon :icon="preIcon" v-if="preIcon" class="mr-1px" />
    {{ title }}
    <Icon :icon="postIcon" v-if="postIcon" class="mr-1px" />
  </el-button>
</template>
