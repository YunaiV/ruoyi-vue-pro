<script lang="ts" setup>
import type { MallDataComparisonResp } from '#/api/mall/statistics/common';
import type { MallTradeStatisticsApi } from '#/api/mall/statistics/trade';
import type { AnalysisOverviewTradeItem } from '#/views/mall/home/components/data';

import { onMounted, ref } from 'vue';

import { DocAlert, Page } from '@vben/common-ui';
import { calculateRelativeRate, fenToYuan } from '@vben/utils';

import * as TradeStatisticsApi from '#/api/mall/statistics/trade';
import analysisTradeOverview from '#/views/mall/home/components/analysis-trade-overview.vue';

import TradeTransactionCard from './components/trade-transaction-card.vue';

const overviewItems = ref<AnalysisOverviewTradeItem[]>();
const summary =
  ref<MallDataComparisonResp<MallTradeStatisticsApi.TradeSummary>>();
const loadOverview = () => {
  overviewItems.value = [
    {
      title: '昨日订单数量',
      value: summary.value?.value?.yesterdayOrderCount || 0,
      tooltip: '昨日订单数量',
      percent: calculateRelativeRate(
        summary?.value?.value?.yesterdayOrderCount,
        summary.value?.reference?.yesterdayOrderCount,
      ),
    },
    {
      title: '本月订单数量',
      value: summary.value?.value?.monthOrderCount || 0,
      tooltip: '本月订单数量',
      percent: calculateRelativeRate(
        summary?.value?.value?.monthOrderCount,
        summary.value?.reference?.monthOrderCount,
      ),
    },
    {
      title: '昨日支付金额',
      value: Number(fenToYuan(summary.value?.value?.yesterdayPayPrice || 0)),
      tooltip: '昨日支付金额',
      prefix: '￥',
      decimals: 2,
      percent: calculateRelativeRate(
        summary?.value?.value?.yesterdayPayPrice,
        summary.value?.reference?.yesterdayPayPrice,
      ),
    },
    {
      title: '本月支付金额',
      value: summary.value?.value?.monthPayPrice || 0,
      tooltip: '本月支付金额',
      prefix: '￥',
      decimals: 2,
      percent: calculateRelativeRate(
        summary?.value?.value?.monthPayPrice,
        summary.value?.reference?.monthPayPrice,
      ),
    },
  ];
};

/** 查询交易统计 */
const getTradeStatisticsSummary = async () => {
  summary.value = await TradeStatisticsApi.getTradeStatisticsSummary();
};

/** 初始化 */
onMounted(async () => {
  await getTradeStatisticsSummary();
  loadOverview();
});
</script>

<template>
  <Page>
    <DocAlert
      title="【统计】会员、商品、交易统计"
      url="https://doc.iocoder.cn/mall/statistics/"
    />
    <!-- 统计值 -->
    <div class="mb-4 mt-5 w-full md:flex">
      <analysisTradeOverview
        v-model:model-value="overviewItems"
        class="mt-5 md:mr-4 md:mt-0 md:w-full"
      />
    </div>
    <div class="mb-4 mt-5 w-full md:flex">
      <TradeTransactionCard class="mt-5 md:mr-4 md:mt-0 md:w-full" />
    </div>
  </Page>
</template>
