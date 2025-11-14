<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { AiKnowledgeDocumentApi } from '#/api/ai/knowledge/document';

import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAccess } from '@vben/access';
import { confirm, Page } from '@vben/common-ui';
import { CommonStatusEnum } from '@vben/constants';

import { message, Switch } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteKnowledgeDocument,
  getKnowledgeDocumentPage,
  updateKnowledgeDocumentStatus,
} from '#/api/ai/knowledge/document';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';

/** AI 知识库文档 列表 */
defineOptions({ name: 'AiKnowledgeDocument' });
const { hasAccessByCodes } = useAccess();

const route = useRoute(); // 路由
const router = useRouter(); // 路由
/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建 */
function handleCreate() {
  router.push({
    name: 'AiKnowledgeDocumentCreate',
    query: { knowledgeId: route.query.knowledgeId },
  });
}

/** 编辑 */
function handleEdit(id: number) {
  router.push({
    name: 'AiKnowledgeDocumentUpdate',
    query: { id, knowledgeId: route.query.knowledgeId },
  });
}

/** 删除 */
async function handleDelete(row: AiKnowledgeDocumentApi.KnowledgeDocument) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
  });
  try {
    await deleteKnowledgeDocument(row.id as number);
    message.success({
      content: $t('ui.actionMessage.deleteSuccess', [row.name]),
    });
    handleRefresh();
  } finally {
    hideLoading();
  }
}
/** 跳转到知识库分段页面 */
const handleSegment = (id: number) => {
  router.push({
    name: 'AiKnowledgeSegment',
    query: { documentId: id },
  });
};
/** 修改是否发布 */
const handleStatusChange = async (
  row: AiKnowledgeDocumentApi.KnowledgeDocument,
) => {
  try {
    // 修改状态的二次确认
    const text = row.status ? '启用' : '禁用';
    await confirm(`确认要"${text}"${row.name}文档吗?`).then(async () => {
      await updateKnowledgeDocumentStatus({
        id: row.id,
        status: row.status,
      });
      handleRefresh();
    });
  } catch {
    row.status =
      row.status === CommonStatusEnum.ENABLE
        ? CommonStatusEnum.DISABLE
        : CommonStatusEnum.ENABLE;
  }
};
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
          return await getKnowledgeDocumentPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
            knowledgeId: route.query.knowledgeId,
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
  } as VxeTableGridOptions<AiKnowledgeDocumentApi.KnowledgeDocument>,
});
/** 初始化 */
onMounted(() => {
  // 如果知识库 ID 不存在，显示错误提示并关闭页面
  if (!route.query.knowledgeId) {
    message.error('知识库 ID 不存在，无法查看文档列表');
    // 关闭当前路由，返回到知识库列表页面
    router.back();
  }
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="知识库文档列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['知识库文档']),
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
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['ai:knowledge:update'],
              onClick: handleEdit.bind(null, row.id),
            },
          ]"
          :drop-down-actions="[
            {
              label: '分段',
              type: 'link',
              auth: ['ai:knowledge:query'],
              onClick: handleSegment.bind(null, row.id),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              auth: ['ai:knowledge:delete'],
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
