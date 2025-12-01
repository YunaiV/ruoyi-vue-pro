<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { BpmProcessInstanceApi } from '#/api/bpm/processInstance';

import { h, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page, prompt } from '@vben/common-ui';

import { ElInput, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getProcessDefinition } from '#/api/bpm/definition';
import {
  cancelProcessInstanceByAdmin,
  getProcessInstanceManagerPage,
} from '#/api/bpm/processInstance';
import { parseFormFields } from '#/components/form-create';

import { useGridColumns, useGridFormSchema } from './data';

defineOptions({ name: 'BpmProcessInstanceReport' });

const router = useRouter();
const { query } = useRoute();
const processDefinitionId = query.processDefinitionId as string;

const formFields = ref<any[]>([]);

/** 获取流程定义 */
async function getProcessDefinitionData() {
  const processDefinition = await getProcessDefinition(processDefinitionId);
  if (processDefinition?.formFields) {
    formFields.value = parseFormCreateFields(processDefinition.formFields);
  }
}

/** 解析表单字段 */
function parseFormCreateFields(formFields?: string[]) {
  const result: Array<Record<string, any>> = [];
  if (formFields) {
    formFields.forEach((fieldStr: string) => {
      try {
        parseFormFields(JSON.parse(fieldStr), result);
      } catch (error) {
        console.error('解析表单字段失败', error);
      }
    });
  }
  return result;
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 查看详情 */
function handleDetail(row: BpmProcessInstanceApi.ProcessInstance) {
  router.push({
    name: 'BpmProcessInstanceDetail',
    query: { id: row.id },
  });
}

/** 取消流程实例 */
function handleCancel(row: BpmProcessInstanceApi.ProcessInstance) {
  prompt({
    component: () => {
      return h(ElInput, {
        placeholder: '请输入取消原因',
        clearable: true,
        rows: 2,
        type: 'textarea',
      });
    },
    content: '请输入取消原因',
    title: '取消流程',
    modelPropName: 'modelValue',
  }).then(async (reason) => {
    if (reason) {
      await cancelProcessInstanceByAdmin(row.id, reason);
      ElMessage.success('取消成功');
      handleRefresh();
    }
  });
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          const { formFieldsParams = {}, ...restValues } = formValues || {};
          const params = {
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...restValues,
            processDefinitionKey: query.processDefinitionKey,
            formFieldsParams: JSON.stringify(formFieldsParams),
          };
          return await getProcessInstanceManagerPage(params);
        },
      },
    },
  } as VxeTableGridOptions<BpmProcessInstanceApi.ProcessInstance>,
});

/** 初始化 */
onMounted(async () => {
  // 获取流程定义
  await getProcessDefinitionData();
  // 更新表单配置、表格列配置
  gridApi.formApi.setState({
    schema: useGridFormSchema(formFields.value),
  });
  await gridApi.grid.reloadColumn(useGridColumns(formFields.value) as any[]);
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="流程实例列表">
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '详情',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.VIEW,
              auth: ['bpm:process-instance:query'],
              onClick: handleDetail.bind(null, row),
            },
            {
              label: '取消',
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['bpm:process-instance:cancel'],
              ifShow: row.status === 1,
              onClick: handleCancel.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
