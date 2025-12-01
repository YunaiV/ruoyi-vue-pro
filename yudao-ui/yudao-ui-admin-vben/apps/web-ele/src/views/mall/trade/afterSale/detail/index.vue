<script setup lang="ts">
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallAfterSaleApi } from '#/api/mall/trade/afterSale';
import type { MallOrderApi } from '#/api/mall/trade/order';

import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { confirm, Page, useVbenModal } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { useTabs } from '@vben/hooks';
import { $t } from '@vben/locales';

import { ElCard, ElLoading, ElMessage, ElTag } from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  agreeAfterSale,
  getAfterSale,
  receiveAfterSale,
  refundAfterSale,
  refuseAfterSale,
} from '#/api/mall/trade/afterSale';
import { useDescription } from '#/components/description';
import { DictTag } from '#/components/dict-tag';
import { TableAction } from '#/components/table-action';

import DisagreeForm from '../modules/disagree-form.vue';
import {
  useAfterSaleInfoSchema,
  useOperateLogSchema,
  useOrderInfoSchema,
  useProductColumns,
  useRefundStatusSchema,
} from './data';

defineOptions({ name: 'TradeAfterSaleDetail' });

const route = useRoute();
const router = useRouter();
const tabs = useTabs();

const loading = ref(false);
const afterSaleId = ref(0);
const afterSale = ref<MallAfterSaleApi.AfterSale>({
  order: {},
  orderItem: {},
  logs: [],
});

const [OrderDescriptions] = useDescription({
  title: '订单信息',
  border: false,
  column: 3,
  direction: 'horizontal',
  labelWidth: 140,
  schema: useOrderInfoSchema(),
});

const [AfterSaleDescriptions] = useDescription({
  title: '售后信息',
  border: false,
  column: 3,
  direction: 'horizontal',
  labelWidth: 140,
  schema: useAfterSaleInfoSchema(),
});

const [RefundStatusDescriptions] = useDescription({
  title: '退款状态',
  border: false,
  column: 1,
  direction: 'horizontal',
  labelWidth: 140,
  schema: useRefundStatusSchema(),
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

const [OperateLogGrid, operateLogGridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useOperateLogSchema(),
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

const [DisagreeModal, disagreeModalApi] = useVbenModal({
  connectedComponent: DisagreeForm,
  destroyOnClose: true,
});

/** 获得详情 */
async function getDetail() {
  loading.value = true;
  try {
    const res = await getAfterSale(afterSaleId.value);
    if (res === null) {
      ElMessage.error('售后订单不存在');
      handleBack();
      return;
    }
    afterSale.value = res;
    productGridApi.setGridOptions({ data: [afterSale.value.orderItem] });
    operateLogGridApi.setGridOptions({
      data: afterSale.value.logs || [],
    });
  } finally {
    loading.value = false;
  }
}

/** 同意售后 */
async function handleAgree() {
  await confirm('是否同意售后？');
  const loadingInstance = ElLoading.service({
    text: '正在处理中...',
  });
  try {
    await agreeAfterSale(afterSale.value.id!);
    ElMessage.success($t('ui.actionMessage.operationSuccess'));
    await getDetail();
  } finally {
    loadingInstance.close();
  }
}

/** 拒绝售后 */
function handleDisagree() {
  disagreeModalApi.setData({ afterSale: afterSale.value }).open();
}

/** 确认收货 */
async function handleReceive() {
  await confirm('是否确认收货？');
  const loadingInstance = ElLoading.service({
    text: '正在处理中...',
  });
  try {
    await receiveAfterSale(afterSale.value.id!);
    ElMessage.success($t('ui.actionMessage.operationSuccess'));
    await getDetail();
  } finally {
    loadingInstance.close();
  }
}

/** 拒绝收货 */
async function handleRefuse() {
  await confirm('是否拒绝收货？');
  const loadingInstance = ElLoading.service({
    text: '正在处理中...',
  });
  try {
    await refuseAfterSale(afterSale.value.id!);
    ElMessage.success($t('ui.actionMessage.operationSuccess'));
    await getDetail();
  } finally {
    loadingInstance.close();
  }
}

/** 确认退款 */
async function handleRefund() {
  await confirm('是否确认退款？');
  const loadingInstance = ElLoading.service({
    text: '正在处理中...',
  });
  try {
    await refundAfterSale(afterSale.value.id!);
    ElMessage.success($t('ui.actionMessage.operationSuccess'));
    await getDetail();
  } finally {
    loadingInstance.close();
  }
}

/** 返回列表页 */
function handleBack() {
  tabs.closeCurrentTab();
  router.push({ name: 'TradeAfterSale' });
}

/** 初始化 */
onMounted(() => {
  afterSaleId.value = Number(route.params.id);
  getDetail();
});
</script>

<template>
  <Page auto-content-height :title="afterSale.no" :loading="loading">
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
            label: '同意售后',
            type: 'primary',
            onClick: handleAgree,
            ifShow: afterSale.status === 10,
          },
          {
            label: '拒绝售后',
            type: 'danger',
            link: true,
            onClick: handleDisagree,
            ifShow: afterSale.status === 10,
          },
          {
            label: '确认收货',
            type: 'primary',
            onClick: handleReceive,
            ifShow: afterSale.status === 30,
          },
          {
            label: '拒绝收货',
            type: 'danger',
            link: true,
            onClick: handleRefuse,
            ifShow: afterSale.status === 30,
          },
          {
            label: '确认退款',
            type: 'primary',
            onClick: handleRefund,
            ifShow: afterSale.status === 40,
          },
        ]"
      />
    </template>

    <!-- 拒绝售后弹窗 -->
    <DisagreeModal @success="getDetail" />

    <!-- 订单信息 -->
    <ElCard class="mb-4">
      <OrderDescriptions :data="afterSale" />
    </ElCard>
    <!-- 售后信息 -->
    <ElCard class="mb-4">
      <AfterSaleDescriptions :data="afterSale" />
    </ElCard>
    <!-- 退款状态 -->
    <ElCard class="mb-4">
      <RefundStatusDescriptions :data="afterSale" />
    </ElCard>
    <!-- 商品信息 -->
    <div class="mb-4">
      <ProductGrid table-title="商品信息">
        <template #spuName="{ row }">
          <div class="flex flex-1 flex-col items-start gap-1 text-left">
            <span class="text-sm">{{ row.spuName }}</span>
            <div class="flex flex-wrap gap-1">
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
        </template>
      </ProductGrid>
    </div>
    <!-- 操作日志 -->
    <div>
      <OperateLogGrid table-title="售后日志">
        <template #userType="{ row }">
          <ElTag v-if="row.userId === 0" type="info">系统</ElTag>
          <DictTag v-else :type="DICT_TYPE.USER_TYPE" :value="row.userType" />
        </template>
      </OperateLogGrid>
    </div>
  </Page>
</template>
