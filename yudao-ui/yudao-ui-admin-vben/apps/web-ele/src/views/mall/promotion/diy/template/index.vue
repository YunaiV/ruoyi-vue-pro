<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallDiyTemplateApi } from '#/api/mall/promotion/diy/template';

import { useRouter } from 'vue-router';

import { confirm, DocAlert, Page, useVbenModal } from '@vben/common-ui';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteDiyTemplate,
  getDiyTemplatePage,
  useDiyTemplate,
} from '#/api/mall/promotion/diy/template';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import DiyTemplateForm from './modules/form.vue';

defineOptions({ name: 'PromotionDiyTemplate' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: DiyTemplateForm,
  destroyOnClose: true,
});

const router = useRouter();

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建 DIY 模板 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑 DIY 模板 */
function handleEdit(row: MallDiyTemplateApi.DiyTemplate) {
  formModalApi.setData(row).open();
}

/** 装修模板 */
function handleDecorate(row: MallDiyTemplateApi.DiyTemplate) {
  router.push({ name: 'DiyTemplateDecorate', params: { id: row.id } });
}

/** 使用模板 */
async function handleUse(row: MallDiyTemplateApi.DiyTemplate) {
  await confirm(`是否使用模板"${row.name}"?`);
  const loadingInstance = ElLoading.service({
    text: `正在使用模板"${row.name}"...`,
  });
  try {
    await useDiyTemplate(row.id as number);
    ElMessage.success('使用成功');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 删除 DIY 模板 */
async function handleDelete(row: MallDiyTemplateApi.DiyTemplate) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.name]),
  });
  try {
    await deleteDiyTemplate(row.id as number);
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
          return await getDiyTemplatePage({
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
  } as VxeTableGridOptions<MallDiyTemplateApi.DiyTemplate>,
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

    <Grid table-title="装修模板列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['装修模板']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['promotion:diy-template:create'],
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
              auth: ['promotion:diy-template:update'],
              onClick: handleDecorate.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['promotion:diy-template:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: '使用',
              type: 'primary',
              link: true,
              auth: ['promotion:diy-template:use'],
              ifShow: !row.used,
              onClick: handleUse.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['promotion:diy-template:delete'],
              ifShow: !row.used,
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
