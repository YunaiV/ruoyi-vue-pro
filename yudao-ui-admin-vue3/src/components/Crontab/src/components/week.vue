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

const radioValue = ref<number>(2)
const weekday = ref<number>(2)
const cycle01 = ref<number>(2)
const cycle02 = ref<number>(3)
const average01 = ref<number>(1)
const average02 = ref<number>(2)
const checkboxList = ref([])
const weekList = ref([
  {
    key: 2,
    value: '星期一'
  },
  {
    key: 3,
    value: '星期二'
  },
  {
    key: 4,
    value: '星期三'
  },
  {
    key: 5,
    value: '星期四'
  },
  {
    key: 6,
    value: '星期五'
  },
  {
    key: 7,
    value: '星期六'
  },
  {
    key: 1,
    value: '星期日'
  }
])

defineExpose({
  radioValue,
  weekday,
  cycle01,
  cycle02,
  average01,
  average02,
  checkboxList
})

const cycleTotal = computed(() => {
  const cycle1 = props.check(cycle01.value, 1, 7)
  const cycle2 = props.check(cycle02.value, 1, 7)
  return cycle1 + '-' + cycle2
})

watch(cycleTotal, () => {
  if (parseInt(radioValue.value.toString()) === 3) {
    emits('update', 'month', cycleTotal)
  }
})

const averageTotal = computed(() => {
  const average1 = props.check(average01.value, 1, 4)
  const average2 = props.check(average02.value, 1, 7)
  return average1 + '#' + average2
})

watch(averageTotal, () => {
  if (parseInt(radioValue.value.toString()) === 4) {
    emits('update', 'month', averageTotal)
  }
})

const weekdayCheck = computed(() => {
  return props.check(weekday.value, 1, 7)
})

watch(weekdayCheck, () => {
  if (parseInt(radioValue.value.toString()) === 5) {
    emits('update', 'week', weekday.value + 'L')
  }
})

const checkboxString = computed(() => {
  let str = checkboxList.value.join()
  return str.length === 0 ? '*' : str
})

watch(checkboxString, () => {
  if (parseInt(radioValue.value.toString()) === 6) {
    emits('update', 'month', checkboxString)
  }
})

watch(radioValue, () => {
  if (parseInt(radioValue.value.toString()) !== 2 && radioValue.value.toString() !== '?') {
    emits('update', 'day', '?', 'week')
  }
  switch (parseInt(radioValue.value.toString())) {
    case 1:
      emits('update', 'week', '*')
      break
    case 2:
      emits('update', 'week', '?')
      break
    case 3:
      emits('update', 'week', cycleTotal)
      break
    case 4:
      emits('update', 'week', averageTotal)
      break
    case 5:
      emits('update', 'week', weekdayCheck.value + 'L')
      break
    case 6:
      emits('update', 'week', checkboxString)
      break
  }
})
</script>
<template>
  <el-form>
    <el-form-item>
      <el-radio v-model="radioValue" :label="1"> 周，允许的通配符[, - * ? / L #] </el-radio>
    </el-form-item>

    <el-form-item>
      <el-radio v-model="radioValue" :label="2"> 不指定 </el-radio>
    </el-form-item>

    <el-form-item>
      <el-radio v-model="radioValue" :label="3">
        周期从星期
        <el-select clearable v-model="cycle01">
          <el-option
            v-for="(item, index) of weekList"
            :key="index"
            :label="item.value"
            :value="item.key"
            :disabled="item.key === 1"
            >{{ item.value }}</el-option
          >
        </el-select>
        -
        <el-select clearable v-model="cycle02">
          <el-option
            v-for="(item, index) of weekList"
            :key="index"
            :label="item.value"
            :value="item.key"
            :disabled="item.key < cycle01 && item.key !== 1"
            >{{ item.value }}</el-option
          >
        </el-select>
      </el-radio>
    </el-form-item>

    <el-form-item>
      <el-radio v-model="radioValue" :label="4">
        第
        <el-input-number v-model="average01" :min="1" :max="4" /> 周的星期
        <el-select clearable v-model="average02">
          <el-option
            v-for="(item, index) of weekList"
            :key="index"
            :label="item.value"
            :value="item.key"
            >{{ item.value }}</el-option
          >
        </el-select>
      </el-radio>
    </el-form-item>

    <el-form-item>
      <el-radio v-model="radioValue" :label="5">
        本月最后一个星期
        <el-select clearable v-model="weekday">
          <el-option
            v-for="(item, index) of weekList"
            :key="index"
            :label="item.value"
            :value="item.key"
            >{{ item.value }}</el-option
          >
        </el-select>
      </el-radio>
    </el-form-item>

    <el-form-item>
      <el-radio v-model="radioValue" :label="6">
        指定
        <el-select
          clearable
          v-model="checkboxList"
          placeholder="可多选"
          multiple
          style="width: 100%"
        >
          <el-option
            v-for="(item, index) of weekList"
            :key="index"
            :label="item.value"
            :value="String(item.key)"
            >{{ item.value }}</el-option
          >
        </el-select>
      </el-radio>
    </el-form-item>
  </el-form>
</template>
