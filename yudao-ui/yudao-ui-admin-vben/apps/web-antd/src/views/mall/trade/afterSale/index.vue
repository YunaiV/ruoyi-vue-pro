<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallAfterSaleApi } from '#/api/mall/trade/afterSale';

import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { DocAlert, Page } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { Button, Image, Tabs, Tag } from 'ant-design-vue';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getAfterSalePage } from '#/api/mall/trade/afterSale';

import { useGridColumns, useGridFormSchema } from './data';

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
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getAfterSalePage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
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
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【交易】售后退款"
        url="https://doc.iocoder.cn/mall/trade-aftersale/"
      />
    </template>

    <Grid>
      <template #toolbar-actions>
        <Tabs
          v-model:active-key="statusTab"
          class="w-full"
          @change="handleChangeStatus"
        >
          <Tabs.TabPane
            v-for="tab in statusTabs"
            :key="tab.value"
            :tab="tab.label"
          />
        </Tabs>
      </template>
      <template #orderNo="{ row }">
        <Button type="link" @click="handleOpenOrderDetail(row)">
          {{ row.orderNo }}
        </Button>
      </template>
      <template #productInfo="{ row }">
        <div class="flex items-start gap-2 text-left">
          <Image
            v-if="row.picUrl"
            :src="row.picUrl"
            :width="40"
            :height="40"
            :preview="{ src: row.picUrl }"
          />
          <div class="flex flex-1 flex-col gap-1">
            <span class="text-sm">{{ row.spuName }}</span>
            <div class="mt-1 flex flex-wrap gap-1">
              <Tag
                v-for="property in row.properties"
                :key="property.propertyId!"
                size="small"
                color="blue"
              >
                {{ property.propertyName }}: {{ property.valueName }}
              </Tag>
            </div>
          </div>
        </div>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '处理退款',
              type: 'link',
              onClick: handleOpenAfterSaleDetail.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
