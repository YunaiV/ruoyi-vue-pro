<script lang="ts" setup>
import type { Dayjs } from 'dayjs';

import type { EchartsUIType } from '@vben/plugins/echarts';

import type { DataComparisonRespVO } from '#/api/mall/statistics/common';
import type { MallTradeStatisticsApi } from '#/api/mall/statistics/trade';

import { ref } from 'vue';

import { confirm, SummaryCard } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { EchartsUI, useEcharts } from '@vben/plugins/echarts';
import {
  downloadFileFromBlobPart,
  fenToYuan,
  formatDateTime,
  isSameDay,
} from '@vben/utils';

import dayjs from 'dayjs';
import { ElButton, ElCard, ElCol, ElRow } from 'element-plus';

import {
  exportTradeStatisticsExcel,
  getTradeStatisticsAnalyse,
  getTradeStatisticsList,
} from '#/api/mall/statistics/trade';
import ShortcutDateRangePicker from '#/components/shortcut-date-range-picker/shortcut-date-range-picker.vue';
import { $t } from '#/locales';

import { getTradeTrendChartOptions } from './trend-chart-options';

/** 交易趋势 */
defineOptions({ name: 'TradeTrendCard' });

const trendLoading = ref(true); // 交易状态加载中
const exportLoading = ref(false); // 导出的加载中
const trendSummary =
  ref<DataComparisonRespVO<MallTradeStatisticsApi.TradeTrendSummaryRespVO>>(); // 交易状况统计数据
const searchTimes = ref<string[]>([]);

const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);

/** 计算环比百分比 */
const calculateRelativeRate = (value?: number, reference?: number): string => {
  const refValue = Number(reference || 0);
  const curValue = Number(value || 0);
  if (!refValue || refValue === 0) {
    return '0.00';
  }
  return (((curValue - refValue) / refValue) * 100).toFixed(2);
};

/** 处理日期范围变化 */
const handleDateRangeChange = (times?: [Dayjs, Dayjs]) => {
  if (times?.length !== 2) {
    loadTradeTrendData();
    return;
  }
  // 处理时间: 开始与截止在同一天的, 折线图出不来, 需要延长一天
  let adjustedTimes = times;
  if (isSameDay(times[0], times[1])) {
    adjustedTimes = [dayjs(times[0]).subtract(1, 'd'), times[1]];
  }
  searchTimes.value = [
    formatDateTime(adjustedTimes[0]) as string,
    formatDateTime(adjustedTimes[1]) as string,
  ];

  // 查询数据
  loadTradeTrendData();
};

/** 处理交易状况查询 */
async function loadTradeTrendData() {
  trendLoading.value = true;
  try {
    await Promise.all([
      loadTradeStatisticsAnalyse(),
      loadTradeStatisticsList(),
    ]);
  } finally {
    trendLoading.value = false;
  }
}

/** 查询交易状况数据统计 */
async function loadTradeStatisticsAnalyse() {
  trendSummary.value = await getTradeStatisticsAnalyse({
    times: searchTimes.value.length > 0 ? searchTimes.value : undefined,
  });
}

/** 查询交易状况数据列表 */
async function loadTradeStatisticsList() {
  const list = await getTradeStatisticsList({
    times: searchTimes.value.length > 0 ? searchTimes.value : undefined,
  });

  // 渲染图表
  await renderEcharts(getTradeTrendChartOptions(list));
}

/** 导出按钮操作 */
async function handleExport() {
  try {
    // 导出的二次确认
    await confirm({
      content: '确认导出交易状况数据吗？',
    });
    // 发起导出
    exportLoading.value = true;
    const data = await exportTradeStatisticsExcel({
      times: searchTimes.value.length > 0 ? searchTimes.value : undefined,
    });
    // 处理下载
    downloadFileFromBlobPart({ fileName: '交易状况.xlsx', source: data });
  } finally {
    exportLoading.value = false;
  }
}
</script>

