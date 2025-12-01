<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { BpmFormApi } from '#/api/bpm/form';

import { onActivated } from 'vue';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteForm, getFormPage } from '#/api/bpm/form';
import { router } from '#/router';

import { useGridColumns, useGridFormSchema } from './data';
import Detail from './modules/detail.vue';

defineOptions({ name: 'BpmForm' });

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 新增表单 */
function handleCreate() {
  router.push({
    name: 'BpmFormEditor',
    query: {
      type: 'create',
    },
  });
}

/** 编辑表单 */
function handleEdit(row: BpmFormApi.Form) {
  router.push({
    name: 'BpmFormEditor',
    query: {
      id: row.id,
      type: 'edit',
    },
  });
}

/** 复制表单 */
function handleCopy(row: BpmFormApi.Form) {
  router.push({
    name: 'BpmFormEditor',
    query: {
      copyId: row.id,
      type: 'copy',
    },
  });
}

/** 删除表单 */
async function handleDelete(row: BpmFormApi.Form) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.name]),
  });
  try {
    await deleteForm(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 查看表单详情 */
async function handleDetail(row: BpmFormApi.Form) {
  detailModalApi.setData(row).open();
}

const [DetailModal, detailModalApi] = useVbenModal({
  connectedComponent: Detail,
  destroyOnClose: true,
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
          return await getFormPage({
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
  } as VxeTableGridOptions<BpmFormApi.Form>,
});

/** 激活时 */
onActivated(() => {
  handleRefresh();
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="审批接入（流程表单）"
        url="https://doc.iocoder.cn/bpm/use-bpm-form/"
      />
    </template>

    <DetailModal />
    <Grid table-title="流程表单">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['流程表单']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['bpm:form:create'],
              onClick: handleCreate,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.copy'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.COPY,
              auth: ['bpm:form:update'],
              onClick: handleCopy.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['bpm:form:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.detail'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.VIEW,
              auth: ['bpm:form:query'],
              onClick: handleDetail.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['bpm:form:delete'],
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
