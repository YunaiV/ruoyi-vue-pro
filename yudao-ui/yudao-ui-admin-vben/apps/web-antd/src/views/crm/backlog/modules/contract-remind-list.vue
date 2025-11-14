<!-- 即将到期的合同 -->
<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmContractApi } from '#/api/crm/contract';

import { useRouter } from 'vue-router';

import { Button } from 'ant-design-vue';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getContractPage } from '#/api/crm/contract';
import { useGridColumns } from '#/views/crm/contract/data';

import { CONTRACT_EXPIRY_TYPE } from '../data';

const { push } = useRouter();

/** 查看审批 */
function handleProcessDetail(row: CrmContractApi.Contract) {
  push({
    name: 'BpmProcessInstanceDetail',
    query: { id: row.processInstanceId },
  });
}

/** 打开合同详情 */
function handleContractDetail(row: CrmContractApi.Contract) {
  push({ name: 'CrmContractDetail', params: { id: row.id } });
}

/** 打开客户详情 */
function handleCustomerDetail(row: CrmContractApi.Contract) {
  push({ name: 'CrmCustomerDetail', params: { id: row.id } });
}

/** 打开联系人详情 */
function handleContactDetail(row: CrmContractApi.Contract) {
  push({ name: 'CrmContactDetail', params: { id: row.id } });
}

/** 打开商机详情 */
function handleBusinessDetail(row: CrmContractApi.Contract) {
  push({ name: 'CrmBusinessDetail', params: { id: row.id } });
}

const [Grid] = useVbenVxeGrid({
  formOptions: {
    schema: [
      {
        fieldName: 'expiryType',
        label: '到期状态',
        component: 'Select',
        componentProps: {
          allowClear: true,
          options: CONTRACT_EXPIRY_TYPE,
        },
        defaultValue: CONTRACT_EXPIRY_TYPE[0]!.value,
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
          return await getContractPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            sceneType: 1, // 自己负责的
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
  } as VxeTableGridOptions<CrmContractApi.Contract>,
});
</script>

<template>
  <Grid>
    <template #name="{ row }">
      <Button type="link" @click="handleContractDetail(row)">
        {{ row.name }}
      </Button>
    </template>
    <template #customerName="{ row }">
      <Button type="link" @click="handleCustomerDetail(row)">
        {{ row.customerName }}
      </Button>
    </template>
    <template #businessName="{ row }">
      <Button type="link" @click="handleBusinessDetail(row)">
        {{ row.businessName }}
      </Button>
    </template>
    <template #signContactName="{ row }">
      <Button type="link" @click="handleContactDetail(row)">
        {{ row.signContactName }}
      </Button>
    </template>
    <template #actions="{ row }">
      <TableAction
        :actions="[
          {
            label: '查看审批',
            type: 'link',
            auth: ['crm:contract:update'],
            onClick: handleProcessDetail.bind(null, row),
          },
        ]"
      />
    </template>
  </Grid>
</template>
