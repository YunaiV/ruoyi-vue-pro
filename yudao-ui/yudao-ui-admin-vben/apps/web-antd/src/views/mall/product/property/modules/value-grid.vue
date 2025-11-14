<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallPropertyApi } from '#/api/mall/product/property';

import { watch } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deletePropertyValue,
  getPropertyValuePage,
} from '#/api/mall/product/property';
import { $t } from '#/locales';

import { useValueGridColumns, useValueGridFormSchema } from '../data';
import ValueForm from './value-form.vue';

const props = defineProps({
  propertyId: {
    type: Number,
    default: undefined,
  },
});

const [ValueFormModal, valueFormModalApi] = useVbenModal({
  connectedComponent: ValueForm,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建属性值 */
function handleCreate() {
  valueFormModalApi.setData({ propertyId: props.propertyId }).open();
}

/** 编辑属性值 */
function handleEdit(row: MallPropertyApi.PropertyValue) {
  valueFormModalApi.setData(row).open();
}

/** 删除属性值 */
async function handleDelete(row: MallPropertyApi.PropertyValue) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
  });
  try {
    await deletePropertyValue(row.id!);
    message.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useValueGridFormSchema(),
  },
  gridOptions: {
    columns: useValueGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getPropertyValuePage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            propertyId: props.propertyId,
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
  } as VxeTableGridOptions<MallPropertyApi.PropertyValue>,
});

/** 监听 propertyId 变化，重新查询 */
watch(
  () => props.propertyId,
  (newPropertyId) => {
    if (newPropertyId) {
      // 设置搜索表单中的 propertyId
      gridApi.formApi.setValues({ propertyId: newPropertyId });
      handleRefresh();
    }
  },
  { immediate: true },
);
</script>

<template>
  <div class="flex h-full flex-col">
    <ValueFormModal @success="handleRefresh" />

    <Grid table-title="属性值列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['属性值']),
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
