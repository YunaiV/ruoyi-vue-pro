<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { AiModelApiKeyApi } from '#/api/ai/model/apiKey';
import type { AiModelModelApi } from '#/api/ai/model/model';

import { onMounted, ref } from 'vue';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getApiKeySimpleList } from '#/api/ai/model/apiKey';
import { deleteModel, getModelPage } from '#/api/ai/model/model';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const apiKeyList = ref([] as AiModelApiKeyApi.ApiKey[]);
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
  formModalApi.setData(null).open();
}

/** 编辑 */
function handleEdit(row: AiModelModelApi.Model) {
  formModalApi.setData(row).open();
}

/** 删除 */
async function handleDelete(row: AiModelModelApi.Model) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    duration: 0,
  });
  try {
    await deleteModel(row.id as number);
    message.success({
      content: $t('ui.actionMessage.deleteSuccess', [row.name]),
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
          return await getModelPage({
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
  } as VxeTableGridOptions<AiModelModelApi.Model>,
});
onMounted(async () => {
  // 获得下拉数据
  apiKeyList.value = await getApiKeySimpleList();
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="AI 手册" url="https://doc.iocoder.cn/ai/build/" />
    </template>
    <FormModal @success="handleRefresh" />
    <Grid table-title="模型配置列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['模型配置']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['ai:model:create'],
              onClick: handleCreate,
            },
          ]"
        />
      </template>
      <template #keyId="{ row }">
        <span>
          {{
            apiKeyList.find(
              (item: AiModelApiKeyApi.ApiKey) => item.id === row.keyId,
            )?.name
          }}
        </span>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['ai:model:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['ai:model:delete'],
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
