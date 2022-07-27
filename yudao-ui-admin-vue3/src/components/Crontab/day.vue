<template>
  <el-form size="small">
    <el-form-item>
      <el-radio v-model="radioValue" :label="1"> 日，允许的通配符[, - * ? / L W] </el-radio>
    </el-form-item>

    <el-form-item>
      <el-radio v-model="radioValue" :label="2"> 不指定 </el-radio>
    </el-form-item>

    <el-form-item>
      <el-radio v-model="radioValue" :label="3">
        周期从
        <el-input-number v-model="cycle01" :min="1" :max="30" /> -
        <el-input-number v-model="cycle02" :min="cycle01 ? cycle01 + 1 : 2" :max="31" /> 日
      </el-radio>
    </el-form-item>

    <el-form-item>
      <el-radio v-model="radioValue" :label="4">
        从
        <el-input-number v-model="average01" :min="1" :max="30" /> 号开始，每
        <el-input-number v-model="average02" :min="1" :max="31 - average01 || 1" /> 日执行一次
      </el-radio>
    </el-form-item>

    <el-form-item>
      <el-radio v-model="radioValue" :label="5">
        每月
        <el-input-number v-model="workday" :min="1" :max="31" /> 号最近的那个工作日
      </el-radio>
    </el-form-item>

    <el-form-item>
      <el-radio v-model="radioValue" :label="6"> 本月最后一天 </el-radio>
    </el-form-item>

    <el-form-item>
      <el-radio v-model="radioValue" :label="7">
        指定
        <el-select
          clearable
          v-model="checkboxList"
          placeholder="可多选"
          multiple
          style="width: 100%"
        >
          <el-option v-for="item in 31" :key="item" :value="item">{{ item }}</el-option>
        </el-select>
      </el-radio>
    </el-form-item>
  </el-form>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue'

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
const workday = ref(1)
const cycle01 = ref(1)
const cycle02 = ref(2)
const average01 = ref(1)
const average02 = ref(1)

const checkboxList = ref([])

defineExpose({
  radioValue,
  workday,
  cycle01,
  cycle02,
  average01,
  average02,
  checkboxList
})

/**
 * 计算两个周期值
 * @type {ComputedRef<string>}
 */
const cycleTotal = computed(() => {
  const cycle1 = props.check(cycle01.value, 1, 30)
  const cycle2 = props.check(cycle02.value, cycle1 ? cycle1 + 1 : 2, 31, 31)
  return cycle1 + '-' + cycle2
})

/**
 * 计算平均用到的值
 */
const averageTotal = computed(() => {
  const average1 = props.check(average01.value, 1, 30)
  const average2 = props.check(average02.value, 1, 31 - average1 || 0)
  return average1 + '/' + average2
})

/**
 * 计算工作日格式
 */
const workdayCheck = computed(() => props.check(workday, 1, 31))

/**
 * 计算勾选的checkbox值合集
 */
const checkboxString = computed(() => {
  const str = checkboxList.value.join()
  return str.length === 0 ? '*' : str
})

watch(radioValue, () => {
  if (parseInt(radioValue.value.toString()) !== 2 && props.cron?.week !== '?') {
    emits('update', 'week', '?', 'day')
  }

  switch (parseInt(radioValue.value.toString())) {
    case 1:
      emits('update', 'day', '*')
      break
    case 2:
      emits('update', 'day', '?')
      break
    case 3:
      emits('update', 'day', cycleTotal)
      break
    case 4:
      emits('update', 'day', averageTotal)
      break
    case 5:
      emits('update', 'day', workday.value + 'W')
      break
    case 6:
      emits('update', 'day', 'L')
      break
    case 7:
      emits('update', 'day', checkboxString)
      break
  }
})

watch(cycleTotal, () => {
  if (parseInt(radioValue.value.toString()) === 3) {
    emits('update', 'day', cycleTotal)
  }
})

watch(averageTotal, () => {
  if (parseInt(radioValue.value.toString()) === 4) {
    emits('update', 'day', averageTotal)
  }
})

watch(workdayCheck, () => {
  if (parseInt(radioValue.value.toString()) === 5) {
    emits('update', 'day', workdayCheck.value + 'W')
  }
})

watch(checkboxString, () => {
  if (parseInt(radioValue.value.toString()) === 7) {
    emits('update', 'day', checkboxString)
  }
})
</script>
