<!-- 待审核回款 -->
<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmReceivableApi } from '#/api/crm/receivable';

import { useRouter } from 'vue-router';

import { Button } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getReceivablePage } from '#/api/crm/receivable';
import { useGridColumns } from '#/views/crm/receivable/data';

import { AUDIT_STATUS } from '../data';

const { push } = useRouter();

/** 查看审批 */
function handleProcessDetail(row: CrmReceivableApi.Receivable) {
  push({
    name: 'BpmProcessInstanceDetail',
    query: { id: row.processInstanceId },
  });
}

/** 打开回款详情 */
function handleDetail(row: CrmReceivableApi.Receivable) {
  push({ name: 'CrmReceivableDetail', params: { id: row.id } });
}

/** 打开客户详情 */
function handleCustomerDetail(row: CrmReceivableApi.Receivable) {
  push({ name: 'CrmCustomerDetail', params: { id: row.customerId } });
}

/** 打开合同详情 */
function handleContractDetail(row: CrmReceivableApi.Receivable) {
  push({ name: 'CrmContractDetail', params: { id: row.contractId } });
}

const [Grid] = useVbenVxeGrid({
  formOptions: {
    schema: [
      {
        fieldName: 'auditStatus',
        label: '合同状态',
        component: 'Select',
        componentProps: {
          allowClear: true,
          options: AUDIT_STATUS,
        },
        defaultValue: AUDIT_STATUS[0]!.value,
      },
    ],
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getReceivablePage({
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
      search: true,
    },
  } as VxeTableGridOptions<CrmReceivableApi.Receivable>,
});
</script>

<template>
  <Grid>
    <template #no="{ row }">
      <Button type="link" @click="handleDetail(row)">
        {{ row.no }}
      </Button>
    </template>
    <template #customerName="{ row }">
      <Button type="link" @click="handleCustomerDetail(row)">
        {{ row.customerName }}
      </Button>
    </template>
    <template #contractNo="{ row }">
      <Button type="link" @click="handleContractDetail(row)">
        {{ row.contractNo }}
      </Button>
    </template>
    <template #actions="{ row }">
      <Button type="link" @click="handleProcessDetail(row)">查看审批</Button>
    </template>
  </Grid>
</template>
