<script lang="ts" setup>
import { ref } from 'vue';

import { DocAlert, Page } from '@vben/common-ui';

import { ElCol, ElRow } from 'element-plus';

import SummaryCard from './modules/summary-card.vue';
import TimeSummaryChart from './modules/time-summary-chart.vue';

/** ERP 首页 */
defineOptions({ name: 'ErpHome' });

const loading = ref(false); // 加载中

/** 图表组件引用 */
const saleChartRef = ref();
const purchaseChartRef = ref();
</script>

<template>
  <Page>
    <template #doc>
      <DocAlert
        title="ERP 手册（功能开启）"
        url="https://doc.iocoder.cn/erp/build/"
      />
    </template>

    <div v-loading="loading" class="flex flex-col gap-4">
      <!-- 销售/采购的全局统计 -->
      <SummaryCard />

      <!-- 销售/采购的时段统计 -->
      <ElRow :gutter="16">
        <!-- 销售统计 -->
        <ElCol :md="12" :sm="12" :xs="24">
          <TimeSummaryChart ref="saleChartRef" title="销售统计" type="sale" />
        </ElCol>
        <!-- 采购统计 -->
        <ElCol :md="12" :sm="12" :xs="24">
          <TimeSummaryChart
            ref="purchaseChartRef"
            title="采购统计"
            type="purchase"
          />
        </ElCol>
      </ElRow>
    </div>
  </Page>
</template>
