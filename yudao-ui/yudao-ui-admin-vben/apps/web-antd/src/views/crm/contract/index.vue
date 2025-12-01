<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmContractApi } from '#/api/crm/contract';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { Button, message, Tabs } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteContract,
  exportContract,
  getContractPage,
  submitContract,
} from '#/api/crm/contract';
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
  const data = await exportContract({
    sceneType: sceneType.value,
    ...formValues,
  });
  downloadFileFromBlobPart({ fileName: '合同.xls', source: data });
}

/** 创建合同 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑合同 */
function handleEdit(row: CrmContractApi.Contract) {
  formModalApi.setData(row).open();
}

/** 删除合同 */
async function handleDelete(row: CrmContractApi.Contract) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
  });
  try {
    await deleteContract(row.id!);
    message.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 提交审核 */
async function handleSubmit(row: CrmContractApi.Contract) {
  const hideLoading = message.loading({
    content: '提交审核中...',
    duration: 0,
  });
  try {
    await submitContract(row.id!);
    message.success('提交审核成功');
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 查看合同详情 */
function handleDetail(row: CrmContractApi.Contract) {
  push({ name: 'CrmContractDetail', params: { id: row.id } });
}

/** 查看客户详情 */
function handleCustomerDetail(row: CrmContractApi.Contract) {
  push({ name: 'CrmCustomerDetail', params: { id: row.customerId } });
}

/** 查看联系人详情 */
function handleContactDetail(row: CrmContractApi.Contract) {
  push({ name: 'CrmContactDetail', params: { id: row.signContactId } });
}

/** 查看商机详情 */
function handleBusinessDetail(row: CrmContractApi.Contract) {
  push({ name: 'CrmBusinessDetail', params: { id: row.businessId } });
}

/** 查看审批详情 */
function handleProcessDetail(row: CrmContractApi.Contract) {
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
          return await getContractPage({
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
  } as VxeTableGridOptions<CrmContractApi.Contract>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【合同】合同管理、合同提醒"
        url="https://doc.iocoder.cn/crm/contract/"
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
              label: $t('ui.actionTitle.create', ['合同']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['crm:contract:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['crm:contract:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
      <template #name="{ row }">
        <Button type="link" @click="handleDetail(row)">
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
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['crm:contract:update'],
              onClick: handleEdit.bind(null, row),
              ifShow: row.auditStatus === 0,
            },
            {
              label: '提交审核',
              type: 'link',
              auth: ['crm:contract:update'],
              onClick: handleSubmit.bind(null, row),
              ifShow: row.auditStatus === 0,
            },
            {
              label: '查看审批',
              type: 'link',
              auth: ['crm:contract:update'],
              onClick: handleProcessDetail.bind(null, row),
              ifShow: row.auditStatus !== 0,
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              auth: ['crm:contract:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.name]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
