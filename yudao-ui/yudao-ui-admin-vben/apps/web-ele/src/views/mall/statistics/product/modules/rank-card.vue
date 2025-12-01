<script lang="ts" setup>
import type { Dayjs } from 'dayjs';

import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { ref } from 'vue';

import { buildSortingField } from '@vben/request';
import { formatDateTime } from '@vben/utils';

import { ElCard } from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getProductStatisticsRankPage } from '#/api/mall/statistics/product';
import ShortcutDateRangePicker from '#/components/shortcut-date-range-picker/shortcut-date-range-picker.vue';

/** 商品排行 */
defineOptions({ name: 'ProductRankCard' });

const searchTimes = ref<string[]>([]);

/** 处理日期范围变化 */
const handleDateRangeChange = (times?: [Dayjs, Dayjs]) => {
  if (times?.length !== 2) {
    return;
  }
  searchTimes.value = [
    formatDateTime(times[0]) as string,
    formatDateTime(times[1]) as string,
  ];
  gridApi.query();
};

const columns: VxeTableGridOptions['columns'] = [
  { field: 'spuId', title: '商品 ID', minWidth: 100 },
  {
    field: 'picUrl',
    title: '商品图片',
    minWidth: 100,
    cellRender: { name: 'CellImage' },
  },
  {
    field: 'name',
    title: '商品名称',
    minWidth: 200,
  },
  {
    field: 'browseCount',
    title: '浏览量',
    minWidth: 100,
    sortable: true,
  },
  {
    field: 'browseUserCount',
    title: '访客数',
    minWidth: 100,
    sortable: true,
  },
  {
    field: 'cartCount',
    title: '加购件数',
    minWidth: 110,
    sortable: true,
  },
  {
    field: 'orderCount',
    title: '下单件数',
    minWidth: 110,
    sortable: true,
  },
  {
    field: 'orderPayCount',
    title: '支付件数',
    minWidth: 110,
    sortable: true,
  },
  {
    field: 'orderPayPrice',
    title: '支付金额（元）',
    minWidth: 120,
    formatter: 'formatFenToYuanAmount',
    sortable: true,
  },
  {
    field: 'favoriteCount',
    title: '收藏数',
    minWidth: 100,
    sortable: true,
  },
  {
    field: 'browseConvertPercent',
    title: '访客-支付转化率(%)',
    minWidth: 160,
    sortable: true,
    formatter: ({ cellValue }) => `${cellValue || 0}%`,
  },
];

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns,
    height: 400,
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page, sorts }) => {
          return await getProductStatisticsRankPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            times: searchTimes.value.length > 0 ? searchTimes.value : undefined,
            ...buildSortingField(sorts),
          });
        },
      },
      sort: true,
    },
    sortConfig: {
      remote: true,
      multiple: false,
    },
    toolbarConfig: {
      enabled: false,
    },
  } as VxeTableGridOptions,
});
</script>

<template>
  <ElCard shadow="never">
    <template #header>
      <div class="flex items-center justify-between">
        <span>商品排行</span>
        <ShortcutDateRangePicker @change="handleDateRangeChange" />
      </div>
    </template>
    <Grid />
  </ElCard>
</template>
