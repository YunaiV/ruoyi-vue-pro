<script setup lang="ts">
import { onMounted, onUpdated, PropType, ref } from 'vue'
import { getDictOptions, DictDataType } from '@/utils/dict'
import { isHexColor } from '@/utils/color'
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
      if (dict.colorType + '' === 'primary' || dict.colorType + '' === 'default') {
        dict.colorType = ''
      }
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
  <ElTag
    :disable-transitions="true"
    :key="dictData?.value + ''"
    :type="dictData?.colorType"
    :color="dictData?.cssClass && isHexColor(dictData?.cssClass) ? dictData?.cssClass : ''"
  >
    {{ dictData?.label }}
  </ElTag>
</template>
