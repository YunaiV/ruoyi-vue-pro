<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmReceivableApi } from '#/api/crm/receivable';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { Button, message, Tabs } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteReceivable,
  exportReceivable,
  getReceivablePage,
  submitReceivable,
} from '#/api/crm/receivable';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const { push } = useRouter();
const sceneType = ref('1');

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
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
  const data = await exportReceivable({
    sceneType: sceneType.value,
    ...formValues,
  });
  downloadFileFromBlobPart({ fileName: '回款.xls', source: data });
}

/** 创建回款 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑回款 */
function handleEdit(row: CrmReceivableApi.Receivable) {
  formModalApi.setData({ receivable: row }).open();
}

/** 删除回款 */
async function handleDelete(row: CrmReceivableApi.Receivable) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.no]),
    duration: 0,
  });
  try {
    await deleteReceivable(row.id!);
    message.success($t('ui.actionMessage.deleteSuccess', [row.no]));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 提交审核 */
async function handleSubmit(row: CrmReceivableApi.Receivable) {
  const hideLoading = message.loading({
    content: '提交审核中...',
    duration: 0,
  });
  try {
    await submitReceivable(row.id!);
    message.success('提交审核成功');
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 查看回款详情 */
function handleDetail(row: CrmReceivableApi.Receivable) {
  push({ name: 'CrmReceivableDetail', params: { id: row.id } });
}

/** 查看客户详情 */
function handleCustomerDetail(row: CrmReceivableApi.Receivable) {
  push({ name: 'CrmCustomerDetail', params: { id: row.customerId } });
}

/** 查看合同详情 */
function handleContractDetail(row: CrmReceivableApi.Receivable) {
  push({ name: 'CrmContractDetail', params: { id: row.contractId } });
}

/** 查看审批详情 */
function handleProcessDetail(row: CrmReceivableApi.Receivable) {
  push({
    name: 'BpmProcessInstanceDetail',
    query: { id: row.processInstanceId },
  });
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
          return await getReceivablePage({
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
  } as VxeTableGridOptions<CrmReceivableApi.Receivable>,
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
    <Grid>
      <template #toolbar-actions>
        <Tabs class="w-full" @change="handleChangeSceneType">
          <Tabs.TabPane tab="我负责的" key="1" />
          <Tabs.TabPane tab="我参与的" key="2" />
          <Tabs.TabPane tab="下属负责的" key="3" />
        </Tabs>
      </template>
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['回款']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['crm:receivable:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['crm:receivable:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
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
        <Button
          v-if="row.contract"
          type="link"
          @click="handleContractDetail(row)"
        >
          {{ row.contract.no }}
        </Button>
        <span v-else>--</span>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['crm:receivable:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: '提交审核',
              type: 'link',
              auth: ['crm:receivable:update'],
              onClick: handleSubmit.bind(null, row),
              ifShow: row.auditStatus === 0,
            },
            {
              label: '查看审批',
              type: 'link',
              auth: ['crm:receivable:update'],
              onClick: handleProcessDetail.bind(null, row),
              ifShow: row.auditStatus !== 0,
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['crm:receivable:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.no]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
