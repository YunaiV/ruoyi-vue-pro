<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { DemoOrderApi } from '#/api/pay/demo/order';

import { useRouter } from 'vue-router';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getDemoOrderPage, refundDemoOrder } from '#/api/pay/demo/order';

import { useGridColumns } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const router = useRouter();

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建订单 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 支付按钮操作 */
function handlePay(row: DemoOrderApi.Order) {
  router.push({
    name: 'PayCashier',
    query: {
      id: row.payOrderId,
      returnUrl: encodeURIComponent(`/pay/demo/order?id=${row.id}`),
    },
  });
}

/** 退款按钮操作 */
async function handleRefund(row: DemoOrderApi.Order) {
  const hideLoading = message.loading({
    content: '退款中，请稍后...',
    duration: 0,
  });
  try {
    await refundDemoOrder(row.id as number);
    message.success('发起退款成功！');
    handleRefresh();
  } finally {
    hideLoading();
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getDemoOrderPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
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
    },
  } as VxeTableGridOptions<DemoOrderApi.Order>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="支付宝支付接入"
        url="https://doc.iocoder.cn/pay/alipay-pay-demo/"
      />
      <DocAlert
        title="支付宝、微信退款接入"
        url="https://doc.iocoder.cn/pay/refund-demo/"
      />
      <DocAlert
        title="微信公众号支付接入"
        url="https://doc.iocoder.cn/pay/wx-pub-pay-demo/"
      />
      <DocAlert
        title="微信小程序支付接入"
        url="https://doc.iocoder.cn/pay/wx-lite-pay-demo/"
      />
    </template>

    <FormModal @success="handleRefresh" />
    <Grid table-title="示例订单列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '发起订单',
              type: 'primary',
              icon: ACTION_ICON.ADD,
              onClick: handleCreate,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '前往支付',
              type: 'link',
              icon: ACTION_ICON.ADD,
              ifShow: !row.payStatus,
              onClick: handlePay.bind(null, row),
            },
            {
              label: '发起退款',
              type: 'link',
              danger: true,
              icon: ACTION_ICON.EDIT,
              ifShow: row.payStatus && !row.payRefundId,
              popConfirm: {
                title: '确定发起退款吗？',
                confirm: handleRefund.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
