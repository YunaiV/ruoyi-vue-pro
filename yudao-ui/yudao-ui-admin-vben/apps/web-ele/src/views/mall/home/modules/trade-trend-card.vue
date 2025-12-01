<script lang="ts" setup>
import type { Dayjs } from 'dayjs';

import type { EchartsUIType } from '@vben/plugins/echarts';

import { onMounted, ref } from 'vue';

import { EchartsUI, useEcharts } from '@vben/plugins/echarts';
import { fenToYuan } from '@vben/utils';

import dayjs from 'dayjs';
import { ElCard, ElRadio, ElRadioGroup } from 'element-plus';

import { getOrderCountTrendComparison } from '#/api/mall/statistics/trade';

import {
  getTradeTrendChartOptions,
  TimeRangeTypeEnum,
} from './trade-trend-chart-options';

/** 交易量趋势 */
defineOptions({ name: 'TradeTrendCard' });

const loading = ref(false);
const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);

const timeRangeConfig = {
  [TimeRangeTypeEnum.DAY30]: {
    name: '30 天',
    seriesCount: 2,
  },
  [TimeRangeTypeEnum.WEEK]: {
    name: '周',
    seriesCount: 4,
  },
  [TimeRangeTypeEnum.MONTH]: {
    name: '月',
    seriesCount: 4,
  },
  [TimeRangeTypeEnum.YEAR]: {
    name: '年',
    seriesCount: 4,
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
  await loadOrderCountTrendComparison(beginTime, endTime);
}

/** 查询订单数量趋势对照数据 */
async function loadOrderCountTrendComparison(beginTime: Dayjs, endTime: Dayjs) {
  loading.value = true;
  try {
    // 1. 查询数据
    const list = await getOrderCountTrendComparison(
      timeRangeType.value,
      beginTime.toDate(),
      endTime.toDate(),
    );
    // 2. 处理数据
    const dates: string[] = [];
    const series: any[] = [];
    const config = timeRangeConfig[timeRangeType.value];
    // 情况一：seriesCount 为 2（近 30 天）
    if (config.seriesCount === 2) {
      const orderPayPriceData: string[] = [];
      const orderPayCountData: number[] = [];
      for (const item of list) {
        dates.push(item.value.date);
        orderPayPriceData.push(fenToYuan(item?.value?.orderPayPrice || 0));
        orderPayCountData.push(item?.value?.orderPayCount || 0);
      }
      series.push(
        {
          name: '订单金额',
          type: 'bar',
          smooth: true,
          data: orderPayPriceData,
        },
        {
          name: '订单数量',
          type: 'line',
          smooth: true,
          data: orderPayCountData,
        },
      );
    } else {
      // 情况二：seriesCount 为 4
      const refPriceData: string[] = [];
      const curPriceData: string[] = [];
      const refCountData: number[] = [];
      const curCountData: number[] = [];
      for (const item of list) {
        dates.push(item.value.date);
        refPriceData.push(fenToYuan(item?.reference?.orderPayPrice || 0));
        curPriceData.push(fenToYuan(item?.value?.orderPayPrice || 0));
        refCountData.push(item?.reference?.orderPayCount || 0);
        curCountData.push(item?.value?.orderPayCount || 0);
      }
      // 根据时间范围类型确定对照数据的标签文本
      let timeLabel: string[];
      if (timeRangeType.value === TimeRangeTypeEnum.WEEK) {
        timeLabel = ['上周', '本周'];
      } else if (timeRangeType.value === TimeRangeTypeEnum.MONTH) {
        timeLabel = ['上月', '本月'];
      } else {
        timeLabel = ['去年', '今年'];
      }
      series.push(
        {
          name: `${timeLabel[0]}金额`,
          type: 'bar',
          smooth: true,
          data: refPriceData,
        },
        {
          name: `${timeLabel[1]}金额`,
          type: 'bar',
          smooth: true,
          data: curPriceData,
        },
        {
          name: `${timeLabel[0]}数量`,
          type: 'line',
          smooth: true,
          data: refCountData,
        },
        {
          name: `${timeLabel[1]}数量`,
          type: 'line',
          smooth: true,
          data: curCountData,
        },
      );
    }

    // 3. 渲染 Echarts 界面
    await renderEcharts(
      getTradeTrendChartOptions(dates, series, timeRangeType.value),
    );
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
        <span>交易量趋势</span>
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
      <EchartsUI ref="chartRef" class="w-full" />
    </div>
  </ElCard>
</template>
