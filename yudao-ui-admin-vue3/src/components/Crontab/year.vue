<template>
  <el-form size="small">
    <el-form-item>
      <el-radio label="1" v-model="radioValue"> 不填，允许的通配符[, - * /] </el-radio>
    </el-form-item>

    <el-form-item>
      <el-radio label="2" v-model="radioValue"> 每年 </el-radio>
    </el-form-item>

    <el-form-item>
      <el-radio label="3" v-model="radioValue">
        周期从
        <el-input-number v-model="cycle01" :min="fullYear" :max="2098" /> -
        <el-input-number
          v-model="cycle02"
          :min="cycle01 ? cycle01 + 1 : fullYear + 1"
          :max="2099"
        />
      </el-radio>
    </el-form-item>

    <el-form-item>
      <el-radio label="4" v-model="radioValue">
        从
        <el-input-number v-model="average01" :min="fullYear" :max="2098" /> 年开始，每
        <el-input-number v-model="average02" :min="1" :max="2099 - average01 || fullYear" />
        年执行一次
      </el-radio>
    </el-form-item>

    <el-form-item>
      <el-radio label="5" v-model="radioValue">
        指定
        <el-select clearable v-model="checkboxList" placeholder="可多选" multiple>
          <el-option
            v-for="item in 9"
            :key="item"
            :value="item - 1 + fullYear"
            :label="item - 1 + fullYear"
          />
        </el-select>
      </el-radio>
    </el-form-item>
  </el-form>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue'

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

const fullYear = ref<number>(0)
const radioValue = ref<number>(1)
const cycle01 = ref<number>(0)
const cycle02 = ref<number>(0)
const average01 = ref<number>(0)
const average02 = ref<number>(1)
const checkboxList = ref([])

defineExpose({
  fullYear,
  radioValue,
  cycle01,
  cycle02,
  average01,
  average02,
  checkboxList
})

const cycleTotal = computed(() => {
  const cycle1 = props.check(cycle01.value, fullYear.value, 2098)
  const cycle2 = props.check(cycle02.value, cycle1 ? cycle1 + 1 : fullYear.value, 2099)
  return cycle1 + '-' + cycle2
})

watch(cycleTotal, () => {
  if (parseInt(radioValue.value.toString()) === 3) {
    emits('update', 'year', cycleTotal)
  }
})

const averageTotal = computed(() => {
  const average1 = props.check(average01.value, fullYear.value, 2098)
  const average2 = props.check(average02.value, 1, 2099 - average1 || fullYear.value)
  return average1 + '/' + average2
})

watch(averageTotal, () => {
  if (parseInt(radioValue.value.toString()) === 4) {
    emits('update', 'year', averageTotal)
  }
})

const checkboxString = computed(() => {
  let str = checkboxList.value.join()
  return str.length === 0 ? '*' : str
})

watch(checkboxString, () => {
  if (parseInt(radioValue.value.toString()) === 5) {
    emits('update', 'year', checkboxString)
  }
})

watch(radioValue, () => {
  switch (parseInt(radioValue.value.toString())) {
    case 1:
      emits('update', 'year', '')
      break
    case 2:
      emits('update', 'year', '*')
      break
    case 3:
      emits('update', 'year', cycleTotal, 'min')
      break
    case 4:
      emits('update', 'year', averageTotal, 'min')
      break
    case 5:
      emits('update', 'year', checkboxString, 'min')
      break
  }
})

onMounted(() => {
  fullYear.value = Number(new Date().getFullYear())
  cycle01.value = fullYear.value
  average01.value = fullYear.value
})
</script>
