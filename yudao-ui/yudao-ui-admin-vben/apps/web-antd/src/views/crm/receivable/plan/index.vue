<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmReceivablePlanApi } from '#/api/crm/receivable/plan';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { Button, message, Tabs } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteReceivablePlan,
  exportReceivablePlan,
  getReceivablePlanPage,
} from '#/api/crm/receivable/plan';
import { $t } from '#/locales';

import ReceivableForm from '../modules/form.vue';
import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const { push } = useRouter();
const sceneType = ref('1');

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const [ReceivableFormModal, receivableFormModalApi] = useVbenModal({
  connectedComponent: ReceivableForm,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 处理场景类型的切换 */
function handleChangeSceneType(key: number | string) {
  sceneType.value = key.toString();
  gridApi.query();
}

/** 导出表格 */
async function handleExport() {
  const formValues = await gridApi.formApi.getValues();
  const data = await exportReceivablePlan({
    sceneType: sceneType.value,
    ...formValues,
  });
  downloadFileFromBlobPart({ fileName: '回款计划.xls', source: data });
}

/** 创建回款计划 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑回款计划 */
function handleEdit(row: CrmReceivablePlanApi.Plan) {
  formModalApi.setData(row).open();
}

/** 删除回款计划 */
async function handleDelete(row: CrmReceivablePlanApi.Plan) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.period]),
    duration: 0,
  });
  try {
    await deleteReceivablePlan(row.id!);
    message.success($t('ui.actionMessage.deleteSuccess', [row.period]));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 创建回款计划 */
function handleCreateReceivable(row: CrmReceivablePlanApi.Plan) {
  receivableFormModalApi.setData({ plan: row }).open();
}

/** 查看回款计划详情 */
function handleDetail(row: CrmReceivablePlanApi.Plan) {
  push({ name: 'CrmReceivablePlanDetail', params: { id: row.id } });
}

/** 查看客户详情 */
function handleCustomerDetail(row: CrmReceivablePlanApi.Plan) {
  push({ name: 'CrmCustomerDetail', params: { id: row.customerId } });
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getReceivablePlanPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            sceneType: sceneType.value,
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
  } as VxeTableGridOptions<CrmReceivablePlanApi.Plan>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【回款】回款管理、回款计划"
        url="https://doc.iocoder.cn/crm/receivable/"
      />
      <DocAlert
        title="【通用】数据权限"
        url="https://doc.iocoder.cn/crm/permission/"
      />
    </template>

    <FormModal @success="handleRefresh" />
    <ReceivableFormModal @success="handleRefresh" />
    <Grid>
      <template #toolbar-actions>
        <Tabs class="w-full" @change="handleChangeSceneType">
          <Tabs.TabPane tab="我负责的" key="1" />
          <Tabs.TabPane tab="下属负责的" key="3" />
        </Tabs>
      </template>
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['回款计划']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['crm:receivable-plan:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['crm:receivable-plan:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
      <template #customerName="{ row }">
        <Button type="link" @click="handleCustomerDetail(row)">
          {{ row.customerName }}
        </Button>
      </template>
      <template #period="{ row }">
        <Button type="link" @click="handleDetail(row)">
          {{ row.period }}
        </Button>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['回款']),
              type: 'link',
              icon: ACTION_ICON.ADD,
              auth: ['crm:receivable:create'],
              onClick: handleCreateReceivable.bind(null, row),
              ifShow: !row.receivableId,
            },
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['crm:receivable-plan:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['crm:receivable-plan:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.period]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
