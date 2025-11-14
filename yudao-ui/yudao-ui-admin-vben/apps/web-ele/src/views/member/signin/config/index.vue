<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MemberSignInConfigApi } from '#/api/member/signin/config';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteSignInConfig,
  getSignInConfigList,
} from '#/api/member/signin/config';
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

/** 创建签到配置 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑签到配置 */
function handleEdit(row: MemberSignInConfigApi.SignInConfig) {
  formModalApi.setData(row).open();
}

/** 删除签到配置 */
async function handleDelete(row: MemberSignInConfigApi.SignInConfig) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting'),
  });
  try {
    await deleteSignInConfig(row.id as number);
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
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
        query: async () => {
          return await getSignInConfigList();
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
  } as VxeTableGridOptions<MemberSignInConfigApi.SignInConfig>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="会员等级、积分、签到"
        url="https://doc.iocoder.cn/member/level/"
      />
    </template>

    <FormModal @success="handleRefresh" />
    <Grid table-title="签到配置列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['签到配置']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['point:sign-in-config:create'],
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
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['point:sign-in-config:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['point:sign-in-config:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.day]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
