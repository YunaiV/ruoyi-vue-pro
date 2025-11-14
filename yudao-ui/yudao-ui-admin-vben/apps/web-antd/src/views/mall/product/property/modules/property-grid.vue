<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallPropertyApi } from '#/api/mall/product/property';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteProperty, getPropertyPage } from '#/api/mall/product/property';
import { $t } from '#/locales';

import { usePropertyGridColumns, usePropertyGridFormSchema } from '../data';
import PropertyForm from './property-form.vue';

const emit = defineEmits(['select']);

const [PropertyFormModal, propertyFormModalApi] = useVbenModal({
  connectedComponent: PropertyForm,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建属性 */
function handleCreate() {
  propertyFormModalApi.setData(null).open();
}

/** 编辑属性 */
function handleEdit(row: MallPropertyApi.Property) {
  propertyFormModalApi.setData(row).open();
}

/** 删除属性 */
async function handleDelete(row: MallPropertyApi.Property) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
  });
  try {
    await deleteProperty(row.id!);
    message.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: usePropertyGridFormSchema(),
  },
  gridOptions: {
    columns: usePropertyGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getPropertyPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isCurrent: true,
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MallPropertyApi.Property>,
  gridEvents: {
    cellClick: ({ row }: { row: MallPropertyApi.Property }) => {
      emit('select', row.id);
    },
  },
});
</script>

<template>
  <div class="h-full">
    <PropertyFormModal @success="handleRefresh" />
    <Grid table-title="属性列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['属性']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['product:property:create'],
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
              auth: ['product:property:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['product:property:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.name]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </div>
</template>
