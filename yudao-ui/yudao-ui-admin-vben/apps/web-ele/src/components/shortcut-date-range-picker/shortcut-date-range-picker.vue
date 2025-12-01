<script lang="ts" setup>
import type { Dayjs } from 'dayjs';

import { onMounted, ref } from 'vue';

import dayjs from 'dayjs';
import { ElDatePicker, ElRadio, ElRadioGroup } from 'element-plus';

import { getRangePickerDefaultProps } from '#/utils/rangePickerProps';

/** 快捷日期范围选择组件 */
defineOptions({ name: 'ShortcutDateRangePicker' });

const emits = defineEmits<{
  change: [times: [Dayjs, Dayjs]];
}>();

const times = ref<[Dayjs, Dayjs]>(); // 日期范围

const rangePickerProps = getRangePickerDefaultProps();

const timeRangeOptions = [
  {
    label: '昨天',
    value: () => [
      dayjs().subtract(1, 'day').startOf('day'),
      dayjs().subtract(1, 'day').endOf('day'),
    ],
  },
  {
    label: '最近 7 天',
    value: () => [
      dayjs().subtract(7, 'day').startOf('day'),
      dayjs().endOf('day'),
    ],
  },
  {
    label: '最近 30 天',
    value: () => [
      dayjs().subtract(30, 'day').startOf('day'),
      dayjs().endOf('day'),
    ],
  },
];

const timeRangeType = ref(timeRangeOptions[1]!.label); // 默认选中"最近 7 天"

/** 设置时间范围 */
function setTimes() {
  // 根据选中的选项设置时间范围
  const selectedOption = timeRangeOptions.find(
    (option) => option.label === timeRangeType.value,
  );
  if (selectedOption) {
    times.value = selectedOption.value() as [Dayjs, Dayjs];
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
    <ElRadioGroup v-model="timeRangeType" @change="handleShortcutDaysChange">
      <ElRadio
        v-for="option in timeRangeOptions"
        :key="option.label"
        :value="option.label"
      >
        {{ option.label }}
      </ElRadio>
    </ElRadioGroup>
    <ElDatePicker
      v-model="times as any"
      type="daterange"
      :shortcuts="rangePickerProps.shortcuts"
      :format="rangePickerProps.format"
      :value-format="rangePickerProps.valueFormat"
      :start-placeholder="rangePickerProps.startPlaceholder"
      :end-placeholder="rangePickerProps.endPlaceholder"
      :default-time="rangePickerProps.defaultTime as any"
      class="!w-[215px]"
      @change="handleDateRangeChange"
    />
    <slot></slot>
  </div>
</template>