<template>
  <ElCard :bordered="false" shadow="never" class="h-full">
    <template #header>
      <div class="flex items-center justify-between">
        <span class="text-base font-medium">交易状况</span>
        <!-- 查询条件 -->
        <div class="flex items-center gap-2">
          <ShortcutDateRangePicker @change="handleDateRangeChange">
            <ElButton
              class="ml-4"
              @click="handleExport"
              :loading="exportLoading"
            >
              <template #icon>
                <IconifyIcon icon="lucide:download" />
              </template>
              {{ $t('page.action.export') }}
            </ElButton>
          </ShortcutDateRangePicker>
        </div>
      </div>
    </template>

    <!-- 统计值 -->
    <ElRow :gutter="16" class="mb-4">
      <ElCol :md="6" :sm="12" :xs="24" class="mb-4">
        <SummaryCard
          title="营业额"
          tooltip="商品支付金额、充值金额"
          icon="lucide:banknote"
          icon-color="text-blue-500"
          icon-bg-color="bg-blue-100"
          prefix="￥"
          :decimals="2"
          :value="Number(fenToYuan(trendSummary?.value?.turnoverPrice || 0))"
          :percent="
            calculateRelativeRate(
              trendSummary?.value?.turnoverPrice,
              trendSummary?.reference?.turnoverPrice,
            )
          "
        />
      </ElCol>

      <ElCol :md="6" :sm="12" :xs="24" class="mb-4">
        <SummaryCard
          title="商品支付金额"
          tooltip="用户购买商品的实际支付金额，包括微信支付、余额支付、支付宝支付、线下支付金额（拼团商品在成团之后计入，线下支付订单在后台确认支付后计入）"
          icon="lucide:shopping-cart"
          icon-color="text-purple-500"
          icon-bg-color="bg-purple-100"
          prefix="￥"
          :decimals="2"
          :value="Number(fenToYuan(trendSummary?.value?.orderPayPrice || 0))"
          :percent="
            calculateRelativeRate(
              trendSummary?.value?.orderPayPrice,
              trendSummary?.reference?.orderPayPrice,
            )
          "
        />
      </ElCol>

      <ElCol :md="6" :sm="12" :xs="24" class="mb-4">
        <SummaryCard
          title="充值金额"
          tooltip="用户成功充值的金额"
          icon="lucide:credit-card"
          icon-color="text-yellow-500"
          icon-bg-color="bg-yellow-100"
          prefix="￥"
          :decimals="2"
          :value="Number(fenToYuan(trendSummary?.value?.rechargePrice || 0))"
          :percent="
            calculateRelativeRate(
              trendSummary?.value?.rechargePrice,
              trendSummary?.reference?.rechargePrice,
            )
          "
        />
      </ElCol>
      <ElCol :md="6" :sm="12" :xs="24" class="mb-4">
        <SummaryCard
          title="支出金额"
          tooltip="余额支付金额、支付佣金金额、商品退款金额"
          icon="lucide:trending-down"
          icon-color="text-green-500"
          icon-bg-color="bg-green-100"
          prefix="￥"
          :decimals="2"
          :value="Number(fenToYuan(trendSummary?.value?.expensePrice || 0))"
          :percent="
            calculateRelativeRate(
              trendSummary?.value?.expensePrice,
              trendSummary?.reference?.expensePrice,
            )
          "
        />
      </ElCol>
      <ElCol :md="6" :sm="12" :xs="24" class="mb-4">
        <SummaryCard
          title="余额支付金额"
          tooltip="用户下单时使用余额实际支付的金额"
          icon="lucide:wallet"
          icon-color="text-cyan-500"
          icon-bg-color="bg-cyan-100"
          prefix="￥"
          :decimals="2"
          :value="Number(fenToYuan(trendSummary?.value?.walletPayPrice || 0))"
          :percent="
            calculateRelativeRate(
              trendSummary?.value?.walletPayPrice,
              trendSummary?.reference?.walletPayPrice,
            )
          "
        />
      </ElCol>
      <ElCol :md="6" :sm="12" :xs="24" class="mb-4">
        <SummaryCard
          title="支付佣金金额"
          tooltip="后台给推广员支付的推广佣金，以实际支付为准"
          icon="lucide:gift"
          icon-color="text-orange-500"
          icon-bg-color="bg-orange-100"
          prefix="￥"
          :decimals="2"
          :value="
            Number(
              fenToYuan(trendSummary?.value?.brokerageSettlementPrice || 0),
            )
          "
          :percent="
            calculateRelativeRate(
              trendSummary?.value?.brokerageSettlementPrice,
              trendSummary?.reference?.brokerageSettlementPrice,
            )
          "
        />
      </ElCol>

      <ElCol :md="6" :sm="12" :xs="24" class="mb-4">
        <SummaryCard
          title="商品退款金额"
          tooltip="用户成功退款的商品金额"
          icon="lucide:undo-2"
          icon-color="text-red-500"
          icon-bg-color="bg-red-100"
          prefix="￥"
          :decimals="2"
          :value="
            Number(fenToYuan(trendSummary?.value?.afterSaleRefundPrice || 0))
          "
          :percent="
            calculateRelativeRate(
              trendSummary?.value?.afterSaleRefundPrice,
              trendSummary?.reference?.afterSaleRefundPrice,
            )
          "
        />
      </ElCol>
    </ElRow>

    <!-- 折线图 -->
    <div v-loading="trendLoading">
      <EchartsUI ref="chartRef" />
    </div>
  </ElCard>
</template>
