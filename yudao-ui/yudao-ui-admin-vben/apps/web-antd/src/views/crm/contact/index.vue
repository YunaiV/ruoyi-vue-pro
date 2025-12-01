<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmContactApi } from '#/api/crm/contact';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { Button, message, Tabs } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteContact,
  exportContact,
  getContactPage,
} from '#/api/crm/contact';
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
  const data = await exportContact({
    sceneType: sceneType.value,
    ...formValues,
  });
  downloadFileFromBlobPart({ fileName: '联系人.xls', source: data });
}

/** 创建联系人 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑联系人 */
function handleEdit(row: CrmContactApi.Contact) {
  formModalApi.setData(row).open();
}

/** 删除联系人 */
async function handleDelete(row: CrmContactApi.Contact) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
  });
  try {
    await deleteContact(row.id!);
    message.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 查看联系人详情 */
function handleDetail(row: CrmContactApi.Contact) {
  push({ name: 'CrmContactDetail', params: { id: row.id } });
}

/** 查看客户详情 */
function handleCustomerDetail(row: CrmContactApi.Contact) {
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
          return await getContactPage({
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
  } as VxeTableGridOptions<CrmContactApi.Contact>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【客户】客户管理、公海客户"
        url="https://doc.iocoder.cn/crm/customer/"
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
              label: $t('ui.actionTitle.create', ['联系人']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['crm:contact:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['crm:contact:export'],
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
      <template #parentId="{ row }">
        <Button type="link" @click="handleDetail(row)">
          {{ row.parentName }}
        </Button>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['crm:contact:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['crm:contact:delete'],
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
