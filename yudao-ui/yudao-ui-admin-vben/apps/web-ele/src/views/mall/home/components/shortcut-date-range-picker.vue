<script lang="ts" setup>
import { onMounted, ref } from 'vue';

import {
  getDateRange,
  getDayRange,
  getLast1Year,
  getLast7Days,
  getLast30Days,
} from '@vben/utils';

import dayjs from 'dayjs';

/** 快捷日期范围选择组件 */
defineOptions({ name: 'ShortcutDateRangePicker' });

/** 触发事件：时间范围选中 */
const emits = defineEmits<{
  (e: 'change', times: [dayjs.ConfigType, dayjs.ConfigType]): void;
}>();
const shortcutDays = ref(7); // 日期快捷天数（单选按钮组）, 默认7天
const times = ref<[string, string]>(['', '']); // 时间范围参数
defineExpose({ times }); // 暴露时间范围参数
/** 日期快捷选择 */
const shortcuts = [
  {
    text: '昨天',
    value: () => getDayRange(new Date(), -1),
  },
  {
    text: '最近7天',
    value: () => getLast7Days(),
  },
  {
    text: '本月',
    value: () => [dayjs().startOf('M'), dayjs().subtract(1, 'd')],
  },
  {
    text: '最近30天',
    value: () => getLast30Days(),
  },
  {
    text: '最近1年',
    value: () => getLast1Year(),
  },
];

/** 设置时间范围 */
function setTimes() {
  const beginDate = dayjs().subtract(shortcutDays.value, 'd');
  const yesterday = dayjs().subtract(1, 'd');
  times.value = getDateRange(beginDate, yesterday);
}

/** 快捷日期单选按钮选中 */
const handleShortcutDaysChange = async () => {
  // 设置时间范围
  setTimes();
  // 发送时间范围选中事件
  await emitDateRangePicker();
};

/** 触发时间范围选中事件 */
const emitDateRangePicker = async () => {
  emits('change', times.value);
};

/** 初始化 */
onMounted(() => {
  handleShortcutDaysChange();
});
</script>
<template>
  <div class="flex flex-row items-center gap-2">
    <el-radio-group v-model="shortcutDays" @change="handleShortcutDaysChange">
      <el-radio-button :value="1">昨天</el-radio-button>
      <el-radio-button :value="7">最近7天</el-radio-button>
      <el-radio-button :value="30">最近30天</el-radio-button>
    </el-radio-group>
    <el-date-picker
      v-model="times"
      value-format="YYYY-MM-DD HH:mm:ss"
      type="daterange"
      start-placeholder="开始日期"
      end-placeholder="结束日期"
      :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
      :shortcuts="shortcuts"
      class="!w-240px"
      @change="emitDateRangePicker"
    />
    <slot></slot>
  </div>
</template>
