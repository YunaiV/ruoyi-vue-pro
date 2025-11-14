<script setup lang="ts">
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallDeliveryPickUpStoreApi } from '#/api/mall/trade/delivery/pickUpStore';
import type { MallOrderApi } from '#/api/mall/trade/order/index';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { DeliveryTypeEnum } from '@vben/constants';
import { $t } from '@vben/locales';
import { fenToYuan } from '@vben/utils';

import { ElImage, ElTag } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getSimpleDeliveryExpressList } from '#/api/mall/trade/delivery/express';
import { getSimpleDeliveryPickUpStoreList } from '#/api/mall/trade/delivery/pickUpStore';
import * as OrderApi from '#/api/mall/trade/order/index';
import { DictTag } from '#/components/dict-tag';
import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getRangePickerDefaultProps } from '#/utils';
import { useGridColumns } from '#/views/mall/trade/order/data';

const props = defineProps<{
  userId: number;
}>();

const pickUpStoreList = ref<MallDeliveryPickUpStoreApi.PickUpStore[]>([]);

getSimpleDeliveryPickUpStoreList().then((res) => {
  pickUpStoreList.value = res;
});
const { push } = useRouter();
/** 详情 */
function handleDetail(row: MallOrderApi.Order) {
  push({ name: 'TradeOrderDetail', params: { id: row.id } });
}

const [Grid] = useVbenVxeGrid({
  formOptions: {
    schema: [
      {
        fieldName: 'bizType',
        label: '订单状态',
        component: 'Select',
        componentProps: {
          clearable: true,
          options: getDictOptions(DICT_TYPE.TRADE_ORDER_STATUS, 'number'),
          placeholder: '全部',
        },
      },
      {
        fieldName: 'payChannelCode',
        label: '支付方式',
        component: 'Select',
        componentProps: {
          clearable: true,
          options: getDictOptions(DICT_TYPE.PAY_CHANNEL_CODE, 'number'),
          placeholder: '全部',
        },
      },
      {
        fieldName: 'createTime',
        label: '创建时间',
        component: 'RangePicker',
        componentProps: {
          ...getRangePickerDefaultProps(),
          clearable: true,
        },
      },
      {
        fieldName: 'terminal',
        label: '订单来源',
        component: 'Select',
        componentProps: {
          clearable: true,
          options: getDictOptions(DICT_TYPE.TERMINAL, 'number'),
          placeholder: '全部',
        },
      },
      {
        fieldName: 'type',
        label: '订单类型',
        component: 'Select',
        componentProps: {
          clearable: true,
          options: getDictOptions(DICT_TYPE.TRADE_ORDER_TYPE, 'number'),
          placeholder: '全部',
        },
      },
      {
        fieldName: 'deliveryType',
        label: '配送方式',
        component: 'Select',
        componentProps: {
          clearable: true,
          options: getDictOptions(DICT_TYPE.TRADE_DELIVERY_TYPE, 'number'),
          placeholder: '全部',
        },
      },
      {
        fieldName: 'logisticsId',
        label: '快递公司',
        component: 'ApiSelect',
        componentProps: {
          clearable: true,
          api: getSimpleDeliveryExpressList,
          labelField: 'name',
          valueField: 'id',
          placeholder: '全部',
        },
        dependencies: {
          triggerFields: ['deliveryType'],
          show: (values) =>
            values.deliveryType === DeliveryTypeEnum.EXPRESS.type,
        },
      },
      {
        fieldName: 'pickUpStoreId',
        label: '自提门店',
        component: 'ApiSelect',
        componentProps: {
          api: getSimpleDeliveryPickUpStoreList,
          labelField: 'name',
          valueField: 'id',
        },
        dependencies: {
          triggerFields: ['deliveryType'],
          show: (values) =>
            values.deliveryType === DeliveryTypeEnum.PICK_UP.type,
        },
      },
      {
        fieldName: 'pickUpVerifyCode',
        label: '核销码',
        component: 'Input',
        dependencies: {
          triggerFields: ['deliveryType'],
          show: (values) =>
            values.deliveryType === DeliveryTypeEnum.PICK_UP.type,
        },
      },
    ],
  },
  gridOptions: {
    columns: useGridColumns(),
    keepSource: true,
    pagerConfig: {
      pageSize: 10,
    },
    expandConfig: {
      trigger: 'row',
      expandAll: true,
      padding: true,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await OrderApi.getOrderPage({
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
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MallOrderApi.Order>,
  separator: false,
});
</script>

<template>
  <Grid table-title="订单列表">
    <template #expand_content="{ row }">
      <div class="order-items">
        <div v-for="(item, index) in row.items" :key="index" class="order-item">
          <div class="order-item-image">
            <ElImage :src="item.picUrl" :width="40" :height="40" />
          </div>
          <div class="order-item-content">
            <div class="order-item-name">
              {{ item.spuName }}
              <ElTag
                v-for="property in item.properties"
                :key="property.id"
                class="ml-1"
              >
                {{ property.propertyName }}: {{ property.valueName }}
              </ElTag>
            </div>
            <div class="order-item-info">
              <span>
                原价：{{ fenToYuan(item.price) }} 元 / 数量：{{ item.count }} 个
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
<style lang="scss" scoped>
.order-items {
  padding: 8px 0;
}

.order-item {
  display: flex;
  align-items: flex-start;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.order-item-image {
  flex-shrink: 0;
  margin-right: 12px;
}

.order-item-content {
  flex: 1;
}

.order-item-name {
  margin-bottom: 4px;
  font-weight: 500;
}

.order-item-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: #666;
}
</style>
