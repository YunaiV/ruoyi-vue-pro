<script setup lang="ts">
import { onMounted, onUpdated, PropType, ref } from 'vue'
import { getDictOptions, DictDataType } from '@/utils/dict'
import { ElTag } from 'element-plus'
const props = defineProps({
  type: {
    type: String as PropType<string>,
    required: true
  },
  value: {
    type: [String, Number, Boolean] as PropType<string | number | boolean>,
    required: true
  }
})
const dictData = ref<DictDataType>()
const getDictObj = (dictType: string, value: string) => {
  const dictOptions = getDictOptions(dictType)
  dictOptions.forEach((dict: DictDataType) => {
    if (dict.value === value) {
      dictData.value = dict
    }
  })
}

onMounted(() => {
  return getDictObj(props.type, props.value?.toString())
})

onUpdated(() => {
  getDictObj(props.type, props.value?.toString())
})
</script>
<template>
  <!-- 默认样式 -->
  <span
    v-if="
      dictData?.colorType === 'default' ||
      dictData?.colorType === '' ||
      dictData?.colorType === undefined
    "
    :key="dictData?.value.toString()"
    :class="dictData?.cssClass"
  >
    {{ dictData?.label }}
  </span>
  <!-- Tag 样式 -->
  <ElTag
    v-else
    :disable-transitions="true"
    :key="dictData?.value + ''"
    :type="dictData?.colorType === 'primary' ? 'success' : dictData?.colorType"
    :class="dictData?.cssClass"
  >
    {{ dictData?.label }}
  </ElTag>
</template>
