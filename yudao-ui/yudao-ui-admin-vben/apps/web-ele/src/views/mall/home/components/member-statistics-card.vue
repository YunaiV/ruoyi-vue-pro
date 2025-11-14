<script lang="ts" setup>
import type { EchartsUIType } from '@vben/plugins/echarts';

import { onMounted, reactive, ref } from 'vue';

import { AnalysisChartCard } from '@vben/common-ui';
import { EchartsUI, useEcharts } from '@vben/plugins/echarts';
import { formatDate } from '@vben/utils';

import dayjs from 'dayjs';

import * as MemberStatisticsApi from '#/api/mall/statistics/member';

/** 会员用户统计卡片 */
defineOptions({ name: 'MemberStatisticsCard' });

const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);

const loading = ref(true); // 加载中
/** 折线图配置 */
const lineChartOptions = reactive({
  dataset: {
    dimensions: ['date', 'count'],
    source: [] as any[],
  },
  grid: {
    left: 20,
    right: 20,
    bottom: 20,
    top: 80,
    containLabel: true,
  },
  legend: {
    top: 50,
  },
  series: [{ name: '注册量', type: 'line', smooth: true, areaStyle: {} }],
  toolbox: {
    feature: {
      // 数据区域缩放
      dataZoom: {
        yAxisIndex: false, // Y轴不缩放
      },
      brush: {
        type: ['lineX', 'clear'], // 区域缩放按钮、还原按钮
      },
      saveAsImage: { show: true, name: '会员统计' }, // 保存为图片
    },
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross',
    },
    padding: [5, 10],
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    axisTick: {
      show: false,
    },
    axisLabel: {
      formatter: (date: string) => formatDate(date, 'MM-DD'),
    },
  },
  yAxis: {
    axisTick: {
      show: false,
    },
  },
});

const getMemberRegisterCountList = async () => {
  loading.value = true;
  // 查询最近一月数据
  const beginTime = dayjs().subtract(30, 'd').startOf('d');
  const endTime = dayjs().endOf('d');
  const list = await MemberStatisticsApi.getMemberRegisterCountList(
    beginTime.toDate(),
    endTime.toDate(),
  );
  // 更新 Echarts 数据
  if (lineChartOptions.dataset && lineChartOptions.dataset.source) {
    lineChartOptions.dataset.source = list;
  }
  loading.value = false;
};

/** 初始化 */
onMounted(async () => {
  await getMemberRegisterCountList();
  renderEcharts(lineChartOptions as any);
});
</script>
<template>
  <AnalysisChartCard title="用户统计">
    <!-- 折线图 -->
    <EchartsUI ref="chartRef" />
  </AnalysisChartCard>
</template>
