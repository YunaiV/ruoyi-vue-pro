<script lang="ts" setup>
import type { DataComparisonRespVO } from '#/api/mall/statistics/common';
import type { MallTradeStatisticsApi } from '#/api/mall/statistics/trade';

import { onMounted, ref } from 'vue';

import { DocAlert, Page, StatisticCard } from '@vben/common-ui';
import { fenToYuan } from '@vben/utils';

import { Col, Row } from 'ant-design-vue';

import { getTradeStatisticsSummary } from '#/api/mall/statistics/trade';

import TradeTrendCard from './modules/trend-card.vue';

/** 交易统计 */
defineOptions({ name: 'TradeStatistics' });

const loading = ref(true); // 加载中
const summary =
  ref<DataComparisonRespVO<MallTradeStatisticsApi.TradeSummaryRespVO>>(); // 交易统计数据

/** 计算环比百分比 */
function calculateRelativeRate(value?: number, reference?: number): string {
  const refValue = Number(reference || 0);
  const curValue = Number(value || 0);
  if (!refValue || refValue === 0) {
    return '0.00';
  }
  return (((curValue - refValue) / refValue) * 100).toFixed(2);
}

/** 查询交易统计 */
async function loadTradeStatisticsSummary() {
  summary.value = await getTradeStatisticsSummary();
}

/** 初始化 */
onMounted(async () => {
  loading.value = true;
  await loadTradeStatisticsSummary();
  loading.value = false;
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【统计】会员、商品、交易统计"
        url="https://doc.iocoder.cn/mall/statistics/"
      />
    </template>

    <!-- 交易概览卡片 -->
    <div class="flex flex-col gap-4">
      <Row :gutter="16">
        <Col :sm="6" :xs="12">
          <StatisticCard
            tooltip="昨日订单数量"
            title="昨日订单数量"
            :value="summary?.value?.yesterdayOrderCount || 0"
            :percent="
              calculateRelativeRate(
                summary?.value?.yesterdayOrderCount,
                summary?.reference?.yesterdayOrderCount,
              )
            "
          />
        </Col>
        <Col :sm="6" :xs="12">
          <StatisticCard
            tooltip="本月订单数量"
            title="本月订单数量"
            :value="summary?.value?.monthOrderCount || 0"
            :percent="
              calculateRelativeRate(
                summary?.value?.monthOrderCount,
                summary?.reference?.monthOrderCount,
              )
            "
          />
        </Col>
        <Col :sm="6" :xs="12">
          <StatisticCard
            tooltip="昨日支付金额"
            title="昨日支付金额"
            prefix="￥"
            :decimals="2"
            :value="Number(fenToYuan(summary?.value?.yesterdayPayPrice || 0))"
            :percent="
              calculateRelativeRate(
                summary?.value?.yesterdayPayPrice,
                summary?.reference?.yesterdayPayPrice,
              )
            "
          />
        </Col>
        <Col :sm="6" :xs="12">
          <StatisticCard
            tooltip="本月支付金额"
            title="本月支付金额"
            prefix="￥"
            :decimals="2"
            :value="Number(fenToYuan(summary?.value?.monthPayPrice || 0))"
            :percent="
              calculateRelativeRate(
                summary?.value?.monthPayPrice,
                summary?.reference?.monthPayPrice,
              )
            "
          />
        </Col>
      </Row>

      <!-- 交易趋势 -->
      <TradeTrendCard />
    </div>
  </Page>
</template>
