<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MpUserApi } from '#/api/mp/user';

import { confirm, DocAlert, Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getUserPage, syncUser } from '#/api/mp/user';
import { $t } from '#/locales';
import { WxAccountSelect } from '#/views/mp/components';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

defineOptions({ name: 'MpUser' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 公众号变化时查询数据 */
function handleAccountChange(accountId: number) {
  gridApi.formApi.setValues({ accountId });
  gridApi.formApi.submitForm();
}

/** 编辑用户 */
function handleEdit(row: MpUserApi.User) {
  formModalApi.setData({ id: row.id }).open();
}

/** 同步粉丝 */
async function handleSync() {
  const formValues = await gridApi.formApi.getValues();
  const accountId = formValues.accountId;
  if (!accountId) {
    message.warning('请先选择公众号');
    return;
  }

  await confirm('是否确认同步粉丝？');
  const hideLoading = message.loading({
    content: '正在同步粉丝...',
    duration: 0,
  });
  try {
    await syncUser(accountId);
    message.success(
      '开始从微信公众号同步粉丝信息，同步需要一段时间，建议稍后再查询',
    );
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
          return await getUserPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
      autoLoad: false,
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MpUserApi.User>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="公众号粉丝" url="https://doc.iocoder.cn/mp/user/" />
    </template>

    <FormModal @success="handleRefresh" />

    <Grid table-title="粉丝列表">
      <template #form-accountId>
        <WxAccountSelect @change="handleAccountChange" />
      </template>
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '同步',
              type: 'primary',
              icon: ACTION_ICON.REFRESH,
              auth: ['mp:user:sync'],
              onClick: handleSync,
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
              auth: ['mp:user:update'],
              onClick: handleEdit.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
