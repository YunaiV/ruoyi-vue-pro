<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallBrokerageUserApi } from '#/api/mall/trade/brokerage/user';

import { useVbenModal } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getBrokerageUserPage } from '#/api/mall/trade/brokerage/user';

import { useUserListColumns, useUserListFormSchema } from '../data';

defineOptions({ name: 'BrokerageUserListModal' });

const [Modal, modalApi] = useVbenModal({});

const [Grid] = useVbenVxeGrid({
  formOptions: {
    schema: useUserListFormSchema(),
  },
  gridOptions: {
    columns: useUserListColumns(),
    height: '600',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getBrokerageUserPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            bindUserId: modalApi.getData().id,
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
  } as VxeTableGridOptions<MallBrokerageUserApi.BrokerageUser>,
});
</script>

<template>
  <Modal title="推广人列表" class="w-3/5">
    <Grid />
  </Modal>
</template>
