<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallDiyPageApi } from '#/api/mall/promotion/diy/page';

import { useRouter } from 'vue-router';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteDiyPage, getDiyPagePage } from '#/api/mall/promotion/diy/page';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import DiyPageForm from './modules/form.vue';

defineOptions({ name: 'PromotionDiyPage' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: DiyPageForm,
  destroyOnClose: true,
});

const { push } = useRouter();

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建 DIY 页面 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑 DIY 页面 */
function handleEdit(row: MallDiyPageApi.DiyPage) {
  formModalApi.setData(row).open();
}

/** 装修页面 */
function handleDecorate(row: MallDiyPageApi.DiyPage) {
  push({ name: 'DiyPageDecorate', params: { id: row.id } });
}

/** 删除 DIY 页面 */
async function handleDelete(row: MallDiyPageApi.DiyPage) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.name]),
  });
  try {
    await deleteDiyPage(row.id as number);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    loadingInstance.close();
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
          return await getDiyPagePage({
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
  } as VxeTableGridOptions<MallDiyPageApi.DiyPage>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【营销】商城装修"
        url="https://doc.iocoder.cn/mall/diy/"
      />
    </template>

    <FormModal @success="handleRefresh" />

    <Grid table-title="装修页面列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['装修页面']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['promotion:diy-page:create'],
              onClick: handleCreate,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '装修',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['promotion:diy-page:update'],
              onClick: handleDecorate.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['promotion:diy-page:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['promotion:diy-page:delete'],
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
