<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallOrderApi } from '#/api/mall/trade/order';

import { useRouter } from 'vue-router';

import { DICT_TYPE } from '@vben/constants';
import { fenToYuan } from '@vben/utils';

import { Image, List, Tag } from 'ant-design-vue';

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
      <List item-layout="vertical" :data-source="row.items">
        <template #renderItem="{ item }">
          <List.Item>
            <List.Item.Meta>
              <template #title>
                {{ item.spuName }}
                <Tag
                  color="blue"
                  v-for="property in item.properties"
                  :key="property.propertyId"
                >
                  {{ property.propertyName }} : {{ property.valueName }}
                </Tag>
              </template>
              <template #avatar>
                <Image :src="item.picUrl" :width="40" :height="40" />
              </template>
              <template #description>
                {{
                  `原价：${fenToYuan(item.price)} 元 / 数量：${item.count} 个`
                }}
                |
                <DictTag
                  :type="DICT_TYPE.TRADE_ORDER_ITEM_AFTER_SALE_STATUS"
                  :value="item.afterSaleStatus"
                />
              </template>
            </List.Item.Meta>
          </List.Item>
        </template>
      </List>
    </template>
    <template #actions="{ row }">
      <TableAction
        :actions="[
          {
            label: $t('common.detail'),
            type: 'link',
            icon: ACTION_ICON.VIEW,
            auth: ['trade:order:query'],
            onClick: handleDetail.bind(null, row),
          },
        ]"
      />
    </template>
  </Grid>
</template>
