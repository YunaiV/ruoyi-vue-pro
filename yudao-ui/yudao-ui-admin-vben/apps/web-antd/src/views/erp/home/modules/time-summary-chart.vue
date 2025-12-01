<script lang="ts" setup>
import type { EchartsUIType } from '@vben/plugins/echarts';

import type { ErpPurchaseStatisticsApi } from '#/api/erp/statistics/purchase';
import type { ErpSaleStatisticsApi } from '#/api/erp/statistics/sale';

import { onMounted, ref, watch } from 'vue';

import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import { Card } from 'ant-design-vue';

import {
  getPurchaseSummary,
  getPurchaseTimeSummary,
} from '#/api/erp/statistics/purchase';
import { getSaleSummary, getSaleTimeSummary } from '#/api/erp/statistics/sale';

interface Props {
  title: string;
  type?: 'purchase' | 'sale';
}

const props = withDefaults(defineProps<Props>(), {
  type: 'sale',
});

/** 销售统计数据 */
const saleSummary = ref<ErpSaleStatisticsApi.SaleSummaryRespVO>(); // 销售概况统计
const saleTimeSummaryList = ref<ErpSaleStatisticsApi.SaleTimeSummaryRespVO[]>(); // 销售时段统计
const getSaleStatistics = async () => {
  saleSummary.value = await getSaleSummary();
  saleTimeSummaryList.value = await getSaleTimeSummary();
};

/** 采购统计数据 */
const purchaseSummary = ref<ErpPurchaseStatisticsApi.PurchaseSummaryRespVO>(); // 采购概况统计
const purchaseTimeSummaryList =
  ref<ErpPurchaseStatisticsApi.PurchaseTimeSummaryRespVO[]>(); // 采购时段统计
const getPurchaseStatistics = async () => {
  purchaseSummary.value = await getPurchaseSummary();
  purchaseTimeSummaryList.value = await getPurchaseTimeSummary();
};

/** 获取当前类型的时段数据 */
const currentTimeSummaryList = ref<Array<{ price: number; time: string }>>();

const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);

/** 折线图配置 */
const lineChartOptions: echarts.EChartsOption = {
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
  series: [
    {
      name: '金额',
      type: 'line',
      smooth: true,
      areaStyle: {},
      data: [],
    },
  ],
  toolbox: {
    feature: {
      dataZoom: {
        yAxisIndex: false,
      },
      brush: {
        type: ['lineX', 'clear'],
      },
      saveAsImage: {
        show: true,
        name: props.title,
      },
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
    data: [],
  },
  yAxis: {
    axisTick: {
      show: false,
    },
  },
};

/** 初始化数据 */
const initData = async () => {
  if (props.type === 'sale') {
    await getSaleStatistics();
    currentTimeSummaryList.value = saleTimeSummaryList.value;
  } else {
    await getPurchaseStatistics();
    currentTimeSummaryList.value = purchaseTimeSummaryList.value;
  }
};

/** 监听数据变化并更新图表 */
watch(
  () => currentTimeSummaryList.value,
  (val) => {
    if (!val || val.length === 0) {
      return;
    }
    // 更新图表数据
    const xAxisData = val.map((item) => item.time);
    const seriesData = val.map((item) => item.price);
    const options = {
      ...lineChartOptions,
      xAxis: {
        ...lineChartOptions.xAxis,
        data: xAxisData,
      },
      series: [
        {
          ...(lineChartOptions.series as any)[0],
          data: seriesData,
        },
      ],
    };
    renderEcharts(options);
  },
  { immediate: true },
);

/** 组件挂载时初始化数据 */
onMounted(() => {
  initData();
});
</script>

<template>
  <Card>
    <template #title>
      <span>{{ title }}</span>
    </template>
    <!-- 折线图 -->
    <EchartsUI ref="chartRef" />
  </Card>
</template>
