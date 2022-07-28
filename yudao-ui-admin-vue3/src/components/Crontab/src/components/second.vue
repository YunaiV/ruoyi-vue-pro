<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElForm, ElFormItem, ElRadio, ElSelect, ElOption, ElInputNumber } from 'element-plus'
const props = defineProps({
  check: {
    type: Function,
    required: true
  },
  cron: {
    type: Object
  }
})
const emits = defineEmits(['update'])

const radioValue = ref(1)
const cycle01 = ref(1)
const cycle02 = ref(2)
const average01 = ref(0)
const average02 = ref(1)
const checkboxList = ref([])

defineExpose({
  radioValue,
  cycle01,
  cycle02,
  average01,
  average02,
  checkboxList
})

const cycleTotal = computed(() => {
  const cycle1 = props.check(cycle01.value, 0, 58)
  const cycle2 = props.check(cycle02.value, cycle1 ? cycle1 + 1 : 1, 59)
  return cycle1 + '-' + cycle2
})

watch(cycleTotal, () => {
  if (parseInt(radioValue.value.toString()) === 2) {
    emits('update', 'second', cycleTotal)
  }
})

const averageTotal = computed(() => {
  const average1 = props.check(average01.value, 0, 58)
  const average2 = props.check(average02.value, 1, 59 - average1 || 0)
  return average1 + '/' + average2
})

watch(averageTotal, () => {
  if (parseInt(radioValue.value.toString()) === 3) {
    emits('update', 'second', averageTotal)
  }
})

const checkboxString = computed(() => {
  let str = checkboxList.value.join()
  return str.length === 0 ? '*' : str
})

watch(checkboxString, () => {
  if (parseInt(radioValue.value.toString()) === 4) {
    emits('update', 'second', checkboxString)
  }
})

watch(radioValue, () => {
  switch (parseInt(radioValue.value.toString())) {
    case 1:
      emits('update', 'second', '*', 'second')
      break
    case 2:
      emits('update', 'second', cycleTotal)
      break
    case 3:
      emits('update', 'second', averageTotal)
      break
    case 4:
      emits('update', 'second', checkboxString)
      break
  }
})
</script>
<template>
  <el-form>
    <el-form-item>
      <el-radio v-model="radioValue" :label="1"> 秒，允许的通配符[, - * /] </el-radio>
    </el-form-item>

    <el-form-item>
      <el-radio v-model="radioValue" :label="2">
        周期从
        <el-input-number v-model="cycle01" :min="0" :max="58" /> -
        <el-input-number v-model="cycle02" :min="cycle01 ? cycle01 + 1 : 1" :max="59" /> 秒
      </el-radio>
    </el-form-item>

    <el-form-item>
      <el-radio v-model="radioValue" :label="3">
        从
        <el-input-number v-model="average01" :min="0" :max="58" /> 秒开始，每
        <el-input-number v-model="average02" :min="1" :max="59 - average01 || 0" /> 秒执行一次
      </el-radio>
    </el-form-item>

    <el-form-item>
      <el-radio v-model="radioValue" :label="4">
        指定
        <el-select
          clearable
          v-model="checkboxList"
          placeholder="可多选"
          multiple
          style="width: 100%"
        >
          <el-option v-for="item in 60" :key="item" :value="item - 1">{{ item - 1 }}</el-option>
        </el-select>
      </el-radio>
    </el-form-item>
  </el-form>
</template>
