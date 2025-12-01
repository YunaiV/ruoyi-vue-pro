<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { AiKnowledgeDocumentApi } from '#/api/ai/knowledge/document';

import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { confirm, Page } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteKnowledgeDocument,
  getKnowledgeDocumentPage,
  updateKnowledgeDocumentStatus,
} from '#/api/ai/knowledge/document';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';

/** AI 知识库文档列表 */
defineOptions({ name: 'AiKnowledgeDocument' });

const route = useRoute();
const router = useRouter();

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建知识库文档 */
function handleCreate() {
  router.push({
    name: 'AiKnowledgeDocumentCreate',
    query: { knowledgeId: route.query.knowledgeId },
  });
}

/** 编辑知识库文档 */
function handleEdit(id: number) {
  router.push({
    name: 'AiKnowledgeDocumentUpdate',
    query: { id, knowledgeId: route.query.knowledgeId },
  });
}

/** 删除知识库文档 */
async function handleDelete(row: AiKnowledgeDocumentApi.KnowledgeDocument) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.name]),
  });
  try {
    await deleteKnowledgeDocument(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.name]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 跳转到知识库分段页面 */
function handleSegment(id: number) {
  router.push({
    name: 'AiKnowledgeSegment',
    query: { documentId: id },
  });
}

/** 更新文档状态 */
async function handleStatusChange(
  newStatus: number,
  row: AiKnowledgeDocumentApi.KnowledgeDocument,
): Promise<boolean | undefined> {
  return new Promise((resolve, reject) => {
    confirm({
      content: `你要将${row.name}的状态切换为【${getDictLabel(DICT_TYPE.COMMON_STATUS, newStatus)}】吗？`,
    })
      .then(async () => {
        // 更新文档状态
        await updateKnowledgeDocumentStatus({
          id: row.id,
          status: newStatus,
        });
        // 提示并返回成功
        ElMessage.success($t('ui.actionMessage.operationSuccess'));
        resolve(true);
      })
      .catch(() => {
        reject(new Error('取消操作'));
      });
  });
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(handleStatusChange),
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
      isHover: true,
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
    ElMessage.error('知识库 ID 不存在，无法查看文档列表');
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
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['ai:knowledge:update'],
              onClick: handleEdit.bind(null, row.id),
            },
            {
              label: '分段',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.BOOK,
              auth: ['ai:knowledge:query'],
              onClick: handleSegment.bind(null, row.id),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
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
