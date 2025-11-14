<script lang="ts" setup>
import type { EchartsUIType } from '@vben/plugins/echarts';

import type { MallDataComparisonResp } from '#/api/mall/statistics/common';
import type { MallTradeStatisticsApi } from '#/api/mall/statistics/trade';
import type { AnalysisOverviewIconItem } from '#/views/mall/home/components/data';

import { reactive, ref } from 'vue';

import { confirm } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { EchartsUI, useEcharts } from '@vben/plugins/echarts';
import {
  calculateRelativeRate,
  downloadFileFromBlobPart,
  fenToYuan,
  formatDate,
  isSameDay,
} from '@vben/utils';

import dayjs from 'dayjs';

import * as TradeStatisticsApi from '#/api/mall/statistics/trade';
import AnalysisChartCard from '#/views/mall/home/components/analysis-chart-card.vue';
import AnalysisOverviewIcon from '#/views/mall/home/components/analysis-overview-icon.vue';
import ShortcutDateRangePicker from '#/views/mall/home/components/shortcut-date-range-picker.vue';

const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);
const overviewItems = ref<AnalysisOverviewIconItem[]>();
const summary =
  ref<MallDataComparisonResp<MallTradeStatisticsApi.TradeTrendSummary>>();
const shortcutDateRangePicker = ref();
const exportLoading = ref(false); // 导出的加载中
const trendLoading = ref(true); // 交易状态加载中
const loadOverview = () => {
  overviewItems.value = [
    {
      icon: 'fa-solid:yen-sign',
      title: '营业额',
      value: Number(fenToYuan(summary?.value?.value.turnoverPrice || 0)),
      tooltip: '商品支付金额、充值金额',
      iconColor: 'bg-blue-100',
      iconBgColor: 'text-blue-500',
      prefix: '￥',
      decimals: 2,
      percent: calculateRelativeRate(
        summary?.value?.value?.turnoverPrice,
        summary?.value?.reference?.turnoverPrice,
      ),
    },
    {
      icon: 'fa-solid:shopping-cart',
      title: '商品支付金额',
      value: Number(fenToYuan(summary.value?.value?.orderPayPrice || 0)),
      tooltip:
        '用户购买商品的实际支付金额，包括微信支付、余额支付、支付宝支付、线下支付金额（拼团商品在成团之后计入，线下支付订单在后台确认支付后计入）',
      iconColor: 'bg-purple-100',
      iconBgColor: 'text-purple-500',
      prefix: '￥',
      decimals: 2,
      percent: calculateRelativeRate(
        summary?.value?.value?.orderPayPrice,
        summary?.value?.reference?.orderPayPrice,
      ),
    },
    {
      icon: 'fa-solid:money-check-alt',
      title: '充值金额',
      value: Number(fenToYuan(summary.value?.value?.rechargePrice || 0)),
      tooltip: '用户成功充值的金额',
      iconColor: 'bg-yellow-100',
      iconBgColor: 'text-yellow-500',
      prefix: '￥',
      decimals: 2,
      percent: calculateRelativeRate(
        summary?.value?.value?.rechargePrice,
        summary?.value?.reference?.rechargePrice,
      ),
    },
    {
      icon: 'ep:warning-filled',
      title: '支出金额',
      value: Number(fenToYuan(summary.value?.value?.expensePrice || 0)),
      tooltip: '余额支付金额、支付佣金金额、商品退款金额',
      iconColor: 'bg-green-100',
      iconBgColor: 'text-green-500',
      prefix: '￥',
      decimals: 2,
      percent: calculateRelativeRate(
        summary?.value?.value?.expensePrice,
        summary?.value?.reference?.expensePrice,
      ),
    },
    {
      icon: 'fa-solid:wallet',
      title: '余额支付金额',
      value: Number(fenToYuan(summary.value?.value?.walletPayPrice || 0)),
      tooltip: '余额支付金额、支付佣金金额、商品退款金额',
      iconColor: 'bg-cyan-100',
      iconBgColor: 'text-cyan-500',
      prefix: '￥',
      decimals: 2,
      percent: calculateRelativeRate(
        summary?.value?.value?.walletPayPrice,
        summary?.value?.reference?.walletPayPrice,
      ),
    },
    {
      icon: 'fa-solid:award',
      title: '支付佣金金额',
      value: Number(
        fenToYuan(summary.value?.value?.brokerageSettlementPrice || 0),
      ),
      tooltip: '后台给推广员支付的推广佣金，以实际支付为准',
      iconColor: 'bg-yellow-100',
      iconBgColor: 'text-yellow-500',
      prefix: '￥',
      decimals: 2,
      percent: calculateRelativeRate(
        summary?.value?.value?.brokerageSettlementPrice,
        summary?.value?.reference?.brokerageSettlementPrice,
      ),
    },
    {
      icon: 'fa-solid:times-circle',
      title: '商品退款金额',
      value: Number(fenToYuan(summary.value?.value?.afterSaleRefundPrice || 0)),
      tooltip: '用户成功退款的商品金额',
      iconColor: 'bg-blue-100',
      iconBgColor: 'text-blue-500',
      prefix: '￥',
      decimals: 2,
      percent: calculateRelativeRate(
        summary?.value?.value?.afterSaleRefundPrice,
        summary?.value?.reference?.afterSaleRefundPrice,
      ),
    },
  ];
};

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await confirm('确定要导出交易状况吗？');
    // 发起导出
    exportLoading.value = true;
    const times = shortcutDateRangePicker.value.times;
    const data = await TradeStatisticsApi.exportTradeStatisticsExcel({ times });
    downloadFileFromBlobPart({ fileName: '交易状况.xls', source: data });
  } finally {
    exportLoading.value = false;
  }
};

