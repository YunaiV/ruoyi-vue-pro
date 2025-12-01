<script setup lang="ts">
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { IotDeviceGroupApi } from '#/api/iot/device/group';

import { Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteDeviceGroup, getDeviceGroupPage } from '#/api/iot/device/group';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import DeviceGroupForm from './modules/device-group-form.vue';

defineOptions({ name: 'IoTDeviceGroup' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: DeviceGroupForm,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建设备分组 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑设备分组 */
function handleEdit(row: IotDeviceGroupApi.DeviceGroup) {
  formModalApi.setData(row).open();
}

/** 删除设备分组 */
async function handleDelete(row: IotDeviceGroupApi.DeviceGroup) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
  });
  try {
    await deleteDeviceGroup(row.id as number);
    message.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    hideLoading();
  }
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
          return await getDeviceGroupPage({
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
  } as VxeTableGridOptions<IotDeviceGroupApi.DeviceGroup>,
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="设备分组列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['设备分组']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['iot:device-group:create'],
              onClick: handleCreate,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['iot:device-group:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['iot:device-group:delete'],
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
