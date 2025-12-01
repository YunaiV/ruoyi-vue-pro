<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallOrderApi } from '#/api/mall/trade/order';

import { useRouter } from 'vue-router';

import { DICT_TYPE } from '@vben/constants';
import { fenToYuan } from '@vben/utils';

import { ElImage, ElTag } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getOrderPage } from '#/api/mall/trade/order';
import { DictTag } from '#/components/dict-tag';
import { $t } from '#/locales';
import {
  useGridColumns,
  useGridFormSchema as useOrderGridFormSchema,
} from '#/views/mall/trade/order/data';

const props = defineProps<{
  userId: number;
}>();

const { push } = useRouter();

/** 列表的搜索表单（过滤掉用户相关字段） */
function useGridFormSchema() {
  const excludeFields = new Set(['userId', 'userNickname']);
  return useOrderGridFormSchema().filter(
    (item) => !excludeFields.has(item.fieldName),
  );
}

/** 详情 */
function handleDetail(row: MallOrderApi.Order) {
  push({ name: 'TradeOrderDetail', params: { id: row.id } });
}

const [Grid] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    expandConfig: {
      trigger: 'row',
      expandAll: true,
      padding: true,
    },
    columns: useGridColumns(),
    keepSource: true,
    pagerConfig: {
      pageSize: 10,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getOrderPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            userId: props.userId,
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
  } as VxeTableGridOptions<MallOrderApi.Order>,
});
</script>

<template>
  <Grid table-title="订单列表">
    <template #expand_content="{ row }">
      <div class="py-2">
        <div
          v-for="item in row.items"
          :key="item.id!"
          class="flex items-start border-b border-gray-100 py-2 last:border-b-0"
        >
          <div class="mr-3 flex-shrink-0">
            <ElImage :src="item.picUrl" class="h-10 w-10" />
          </div>
          <div class="flex-1">
            <div class="mb-1 font-medium">
              {{ item.spuName }}
              <ElTag
                v-for="property in item.properties"
                :key="property.propertyId"
                class="ml-1"
                size="small"
              >
                {{ property.propertyName }}: {{ property.valueName }}
              </ElTag>
            </div>
            <div
              class="flex items-center justify-between text-xs text-gray-500"
            >
              <span>
                原价：{{ fenToYuan(item.price!) }} 元 / 数量：{{ item.count }}个
              </span>
              <DictTag
                :type="DICT_TYPE.TRADE_ORDER_ITEM_AFTER_SALE_STATUS"
                :value="item.afterSaleStatus"
              />
            </div>
          </div>
        </div>
      </div>
    </template>
    <template #actions="{ row }">
      <TableAction
        :actions="[
          {
            label: $t('common.detail'),
            type: 'primary',
            link: true,
            icon: ACTION_ICON.VIEW,
            auth: ['trade:order:query'],
            onClick: handleDetail.bind(null, row),
          },
        ]"
      />
    </template>
  </Grid>
</template>
