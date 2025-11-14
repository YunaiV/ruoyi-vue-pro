<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { BpmModelFormType } from '@vben/constants';

import { Button, Tooltip } from 'ant-design-vue';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getProcessDefinitionPage } from '#/api/bpm/definition';

// 导入 FormCreate 表单详情
import FormCreateDetail from '../../form/modules/detail.vue';
import { useGridColumns } from './data';

defineOptions({ name: 'BpmProcessDefinition' });

const [FormCreateDetailModal, formCreateDetailModalApi] = useVbenModal({
  connectedComponent: FormCreateDetail,
  destroyOnClose: true,
});

/** 查看表单详情 */
function handleFormDetail(row: any) {
  if (row.formType === BpmModelFormType.NORMAL) {
    const data = {
      id: row.formId,
    };
    formCreateDetailModalApi.setData(data).open();
  } else {
    // TODO 待实现
    console.warn('业务表单待实现', row);
  }
}

const router = useRouter();
/** 恢复流程模型 */
async function openModelForm(id?: number) {
  await router.push({
    name: 'BpmModelUpdate',
    params: { id, type: 'definition' },
  });
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

const route = useRoute();

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: true,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }) => {
          const params = {
            pageNo: page?.currentPage,
            pageSize: page?.pageSize,
            key: route.query.key,
          };
          return await getProcessDefinitionPage(params);
        },
      },
    },
    rowConfig: {
      keyField: 'id',
    },
    toolbarConfig: {
      refresh: true,
    },
  } as VxeTableGridOptions,
});

/** 初始化 */
onMounted(() => {
  handleRefresh();
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="工作流手册" url="https://doc.iocoder.cn/bpm/" />
    </template>
    <Grid table-title="流程定义列表">
      <template #startUsers="{ row }">
        <template v-if="!row.startUsers?.length">全部可见</template>
        <template v-else-if="row.startUsers.length === 1">
          {{ row.startUsers[0].nickname }}
        </template>
        <template v-else>
          <Tooltip
            placement="top"
            :title="row.startUsers.map((user: any) => user.nickname).join(',')"
          >
            {{ row.startUsers[0].nickname }}等
            {{ row.startUsers.length }} 人可见
          </Tooltip>
        </template>
      </template>
      <template #formInfo="{ row }">
        <Button
          v-if="row.formType === BpmModelFormType.NORMAL"
          type="link"
          @click="handleFormDetail(row)"
        >
          <span>{{ row.formName }}</span>
        </Button>
        <Button
          v-else-if="row.formType === BpmModelFormType.CUSTOM"
          type="link"
          @click="handleFormDetail(row)"
        >
          <span>{{ row.formCustomCreatePath }}</span>
        </Button>
        <span v-else>暂无表单</span>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '恢复',
              type: 'link',
              auth: ['bpm:model:update'],
              onClick: openModelForm.bind(null, row.id),
            },
          ]"
        />
      </template>
    </Grid>
    <FormCreateDetailModal />
  </Page>
</template>
