<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmCustomerApi } from '#/api/crm/customer';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import {
  ElButton,
  ElLoading,
  ElMessage,
  ElTabPane,
  ElTabs,
} from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteCustomer,
  exportCustomer,
  getCustomerPage,
} from '#/api/crm/customer';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';
import ImportForm from './modules/import-form.vue';

const { push } = useRouter();
const sceneType = ref('1');

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const [ImportModal, importModalApi] = useVbenModal({
  connectedComponent: ImportForm,
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

/** 导入客户 */
function handleImport() {
  importModalApi.open();
}

/** 导出表格 */
async function handleExport() {
  const formValues = await gridApi.formApi.getValues();
  const data = await exportCustomer({
    sceneType: sceneType.value,
    ...formValues,
  });
  downloadFileFromBlobPart({ fileName: '客户.xls', source: data });
}

/** 创建客户 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑客户 */
function handleEdit(row: CrmCustomerApi.Customer) {
  formModalApi.setData(row).open();
}

/** 删除客户 */
async function handleDelete(row: CrmCustomerApi.Customer) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.name]),
  });
  try {
    await deleteCustomer(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 查看客户详情 */
function handleDetail(row: CrmCustomerApi.Customer) {
  push({ name: 'CrmCustomerDetail', params: { id: row.id } });
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
          return await getCustomerPage({
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
  } as VxeTableGridOptions<CrmCustomerApi.Customer>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【客户】客户管理"
        url="https://doc.iocoder.cn/crm/customer/"
      />
      <DocAlert
        title="【通用】数据权限"
        url="https://doc.iocoder.cn/crm/permission/"
      />
    </template>

    <FormModal @success="handleRefresh" />
    <ImportModal @success="handleRefresh" />
    <Grid>
      <template #toolbar-actions>
        <ElTabs
          class="w-full"
          @tab-change="handleChangeSceneType"
          v-model:model-value="sceneType"
        >
          <ElTabPane label="我负责的" name="1" />
          <ElTabPane label="我参与的" name="2" />
          <ElTabPane label="下属负责的" name="3" />
        </ElTabs>
      </template>
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['客户']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['crm:customer:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.import'),
              type: 'primary',
              icon: ACTION_ICON.UPLOAD,
              auth: ['crm:customer:import'],
              onClick: handleImport,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['crm:customer:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
      <template #name="{ row }">
        <ElButton type="primary" link @click="handleDetail(row)">
          {{ row.name }}
        </ElButton>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['crm:customer:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['crm:customer:delete'],
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
