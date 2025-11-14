<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { BpmProcessInstanceApi } from '#/api/bpm/processInstance';

import { nextTick, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page, prompt } from '@vben/common-ui';

import { Input, message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getProcessDefinition } from '#/api/bpm/definition';
import {
  cancelProcessInstanceByAdmin,
  getProcessInstanceManagerPage,
} from '#/api/bpm/processInstance';
import { getSimpleUserList } from '#/api/system/user';
import { parseFormFields } from '#/components/simple-process-design';

import { useGridColumns, useGridFormSchema } from './data';

defineOptions({ name: 'BpmProcessInstanceReport' });

const router = useRouter(); // 路由
const { query } = useRoute();
const processDefinitionId = query.processDefinitionId as string;

const formFields = ref<any[]>([]);
const userList = ref<any[]>([]); // 用户列表
const gridReady = ref(false); // 表格是否准备好

// 表格的列需要解析表单字段，这里定义成变量，解析表单字段后再渲染
let Grid: any = null;
let gridApi: any = null;

/** 获取流程定义 */
const getProcessDefinitionData = async () => {
  try {
    const processDefinition = await getProcessDefinition(processDefinitionId);
    if (processDefinition && processDefinition.formFields) {
      formFields.value = parseFormCreateFields(processDefinition.formFields);
    }
  } catch (error) {
    console.error('获取流程定义失败', error);
  }
};

/** 解析表单字段 */
const parseFormCreateFields = (formFields?: string[]) => {
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
};

/** 刷新表格 */
function handleRefresh() {
  if (gridApi) {
    gridApi.query();
  }
}

/** 查看详情 */
const handleDetail = (row: BpmProcessInstanceApi.ProcessInstance) => {
  router.push({
    name: 'BpmProcessInstanceDetail',
    query: {
      id: row.id,
    },
  });
};

/** 取消按钮操作 */
const handleCancel = async (row: BpmProcessInstanceApi.ProcessInstance) => {
  prompt({
    content: '请输入取消原因：',
    title: '取消流程',
    icon: 'question',
    component: Input,
    modelPropName: 'value',
    async beforeClose(scope) {
      if (!scope.isConfirm) return;
      if (!scope.value) {
        message.warning('请输入取消原因');
        return false;
      }
      await cancelProcessInstanceByAdmin(row.id, scope.value);
      return true;
    },
  }).then(() => {
    message.success('取消成功');
    handleRefresh();
  });
};

/** 创建表格 */
const createGrid = () => {
  const [GridCompnent, api] = useVbenVxeGrid({
    formOptions: {
      schema: useGridFormSchema(userList.value, formFields.value),
    },
    gridOptions: {
      columns: useGridColumns(formFields.value),
      height: 'auto',
      keepSource: true,
      rowConfig: {
        keyField: 'id',
      },
      toolbarConfig: {
        refresh: true,
        search: true,
      },
      proxyConfig: {
        ajax: {
          query: async ({ page }, formValues) => {
            // 处理表单值，将 formFieldsParams 对象提取出来
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
    } as VxeTableGridOptions,
  });

  Grid = GridCompnent;
  gridApi = api;
  gridReady.value = true;
};

/** 初始化 */
onMounted(async () => {
  // 获取用户列表
  userList.value = await getSimpleUserList();

  // 获取流程定义，并获取表单字段。
  await getProcessDefinitionData();

  // 解析表单字段后，再创建表格，表格的列依赖于表单字段
  createGrid();

  // 确保 DOM 更新完成
  await nextTick();

  // 加载表格数据
  gridApi.query();
});
</script>

<template>
  <Page auto-content-height>
    <!-- 动态渲染表格 -->
    <component :is="Grid" v-if="gridReady" table-title="流程实例列表">
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '详情',
              type: 'link',
              icon: ACTION_ICON.VIEW,
              auth: ['bpm:process-instance:query'],
              onClick: handleDetail.bind(null, row),
            },
            {
              label: '取消',
              type: 'link',
              icon: ACTION_ICON.DELETE,
              auth: ['bpm:process-instance:cancel'],
              ifShow: row.status === 1,
              onClick: handleCancel.bind(null, row),
            },
          ]"
        />
      </template>
    </component>
  </Page>
</template>