const getTradeTrendData = async () => {
  trendLoading.value = true;
  // 1. 处理时间: 开始与截止在同一天的, 折线图出不来, 需要延长一天
  const times = shortcutDateRangePicker.value.times;
  if (isSameDay(times[0], times[1])) {
    // 前天
    times[0] = formatDate(dayjs(times[0]).subtract(1, 'd').toDate());
  }
  // 查询数据
  await Promise.all([getTradeStatisticsAnalyse(), getTradeStatisticsList()]);
  trendLoading.value = false;

  loadOverview();
  renderEcharts(lineChartOptions as any);
};

/** 查询交易状况数据统计 */
const getTradeStatisticsAnalyse = async () => {
  const times = shortcutDateRangePicker.value.times;
  summary.value = await TradeStatisticsApi.getTradeStatisticsAnalyse({
    times,
  });
};

/** 查询交易状况数据列表 */
const getTradeStatisticsList = async () => {
  // 查询数据
  const times = shortcutDateRangePicker.value.times;
  const list = await TradeStatisticsApi.getTradeStatisticsList({ times });
  // 处理数据
  for (const item of list) {
    item.turnoverPrice = Number(fenToYuan(item.turnoverPrice));
    item.orderPayPrice = Number(fenToYuan(item.orderPayPrice));
    item.rechargePrice = Number(fenToYuan(item.rechargePrice));
    item.expensePrice = Number(fenToYuan(item.expensePrice));
  }
  // 更新 Echarts 数据
  if (lineChartOptions.dataset && lineChartOptions.dataset.source) {
    lineChartOptions.dataset.source = list;
  }
};

/** 折线图配置 */
const lineChartOptions = reactive({
  dataset: {
    dimensions: [
      'date',
      'turnoverPrice',
      'orderPayPrice',
      'rechargePrice',
      'expensePrice',
    ],
    source: [] as MallTradeStatisticsApi.TradeTrendSummary[],
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
  series: [
    { name: '营业额', type: 'line', smooth: true },
    { name: '商品支付金额', type: 'line', smooth: true },
    { name: '充值金额', type: 'line', smooth: true },
    { name: '支出金额', type: 'line', smooth: true },
  ],
  toolbox: {
    feature: {
      // 数据区域缩放
      dataZoom: {
        yAxisIndex: false, // Y轴不缩放
      },
      brush: {
        type: ['lineX', 'clear'] as const, // 区域缩放按钮、还原按钮
      },
      saveAsImage: { show: true, name: '交易状况' }, // 保存为图片
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
    type: 'category' as const,
    boundaryGap: false,
    axisTick: {
      show: false,
    },
  },
  yAxis: {
    axisTick: {
      show: false,
    },
  },
});
</script>
<template>
  <AnalysisChartCard title="交易状况">
    <template #header-suffix>
      <!-- 查询条件 -->
      <ShortcutDateRangePicker
        ref="shortcutDateRangePicker"
        @change="getTradeTrendData"
      >
        <el-button
          class="ml-4"
          @click="handleExport"
          :loading="exportLoading"
          v-access:code="['statistics:trade:export']"
        >
          <IconifyIcon icon="ep:download" class="mr-1" />导出
        </el-button>
      </ShortcutDateRangePicker>
    </template>
    <AnalysisOverviewIcon v-model:model-value="overviewItems" />
    <EchartsUI height="500px" ref="chartRef" />
  </AnalysisChartCard>
</template>
