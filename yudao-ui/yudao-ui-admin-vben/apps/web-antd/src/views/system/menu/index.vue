<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemMenuApi } from '#/api/system/menu';

import { ref } from 'vue';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { SystemMenuTypeEnum } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteMenu, getMenuList } from '#/api/system/menu';
import { $t } from '#/locales';

import { useGridColumns } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建菜单 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 添加下级菜单 */
function handleAppend(row: SystemMenuApi.Menu) {
  formModalApi.setData({ parentId: row.id }).open();
}

/** 编辑菜单 */
function handleEdit(row: SystemMenuApi.Menu) {
  formModalApi.setData(row).open();
}

/** 删除菜单 */
async function handleDelete(row: SystemMenuApi.Menu) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
  });
  try {
    await deleteMenu(row.id!);
    message.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 切换树形展开/收缩状态 */
const isExpanded = ref(false);
function handleExpand() {
  isExpanded.value = !isExpanded.value;
  gridApi.grid.setAllTreeExpand(isExpanded.value);
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: false,
    },
    proxyConfig: {
      ajax: {
        query: async (_params) => {
          return await getMenuList();
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
    },
    treeConfig: {
      parentField: 'parentId',
      rowField: 'id',
      transform: true,
      reserve: true,
    },
  } as VxeTableGridOptions<SystemMenuApi.Menu>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="功能权限"
        url="https://doc.iocoder.cn/resource-permission"
      />
      <DocAlert title="菜单路由" url="https://doc.iocoder.cn/vue3/route/" />
    </template>

    <FormModal @success="handleRefresh" />
    <Grid table-title="菜单列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['菜单']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['system:menu:create'],
              onClick: handleCreate,
            },
            {
              label: isExpanded ? '收缩' : '展开',
              type: 'primary',
              onClick: handleExpand,
            },
          ]"
        />
      </template>
      <template #name="{ row }">
        <div class="flex w-full items-center gap-1">
          <div class="size-5 flex-shrink-0">
            <IconifyIcon
              v-if="row.type === SystemMenuTypeEnum.BUTTON"
              icon="carbon:square-outline"
              class="size-full"
            />
            <IconifyIcon
              v-else-if="row.icon"
              :icon="row.icon || 'carbon:circle-dash'"
              class="size-full"
            />
          </div>
          <span class="flex-auto">{{ $t(row.name) }}</span>
          <div class="items-center justify-end"></div>
        </div>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '新增下级',
              type: 'link',
              icon: ACTION_ICON.ADD,
              auth: ['system:menu:create'],
              onClick: handleAppend.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['system:menu:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['system:menu:delete'],
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
