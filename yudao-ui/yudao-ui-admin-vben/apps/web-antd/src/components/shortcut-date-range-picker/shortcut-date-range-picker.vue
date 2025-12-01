<script lang="ts" setup>
import type { Dayjs } from 'dayjs';

import { onMounted, ref } from 'vue';

import { DatePicker, Radio, RadioGroup } from 'ant-design-vue';

import { getRangePickerDefaultProps } from '#/utils/rangePickerProps';

/** 快捷日期范围选择组件 */
defineOptions({ name: 'ShortcutDateRangePicker' });

const emits = defineEmits<{
  change: [times: [Dayjs, Dayjs]];
}>();

const times = ref<[Dayjs, Dayjs]>(); // 日期范围

const rangePickerProps = getRangePickerDefaultProps();
const timeRangeOptions = [
  rangePickerProps.presets[3]!, // 昨天
  rangePickerProps.presets[1]!, // 最近 7 天
  rangePickerProps.presets[2]!, // 最近 30 天
];
const timeRangeType = ref(timeRangeOptions[1]!.label); // 默认选中第一个选项

/** 设置时间范围 */
function setTimes() {
  // 根据选中的选项设置时间范围
  const selectedOption = timeRangeOptions.find(
    (option) => option.label === timeRangeType.value,
  );
  if (selectedOption) {
    times.value = selectedOption.value as [Dayjs, Dayjs];
  }
}

/** 快捷日期单选按钮选中 */
async function handleShortcutDaysChange() {
  // 设置时间范围
  setTimes();
  // 触发时间范围选中事件
  emitDateRangePicker();
}

/** 日期范围改变 */
function handleDateRangeChange() {
  emitDateRangePicker();
}

/** 触发时间范围选中事件 */
function emitDateRangePicker() {
  if (times.value && times.value.length === 2) {
    emits('change', times.value);
  }
}

/** 初始化 */
onMounted(() => {
  handleShortcutDaysChange();
});
</script>

<template>
  <div class="flex items-center gap-2">
    <RadioGroup
      v-model:value="timeRangeType"
      @change="handleShortcutDaysChange"
    >
      <Radio
        v-for="option in timeRangeOptions"
        :key="option.label"
        :value="option.label"
      >
        {{ option.label }}
      </Radio>
    </RadioGroup>
    <DatePicker.RangePicker
      v-model:value="times"
      :format="rangePickerProps.format"
      :value-format="rangePickerProps.valueFormat"
      :placeholder="rangePickerProps.placeholder"
      :presets="rangePickerProps.presets"
      class="!w-full !max-w-96"
      @change="handleDateRangeChange"
    />
    <slot></slot>
  </div>
</template>
