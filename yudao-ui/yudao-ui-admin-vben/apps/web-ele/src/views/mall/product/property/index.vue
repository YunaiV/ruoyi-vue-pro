<script lang="ts" setup>
import { ref } from 'vue';

import { DocAlert, Page } from '@vben/common-ui';

import PropertyGrid from './modules/property-grid.vue';
import ValueGrid from './modules/value-grid.vue';

const searchPropertyId = ref<number>(); // 搜索的属性 ID

function handlePropertyIdSelect(propertyId: number) {
  searchPropertyId.value = propertyId;
}
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【商品】商品属性"
        url="https://doc.iocoder.cn/mall/product-property/"
      />
    </template>
    <div class="flex h-full">
      <!-- 左侧属性列表 -->
      <div class="w-1/2 pr-3">
        <PropertyGrid @select="handlePropertyIdSelect" />
      </div>
      <!-- 右侧属性数据列表 -->
      <div class="w-1/2">
        <ValueGrid :property-id="searchPropertyId" />
      </div>
    </div>
  </Page>
</template>
