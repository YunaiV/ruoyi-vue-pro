<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallAfterSaleApi } from '#/api/mall/trade/afterSale';

import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { ElButton, ElImage, ElTabs, ElTag } from 'element-plus';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getAfterSalePage } from '#/api/mall/trade/afterSale';
import {
  useGridColumns,
  useGridFormSchema,
} from '#/views/mall/trade/afterSale/data';

const props = defineProps<{
  userId: number;
}>();

const { push } = useRouter();

const statusTabs = ref([
  {
    label: '全部',
    value: '0',
  },
]);
const statusTab = ref(statusTabs.value[0]!.value);

/** 处理退款 */
function handleOpenAfterSaleDetail(row: MallAfterSaleApi.AfterSale) {
  push({ name: 'TradeAfterSaleDetail', params: { id: row.id } });
}

/** 查看订单详情 */
function handleOpenOrderDetail(row: MallAfterSaleApi.AfterSale) {
  push({ name: 'TradeOrderDetail', params: { id: row.orderId } });
}

/** 切换售后状态 */
function handleChangeStatus(key: number | string) {
  statusTab.value = key.toString();
  gridApi.query();
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    cellConfig: {
      height: 60,
    },
    columns: useGridColumns(),
    keepSource: true,
    pagerConfig: {
      pageSize: 10,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getAfterSalePage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            userId: props.userId,
            status:
              statusTab.value === '0' ? undefined : Number(statusTab.value),
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MallAfterSaleApi.AfterSale>,
});

/** 初始化 */
onMounted(() => {
  for (const dict of getDictOptions(DICT_TYPE.TRADE_AFTER_SALE_STATUS)) {
    statusTabs.value.push({
      label: dict.label,
      value: dict.value.toString(),
    });
  }
});
</script>

<template>
  <Grid>
    <template #toolbar-actions>
      <ElTabs
        v-model="statusTab"
        class="w-full"
        @tab-change="handleChangeStatus"
      >
        <ElTabs.TabPane
          v-for="tab in statusTabs"
          :key="tab.value"
          :label="tab.label"
          :name="tab.value"
        />
      </ElTabs>
    </template>
    <template #orderNo="{ row }">
      <ElButton type="primary" link @click="handleOpenOrderDetail(row)">
        {{ row.orderNo }}
      </ElButton>
    </template>
    <template #productInfo="{ row }">
      <div class="flex items-start gap-2 text-left">
        <ElImage
          v-if="row.picUrl"
          :src="row.picUrl"
          style="width: 40px; height: 40px"
          :preview-src-list="[row.picUrl]"
        />
        <div class="flex flex-1 flex-col gap-1">
          <span class="text-sm">{{ row.spuName }}</span>
          <div class="mt-1 flex flex-wrap gap-1">
            <ElTag
              v-for="property in row.properties"
              :key="property.propertyId!"
              size="small"
              type="info"
            >
              {{ property.propertyName }}: {{ property.valueName }}
            </ElTag>
          </div>
        </div>
      </div>
    </template>
    <template #actions="{ row }">
      <TableAction
        :actions="[
          {
            label: '处理退款',
            type: 'primary',
            link: true,
            onClick: handleOpenAfterSaleDetail.bind(null, row),
          },
        ]"
      />
    </template>
  </Grid>
</template>
