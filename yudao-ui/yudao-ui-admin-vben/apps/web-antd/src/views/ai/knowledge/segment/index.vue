<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { AiKnowledgeKnowledgeApi } from '#/api/ai/knowledge/knowledge';
import type { AiKnowledgeSegmentApi } from '#/api/ai/knowledge/segment';

import { onMounted } from 'vue';
import { useRoute } from 'vue-router';

import { useAccess } from '@vben/access';
import { confirm, Page, useVbenModal } from '@vben/common-ui';
import { CommonStatusEnum } from '@vben/constants';

import { message, Switch } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteKnowledgeSegment,
  getKnowledgeSegmentPage,
  updateKnowledgeSegmentStatus,
} from '#/api/ai/knowledge/segment';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const route = useRoute();
const { hasAccessByCodes } = useAccess();
const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建 */
function handleCreate() {
  formModalApi.setData({ documentId: route.query.documentId }).open();
}

/** 编辑 */
function handleEdit(row: AiKnowledgeKnowledgeApi.Knowledge) {
  formModalApi.setData(row).open();
}

/** 删除 */
async function handleDelete(row: AiKnowledgeKnowledgeApi.Knowledge) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.id]),
    duration: 0,
  });
  try {
    await deleteKnowledgeSegment(row.id as number);
    message.success({
      content: $t('ui.actionMessage.deleteSuccess', [row.id]),
    });
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
          return await getKnowledgeSegmentPage({
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
  } as VxeTableGridOptions<AiKnowledgeKnowledgeApi.Knowledge>,
});

/** 修改是否发布 */
async function handleStatusChange(row: AiKnowledgeSegmentApi.KnowledgeSegment) {
  try {
    // 修改状态的二次确认
    const text = row.status ? '启用' : '禁用';
    await confirm(`确认要"${text}"该分段吗?`).then(async () => {
      await updateKnowledgeSegmentStatus({
        id: row.id,
        status: row.status,
      });
      gridApi.reload();
    });
  } catch {
    row.status =
      row.status === CommonStatusEnum.ENABLE
        ? CommonStatusEnum.DISABLE
        : CommonStatusEnum.ENABLE;
  }
}

onMounted(() => {
  gridApi.formApi.setFieldValue('documentId', route.query.documentId);
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="分段列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['分段']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['ai:knowledge:create'],
              onClick: handleCreate,
            },
          ]"
        />
      </template>
      <template #status="{ row }">
        <Switch
          v-model:checked="row.status"
          :checked-value="0"
          :un-checked-value="1"
          @change="handleStatusChange(row)"
          :disabled="!hasAccessByCodes(['ai:knowledge:update'])"
        />
      </template>
      <template #expand_content="{ row }">
        <div
          class="whitespace-pre-wrap border-l-4 border-blue-500 px-2.5 py-5 leading-5"
        >
          <div class="mb-2 text-sm font-bold text-gray-600">完整内容：</div>
          {{ row.content }}
        </div>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['ai:knowledge:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['ai:knowledge:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.id]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
