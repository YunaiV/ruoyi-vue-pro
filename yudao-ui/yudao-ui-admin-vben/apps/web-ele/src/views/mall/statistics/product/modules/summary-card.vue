<script lang="ts" setup>
import type { Dayjs } from 'dayjs';

import type { EchartsUIType } from '@vben/plugins/echarts';

import type { DataComparisonRespVO } from '#/api/mall/statistics/common';
import type { MallProductStatisticsApi } from '#/api/mall/statistics/product';

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
  exportProductStatisticsExcel,
  getProductStatisticsAnalyse,
  getProductStatisticsList,
} from '#/api/mall/statistics/product';
import ShortcutDateRangePicker from '#/components/shortcut-date-range-picker/shortcut-date-range-picker.vue';
import { $t } from '#/locales';

import { getProductSummaryChartOptions } from './summary-chart-options';

/** 商品概况 */
defineOptions({ name: 'ProductSummaryCard' });

const trendLoading = ref(true); // 商品状态加载中
const exportLoading = ref(false); // 导出的加载中
const trendSummary =
  ref<DataComparisonRespVO<MallProductStatisticsApi.ProductStatisticsRespVO>>(); // 商品状况统计数据
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
    loadProductTrendData();
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
  loadProductTrendData();
};

/** 处理商品状况查询 */
const loadProductTrendData = async () => {
  trendLoading.value = true;
  try {
    await Promise.all([loadProductTrendSummary(), loadProductStatisticsList()]);
  } finally {
    trendLoading.value = false;
  }
};

/** 查询商品状况数据统计 */
async function loadProductTrendSummary() {
  trendSummary.value = await getProductStatisticsAnalyse({
    times: searchTimes.value.length > 0 ? searchTimes.value : undefined,
  });
}

/** 查询商品状况数据列表 */
async function loadProductStatisticsList() {
  const list = await getProductStatisticsList({
    times: searchTimes.value.length > 0 ? searchTimes.value : undefined,
  });

  // 渲染图表
  await renderEcharts(getProductSummaryChartOptions(list));
}

/** 导出按钮操作 */
async function handleExport() {
  try {
    // 导出的二次确认
    await confirm({
      content: '确认导出商品状况数据吗？',
    });
    // 发起导出
    exportLoading.value = true;
    const data = await exportProductStatisticsExcel({
      times: searchTimes.value.length > 0 ? searchTimes.value : undefined,
    });
    // 处理下载
    downloadFileFromBlobPart({ fileName: '商品状况.xlsx', source: data });
  } finally {
    exportLoading.value = false;
  }
}
</script>

<template>
  <ElCard shadow="never" class="h-full">
    <template #header>
      <div class="flex items-center justify-between">
        <span>商品概况</span>
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
      <ElCol :xl="8" :md="8" :sm="24" class="mb-4">
        <SummaryCard
          title="商品浏览量"
          tooltip="在选定条件下，所有商品详情页被访问的次数，一个人在统计时间内访问多次记为多次"
          icon="lucide:eye"
          icon-color="text-blue-500"
          icon-bg-color="bg-blue-100"
          :decimals="0"
          :value="trendSummary?.value?.browseCount || 0"
          :percent="
            calculateRelativeRate(
              trendSummary?.value?.browseCount,
              trendSummary?.reference?.browseCount,
            )
          "
        />
      </ElCol>
      <ElCol :xl="8" :md="8" :sm="24" class="mb-4">
        <SummaryCard
          title="商品访客数"
          tooltip="在选定条件下，访问任何商品详情页的人数，一个人在统计时间范围内访问多次只记为一个"
          icon="lucide:users"
          icon-color="text-purple-500"
          icon-bg-color="bg-purple-100"
          :decimals="0"
          :value="trendSummary?.value?.browseUserCount || 0"
          :percent="
            calculateRelativeRate(
              trendSummary?.value?.browseUserCount,
              trendSummary?.reference?.browseUserCount,
            )
          "
        />
      </ElCol>
      <ElCol :xl="8" :md="8" :sm="24" class="mb-4">
        <SummaryCard
          title="支付件数"
          tooltip="在选定条件下，成功付款订单的商品件数之和"
          icon="lucide:credit-card"
          icon-color="text-yellow-500"
          icon-bg-color="bg-yellow-100"
          :decimals="0"
          :value="trendSummary?.value?.orderPayCount || 0"
          :percent="
            calculateRelativeRate(
              trendSummary?.value?.orderPayCount,
              trendSummary?.reference?.orderPayCount,
            )
          "
        />
      </ElCol>
      <ElCol :xl="8" :md="8" :sm="24" class="mb-4">
        <SummaryCard
          title="支付金额"
          tooltip="在选定条件下，成功付款订单的商品金额之和"
          icon="lucide:banknote"
          icon-color="text-green-500"
          icon-bg-color="bg-green-100"
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
      <ElCol :xl="8" :md="8" :sm="24" class="mb-4">
        <SummaryCard
          title="退款件数"
          tooltip="在选定条件下，成功退款的商品件数之和"
          icon="lucide:wallet"
          icon-color="text-cyan-500"
          icon-bg-color="bg-cyan-100"
          :decimals="0"
          :value="trendSummary?.value?.afterSaleCount || 0"
          :percent="
            calculateRelativeRate(
              trendSummary?.value?.afterSaleCount,
              trendSummary?.reference?.afterSaleCount,
            )
          "
        />
      </ElCol>
      <ElCol :xl="8" :md="8" :sm="24" class="mb-4">
        <SummaryCard
          title="退款金额"
          tooltip="在选定条件下，成功退款的商品金额之和"
          icon="lucide:receipt"
          icon-color="text-orange-500"
          icon-bg-color="bg-orange-100"
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
