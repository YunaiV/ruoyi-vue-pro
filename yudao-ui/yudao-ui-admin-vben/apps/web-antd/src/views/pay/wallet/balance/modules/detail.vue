<script setup lang="ts">
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { PayWalletApi } from '#/api/pay/wallet/balance';
import type { WalletTransactionApi } from '#/api/pay/wallet/transaction';

import { Page, useVbenModal } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getTransactionPage } from '#/api/pay/wallet/transaction';

import { useTransactionGridColumns } from '../data';

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useTransactionGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }) => {
          return await getTransactionPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            walletId: modalApi.getData<PayWalletApi.Wallet>().id,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      enabled: false,
    },
  } as VxeTableGridOptions<WalletTransactionApi.Transaction>,
});

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      return;
    }
    // 加载数据
    modalApi.lock();
    try {
      await gridApi.query();
    } finally {
      modalApi.unlock();
    }
  },
});
</script>
<template>
  <Modal
    title="钱包交易记录"
    class="w-1/2"
    :show-cancel-button="false"
    :show-confirm-button="false"
  >
    <Page auto-content-height>
      <Grid />
    </Page>
  </Modal>
</template>
