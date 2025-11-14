<script lang="ts" setup>
import { DocAlert, Page } from '@vben/common-ui';

import ProductRank from './components/product-rank.vue';
import ProductSummary from './components/product-summary.vue';
</script>

<template>
  <Page>
    <DocAlert
      title="【统计】会员、商品、交易统计"
      url="https://doc.iocoder.cn/mall/statistics/"
    />
    <div class="mt-5 w-full">
      <ProductSummary class="mt-5 md:mr-4 md:mt-0" />
    </div>
    <div class="mt-5 w-full">
      <ProductRank class="mt-5 md:mr-4 md:mt-0" />
    </div>
  </Page>
</template>
