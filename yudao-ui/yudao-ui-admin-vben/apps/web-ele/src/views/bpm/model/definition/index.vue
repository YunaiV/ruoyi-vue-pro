<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { BpmProcessDefinitionApi } from '#/api/bpm/definition';

import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { BpmModelFormType } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';

import { ElButton, ElTooltip } from 'element-plus';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getProcessDefinitionPage } from '#/api/bpm/definition';

import FormCreateDetail from '../../form/modules/detail.vue';
import { useGridColumns } from './data';

defineOptions({ name: 'BpmProcessDefinition' });

const route = useRoute();
const router = useRouter();

const [FormCreateDetailModal, formCreateDetailModalApi] = useVbenModal({
  connectedComponent: FormCreateDetail,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 查看表单详情 */
async function handleFormDetail(
  row: BpmProcessDefinitionApi.ProcessDefinition,
) {
  if (row.formType === BpmModelFormType.NORMAL) {
    const data = {
      id: row.formId,
    };
    formCreateDetailModalApi.setData(data).open();
  } else {
    await router.push({
      path: row.formCustomCreatePath,
    });
  }
}

/** 恢复流程模型 */
async function handleRecover(row: BpmProcessDefinitionApi.ProcessDefinition) {
  await router.push({
    name: 'BpmModelUpdate',
    params: { id: row.id, type: 'definition' },
  });
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }) => {
          return await getProcessDefinitionPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            key: route.query.key as string,
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
    },
  } as VxeTableGridOptions<BpmProcessDefinitionApi.ProcessDefinition>,
});

/** 初始化 */
onMounted(() => {
  handleRefresh();
});
</script>

<template>
  <Page auto-content-height>
    <FormCreateDetailModal />
    <template #doc>
      <DocAlert title="工作流手册" url="https://doc.iocoder.cn/bpm/" />
    </template>
    <Grid table-title="流程定义列表">
      <template #startUsers="{ row }">
        <template v-if="!row.startUsers || row.startUsers.length === 0">
          全部可见
        </template>
        <template v-else-if="row.startUsers.length === 1">
          {{ row.startUsers[0]!.nickname }}
        </template>
        <template v-else>
          <ElTooltip
            placement="top"
            :content="
              row.startUsers.map((user: any) => user.nickname).join(',')
            "
          >
            {{ row.startUsers[0]!.nickname }}等
            {{ row.startUsers.length }} 人可见
          </ElTooltip>
        </template>
      </template>
      <template #formInfo="{ row }">
        <ElButton
          v-if="row.formType === BpmModelFormType.NORMAL"
          link
          @click="handleFormDetail(row)"
        >
          <IconifyIcon icon="lucide:file-text" class="mr-1" />
          <span>{{ row.formName }}</span>
        </ElButton>
        <ElButton
          v-else-if="row.formType === BpmModelFormType.CUSTOM"
          link
          @click="handleFormDetail(row)"
        >
          <IconifyIcon icon="lucide:file-code" class="mr-1" />
          <span>{{ row.formCustomCreatePath }}</span>
        </ElButton>
        <span v-else>暂无表单</span>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '恢复',
              icon: 'lucide:undo-2',
              link: true,
              auth: ['bpm:model:update'],
              onClick: handleRecover.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
