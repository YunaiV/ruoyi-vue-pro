<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallDeliveryExpressApi } from '#/api/mall/trade/delivery/express';
import type { MallDeliveryPickUpStoreApi } from '#/api/mall/trade/delivery/pickUpStore';
import type { MallOrderApi } from '#/api/mall/trade/order';

import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { confirm, Page, useVbenModal } from '@vben/common-ui';
import {
  DeliveryTypeEnum,
  DICT_TYPE,
  TradeOrderStatusEnum,
} from '@vben/constants';
import { useTabs } from '@vben/hooks';

import { message, Tag } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getSimpleDeliveryExpressList } from '#/api/mall/trade/delivery/express';
import { getDeliveryPickUpStore } from '#/api/mall/trade/delivery/pickUpStore';
import {
  getExpressTrackList,
  getOrder,
  pickUpOrder,
} from '#/api/mall/trade/order';
import { useDescription } from '#/components/description';
import { DictTag } from '#/components/dict-tag';
import { TableAction } from '#/components/table-action';

import AddressForm from '../modules/address-form.vue';
import DeliveryForm from '../modules/delivery-form.vue';
import PriceForm from '../modules/price-form.vue';
import RemarkForm from '../modules/remark-form.vue';
import {
  useDeliveryInfoSchema,
  useExpressTrackColumns,
  useOperateLogColumns,
  useOrderInfoSchema,
  useOrderPriceSchema,
  useOrderStatusSchema,
  useProductColumns,
} from './data';

defineOptions({ name: 'TradeOrderDetail' });

const route = useRoute();
const router = useRouter();
const tabs = useTabs();

const loading = ref(false);
const orderId = ref(0);
const order = ref<MallOrderApi.Order>({
  logs: [],
});
const deliveryExpressList = ref<MallDeliveryExpressApi.DeliveryExpress[]>([]);
const expressTrackList = ref<any[]>([]);
const pickUpStore = ref<
  MallDeliveryPickUpStoreApi.DeliveryPickUpStore | undefined
>();

const [OrderInfoDescriptions] = useDescription({
  title: '订单信息',
  bordered: false,
  column: 3,
  class: 'mx-4',
  schema: useOrderInfoSchema(),
});

const [OrderStatusDescriptions] = useDescription({
  title: '订单状态',
  bordered: false,
  column: 1,
  class: 'mx-4',
  schema: useOrderStatusSchema(),
});

const [OrderPriceDescriptions] = useDescription({
  title: '费用信息',
  bordered: false,
  column: 4,
  class: 'mx-4',
  schema: useOrderPriceSchema(),
});

const [DeliveryInfoDescriptions] = useDescription({
  title: '收货信息',
  bordered: false,
  column: 3,
  class: 'mx-4',
  schema: useDeliveryInfoSchema(),
});

