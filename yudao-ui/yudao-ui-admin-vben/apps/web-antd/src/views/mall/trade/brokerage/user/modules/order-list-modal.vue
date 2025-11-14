<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallBrokerageRecordApi } from '#/api/mall/trade/brokerage/record';

import { useVbenModal } from '@vben/common-ui';
import { BrokerageRecordBizTypeEnum } from '@vben/constants';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getBrokerageRecordPage } from '#/api/mall/trade/brokerage/record';

import { useOrderListColumns, useOrderListFormSchema } from '../data';

/** 推广订单列表 */
defineOptions({ name: 'BrokerageOrderListModal' });

const [Modal, modalApi] = useVbenModal({});

const [Grid] = useVbenVxeGrid({
  formOptions: {
    schema: useOrderListFormSchema(),
  },
  gridOptions: {
    columns: useOrderListColumns(),
    height: '600',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          const params = {
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            userId: modalApi.getData()?.id,
            bizType: BrokerageRecordBizTypeEnum.ORDER.type,
            sourceUserLevel:
              formValues.sourceUserLevel === 0
                ? undefined
                : formValues.sourceUserLevel,
            status: formValues.status,
            createTime: formValues.createTime,
          };
          return await getBrokerageRecordPage(params);
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
  } as VxeTableGridOptions<MallBrokerageRecordApi.BrokerageRecord>,
});
</script>

<template>
  <Modal title="推广订单列表" class="w-3/5">
    <Grid />
  </Modal>
</template>
