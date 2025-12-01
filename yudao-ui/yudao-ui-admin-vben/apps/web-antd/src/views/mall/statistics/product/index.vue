<script lang="ts" setup>
import { DocAlert, Page } from '@vben/common-ui';

import ProductRankCard from './modules/rank-card.vue';
import ProductSummaryCard from './modules/summary-card.vue';

/** 商品统计 */
defineOptions({ name: 'ProductStatistics' });
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【统计】会员、商品、交易统计"
        url="https://doc.iocoder.cn/mall/statistics/"
      />
    </template>

    <div class="flex flex-col gap-4">
      <!-- 商品概览 -->
      <ProductSummaryCard />
      <!-- 商品排行 -->
      <ProductRankCard />
    </div>
  </Page>
</template>
