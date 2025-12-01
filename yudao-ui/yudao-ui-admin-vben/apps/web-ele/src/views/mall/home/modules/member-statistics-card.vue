<script lang="ts" setup>
import type { Dayjs } from 'dayjs';

import type { EchartsUIType } from '@vben/plugins/echarts';

import { onMounted, ref } from 'vue';

import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import dayjs from 'dayjs';
import { ElCard, ElRadio, ElRadioGroup } from 'element-plus';

import { getMemberRegisterCountList } from '#/api/mall/statistics/member';

import {
  getMemberStatisticsChartOptions,
  TimeRangeTypeEnum,
} from './member-statistics-chart-options';

/** 会员用户统计卡片 */
defineOptions({ name: 'MemberStatisticsCard' });

const loading = ref(false);
const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);

const timeRangeConfig = {
  [TimeRangeTypeEnum.DAY30]: {
    name: '30 天',
  },
  [TimeRangeTypeEnum.WEEK]: {
    name: '周',
  },
  [TimeRangeTypeEnum.MONTH]: {
    name: '月',
  },
  [TimeRangeTypeEnum.YEAR]: {
    name: '年',
  },
}; // 时间范围 Map
const timeRangeType = ref(TimeRangeTypeEnum.DAY30); // 日期快捷选择按钮, 默认 30 天

/** 时间范围类型单选按钮选中 */
async function handleTimeRangeTypeChange() {
  // 设置时间范围
  let beginTime: Dayjs;
  let endTime: Dayjs;
  switch (timeRangeType.value) {
    case TimeRangeTypeEnum.DAY30: {
      beginTime = dayjs().subtract(30, 'day').startOf('d');
      endTime = dayjs().endOf('d');
      break;
    }
    case TimeRangeTypeEnum.MONTH: {
      beginTime = dayjs().startOf('month');
      endTime = dayjs().endOf('month');
      break;
    }
    case TimeRangeTypeEnum.WEEK: {
      beginTime = dayjs().startOf('week');
      endTime = dayjs().endOf('week');
      break;
    }
    case TimeRangeTypeEnum.YEAR: {
      beginTime = dayjs().startOf('year');
      endTime = dayjs().endOf('year');
      break;
    }
    default: {
      throw new Error(`未知的时间范围类型: ${timeRangeType.value}`);
    }
  }
  // 发送时间范围选中事件
  await loadMemberRegisterCountList(beginTime, endTime);
}

async function loadMemberRegisterCountList(beginTime: Dayjs, endTime: Dayjs) {
  loading.value = true;
  try {
    const list = await getMemberRegisterCountList(
      beginTime.toDate(),
      endTime.toDate(),
    );
    // 更新 Echarts 数据
    await renderEcharts(getMemberStatisticsChartOptions(list));
  } finally {
    loading.value = false;
  }
}

/** 初始化 */
onMounted(() => {
  handleTimeRangeTypeChange();
});
</script>

<template>
  <ElCard :border="false">
    <template #header>
      <div class="flex items-center justify-between">
        <span>用户统计</span>
        <ElRadioGroup
          v-model="timeRangeType"
          @change="handleTimeRangeTypeChange"
        >
          <ElRadio
            v-for="[key, value] in Object.entries(timeRangeConfig)"
            :key="key"
            :value="Number(key)"
          >
            {{ value.name }}
          </ElRadio>
        </ElRadioGroup>
      </div>
    </template>
    <div v-loading="loading">
      <EchartsUI ref="chartRef" class="h-[300px] w-full" />
    </div>
  </ElCard>
</template>
