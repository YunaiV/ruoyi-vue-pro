<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MpTagApi } from '#/api/mp/tag';

import { onMounted, ref } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getSimpleAccountList } from '#/api/mp/account';
import { deleteTag, getTagPage, syncTag } from '#/api/mp/tag';
import { $t } from '#/locales';

import { useGridColumns } from './data';
import Form from './modules/form.vue';

const accountId = ref(-1);
const accountOptions = ref<{ label: string; value: number }[]>([]);

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

async function getAccountList() {
  const res = await getSimpleAccountList();
  if (res.length > 0) {
    accountId.value = res[0]?.id as number;
    accountOptions.value = res.map((item) => ({
      label: item.name,
      value: item.id,
    }));
    gridApi.formApi.setValues({
      accountId: accountId.value,
    });
  } else {
    message.error('未配置公众号，请在【公众号管理 -> 账号管理】菜单，进行配置');
    // await push({ name: 'MpAccount' });
    // tabs.closeCurrentTab();
  }
  gridApi.setState({
    formOptions: {
      schema: [
        {
          fieldName: 'accountId',
          label: '公众号',
          component: 'Select',
          componentProps: {
            options: accountOptions.value || [],
          },
        },
      ],
    },
  });
}
/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建标签 */
function handleCreate() {
  formModalApi.setData({ accountId: accountId.value }).open();
}

/** 编辑标签 */
function handleEdit(row: MpTagApi.Tag) {
  formModalApi.setData({ row, accountId: accountId.value }).open();
}

/** 删除标签 */
async function handleDelete(row: MpTagApi.Tag) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
  });
  try {
    await deleteTag(row.id as number);
    message.success({
      content: $t('ui.actionMessage.deleteSuccess', [row.name]),
    });
    handleRefresh();
  } finally {
    hideLoading();
  }
}
/** 同步标签 */
async function handleSync() {
  const hideLoading = message.loading({
    content: '是否确认同步标签？',
    duration: 0,
  });
  try {
    await syncTag(accountId.value);
    message.success({
      content: '同步标签成功',
    });
    handleRefresh();
  } finally {
    hideLoading();
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: [],
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          accountId.value = formValues.accountId;
          return await getTagPage({
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
  } as VxeTableGridOptions<MpTagApi.Tag>,
});

onMounted(async () => {
  await getAccountList();
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="公众号标签列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['公众号标签']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['mp:tag:create'],
              onClick: handleCreate,
            },
            {
              label: '同步',
              type: 'primary',
              icon: 'lucide:refresh-ccw',
              auth: ['mp:tag:sync'],
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
              auth: ['mp:tag:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['mp:tag:delete'],
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