const [ProductGrid, productGridApi] = useVbenVxeGrid({
  gridOptions: {
    cellConfig: {
      height: 60,
    },
    columns: useProductColumns(),
    data: [],
    height: 'auto',
    border: true,
    pagerConfig: {
      enabled: false,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MallOrderApi.OrderItem>,
});

const [ExpressTrackGrid, expressTrackGridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useExpressTrackColumns(),
    data: [],
    border: true,
    pagerConfig: {
      enabled: false,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions,
});

const [OperateLogGrid, operateLogGridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useOperateLogColumns(),
    data: [],
    border: true,
    pagerConfig: {
      enabled: false,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions,
});

const [DeliveryFormModal, deliveryFormModalApi] = useVbenModal({
  connectedComponent: DeliveryForm,
  destroyOnClose: true,
});

const [RemarkFormModal, remarkFormModalApi] = useVbenModal({
  connectedComponent: RemarkForm,
  destroyOnClose: true,
});

const [AddressFormModal, addressFormModalApi] = useVbenModal({
  connectedComponent: AddressForm,
  destroyOnClose: true,
});

const [PriceFormModal, priceFormModalApi] = useVbenModal({
  connectedComponent: PriceForm,
  destroyOnClose: true,
});

/** 获得详情 */
async function getDetail() {
  loading.value = true;
  try {
    const res = await getOrder(orderId.value);
    if (res === null) {
      message.error('交易订单不存在');
      handleBack();
      return;
    }
    order.value = res;
    productGridApi.setGridOptions({ data: res.items || [] });
    operateLogGridApi.setGridOptions({ data: res.logs || [] });

    // 如果配送方式为快递，则查询物流公司
    if (res.deliveryType === DeliveryTypeEnum.EXPRESS.type) {
      deliveryExpressList.value = await getSimpleDeliveryExpressList();
      if (res.logisticsId) {
        expressTrackList.value = await getExpressTrackList(res.id!);
        expressTrackGridApi.setGridOptions({
          data: expressTrackList.value || [],
        });
      }
    } else if (
      res.deliveryType === DeliveryTypeEnum.PICK_UP.type &&
      res.pickUpStoreId
    ) {
      pickUpStore.value = await getDeliveryPickUpStore(res.pickUpStoreId);
    }
  } finally {
    loading.value = false;
  }
}

/** 各种操作 */
const handleRemark = () => {
  remarkFormModalApi.setData(order.value).open();
};

const handleDelivery = () => {
  deliveryFormModalApi.setData(order.value).open();
};

const handleUpdateAddress = () => {
  addressFormModalApi.setData(order.value).open();
};

const handleUpdatePrice = () => {
  priceFormModalApi.setData(order.value).open();
};

/** 核销 */
const handlePickUp = async () => {
  await confirm('确认核销订单吗？');
  const hideLoading = message.loading({
    content: '正在处理中...',
    duration: 0,
  });
  try {
    await pickUpOrder(order.value.id!);
    message.success('核销成功');
    await getDetail();
  } finally {
    hideLoading();
  }
};

/** 返回列表页 */
function handleBack() {
  tabs.closeCurrentTab();
  router.push({ name: 'TradeOrder' });
}

/** 初始化 */
onMounted(async () => {
  orderId.value = Number(route.params.id);
  await getDetail();
});
</script>

<template>
  <Page auto-content-height :title="order.no" :loading="loading">
    <template #extra>
      <TableAction
        :actions="[
          {
            label: '返回',
            type: 'default',
            icon: 'lucide:arrow-left',
            onClick: handleBack,
          },
          {
            label: '调整价格',
            type: 'primary',
            onClick: handleUpdatePrice,
            ifShow: order.status === TradeOrderStatusEnum.UNPAID.status,
          },
          {
            label: '备注',
            type: 'primary',
            onClick: handleRemark,
          },
          {
            label: '发货',
            type: 'primary',
            onClick: handleDelivery,
            ifShow:
              order.status === TradeOrderStatusEnum.UNDELIVERED.status &&
              order.deliveryType === DeliveryTypeEnum.EXPRESS.type,
          },
          {
            label: '修改地址',
            type: 'primary',
            onClick: handleUpdateAddress,
            ifShow:
              order.status === TradeOrderStatusEnum.UNDELIVERED.status &&
              order.deliveryType === DeliveryTypeEnum.EXPRESS.type,
          },
          {
            label: '核销',
            type: 'primary',
            onClick: handlePickUp,
            ifShow:
              order.status === TradeOrderStatusEnum.UNDELIVERED.status &&
              order.deliveryType === DeliveryTypeEnum.PICK_UP.type,
          },
        ]"
      />
    </template>

    <!-- 各种操作的弹窗 -->
    <DeliveryFormModal @success="getDetail" />
    <RemarkFormModal @success="getDetail" />
    <AddressFormModal @success="getDetail" />
    <PriceFormModal @success="getDetail" />

    <!-- 订单信息 -->
    <div class="mb-4">
      <OrderInfoDescriptions :data="order" />
    </div>
    <!-- 订单状态 -->
    <div class="mb-4">
      <OrderStatusDescriptions :data="order" />
    </div>
    <!-- 商品信息 -->
    <div class="mb-4">
      <ProductGrid table-title="商品信息">
        <template #spuName="{ row }">
          <div class="flex flex-1 flex-col items-start gap-1 text-left">
            <span class="text-sm">{{ row.spuName }}</span>
            <div class="flex flex-wrap gap-1">
              <Tag
                v-for="property in row.properties"
                :key="property.propertyId!"
                size="small"
              >
                {{ property.propertyName }}: {{ property.valueName }}
              </Tag>
            </div>
          </div>
        </template>
      </ProductGrid>
    </div>
    <!-- 费用信息 -->
    <div class="mb-4">
      <OrderPriceDescriptions :data="order" />
    </div>
    <!-- 收货信息 -->
    <div class="mb-4">
      <DeliveryInfoDescriptions :data="order" />
    </div>
    <!-- 物流详情 -->
    <div v-if="expressTrackList.length > 0" class="mb-4">
      <ExpressTrackGrid table-title="物流详情" />
    </div>
    <!-- 操作日志 -->
    <div>
      <OperateLogGrid table-title="操作日志">
        <template #userType="{ row }">
          <Tag v-if="row.userType === 0" color="default"> 系统 </Tag>
          <DictTag v-else :type="DICT_TYPE.USER_TYPE" :value="row.userType" />
        </template>
      </OperateLogGrid>
    </div>
  </Page>
</template>
