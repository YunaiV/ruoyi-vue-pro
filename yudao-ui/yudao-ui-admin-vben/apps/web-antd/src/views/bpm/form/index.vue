<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { BpmFormApi } from '#/api/bpm/form';

import { watch } from 'vue';
import { useRoute } from 'vue-router';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { message } from 'ant-design-vue';

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

/** 新增 */
function handleCreate() {
  router.push({
    name: 'BpmFormEditor',
    query: {
      type: 'create',
    },
  });
}

/** 编辑 */
function handleEdit(row: BpmFormApi.Form) {
  router.push({
    name: 'BpmFormEditor',
    query: {
      id: row.id,
      type: 'edit',
    },
  });
}

/** 复制 */
function handleCopy(row: BpmFormApi.Form) {
  router.push({
    name: 'BpmFormEditor',
    query: {
      copyId: row.id,
      type: 'copy',
    },
  });
}

/** 删除 */
async function handleDelete(row: BpmFormApi.Form) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
  });
  try {
    await deleteForm(row.id as number);
    message.success({
      content: $t('ui.actionMessage.deleteSuccess', [row.name]),
    });
    handleRefresh();
  } finally {
    hideLoading();
  }
}
async function handleDetail(row: BpmFormApi.Form) {
  detailModalApi.setData(row).open();
}

/** 详情弹窗 */
const [DetailModal, detailModalApi] = useVbenModal({
  connectedComponent: Detail,
  destroyOnClose: true,
});

/** 检测路由参数 */
const route = useRoute();

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
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
    cellConfig: {
      height: 64,
    },
  } as VxeTableGridOptions<BpmFormApi.Form>,
});

watch(
  () => route.query.refresh,
  (val) => {
    if (val === '1') {
      handleRefresh();
    }
  },
  { immediate: true },
);
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
              type: 'link',
              icon: ACTION_ICON.COPY,
              auth: ['bpm:form:update'],
              onClick: handleCopy.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['bpm:form:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.detail'),
              type: 'link',
              icon: ACTION_ICON.VIEW,
              auth: ['bpm:form:query'],
              onClick: handleDetail.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
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
