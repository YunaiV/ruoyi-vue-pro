<!-- 待进入公海的客户 -->
<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmCustomerApi } from '#/api/crm/customer';

import { useRouter } from 'vue-router';

import { Button } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getCustomerPage } from '#/api/crm/customer';
import { useGridColumns } from '#/views/crm/customer/data';

import { SCENE_TYPES } from '../data';

const { push } = useRouter();

/** 打开客户详情 */
function handleDetail(row: CrmCustomerApi.Customer) {
  push({ name: 'CrmCustomerDetail', params: { id: row.id } });
}

const [Grid] = useVbenVxeGrid({
  formOptions: {
    schema: [
      {
        fieldName: 'sceneType',
        label: '归属',
        component: 'Select',
        componentProps: {
          allowClear: true,
          options: SCENE_TYPES,
        },
        defaultValue: SCENE_TYPES[0]!.value,
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
          return await getCustomerPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            pool: true, // 固定 公海参数为 true
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
  } as VxeTableGridOptions<CrmCustomerApi.Customer>,
});
</script>

<template>
  <Grid>
    <template #name="{ row }">
      <Button type="link" @click="handleDetail(row)">{{ row.name }}</Button>
    </template>
    <template #actions="{ row }">
      <Button type="link" @click="handleDetail(row)">查看详情</Button>
    </template>
  </Grid>
</template>
