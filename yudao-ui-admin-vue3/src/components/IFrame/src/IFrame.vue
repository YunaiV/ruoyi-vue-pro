<script setup lang="ts">
import { propTypes } from '@/utils/propTypes'
import { ref, onMounted } from 'vue'
const props = defineProps({
  src: propTypes.string.def('')
})
const loading = ref(true)
const frameSrc = ref<string>('')
const height = ref('')
const frameRef = ref<HTMLElement | null>(null)
const init = () => {
  frameSrc.value = props.src
  height.value = document.documentElement.clientHeight - 94.5 + 'px'
  loading.value = false
}
onMounted(() => {
  setTimeout(() => {
    init()
  }, 300)
})
</script>
<template>
  <div v-loading="loading" :style="'height:' + height">
    <iframe
      :src="frameSrc"
      style="width: 100%; height: 100%"
      frameborder="no"
      scrolling="auto"
      ref="frameRef"
    ></iframe>
  </div>
</template>
<script lang="less" scoped></script>
