<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { BpmUserGroupApi } from '#/api/bpm/userGroup';
import type { SystemUserApi } from '#/api/system/user';

import { onMounted, ref } from 'vue';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';

import { message, Tag } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteUserGroup, getUserGroupPage } from '#/api/bpm/userGroup';
import { getSimpleUserList } from '#/api/system/user';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建用户分组 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑用户分组 */
function handleEdit(row: BpmUserGroupApi.UserGroup) {
  formModalApi.setData(row).open();
}

/** 删除用户分组 */
async function handleDelete(row: BpmUserGroupApi.UserGroup) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
  });
  try {
    await deleteUserGroup(row.id as number);
    message.success({
      content: $t('ui.actionMessage.deleteSuccess', [row.name]),
    });
    handleRefresh();
  } catch {
    hideLoading();
  }
}

const userList = ref<SystemUserApi.User[]>([]);
/** 初始化 */
onMounted(async () => {
  // 加载用户列表
  userList.value = await getSimpleUserList();
});

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
          return await getUserGroupPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<BpmUserGroupApi.UserGroup>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="工作流手册" url="https://doc.iocoder.cn/bpm/" />
    </template>

    <FormModal @success="handleRefresh" />
    <Grid table-title="用户分组">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['用户分组']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['bpm:user-group:create'],
              onClick: handleCreate,
            },
          ]"
        />
      </template>
      <template #userIds="{ row }">
        <Tag v-for="userId in row.userIds" :key="userId" color="blue">
          {{ userList.find((u) => u.id === userId)?.nickname }}
        </Tag>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['bpm:user-group:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['bpm:user-group:delete'],
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
