<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { InfraCodegenApi } from '#/api/infra/codegen';

import { reactive } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { createCodegenList, getSchemaTableList } from '#/api/infra/codegen';
import { $t } from '#/locales';
import {
  useImportTableColumns,
  useImportTableFormSchema,
} from '#/views/infra/codegen/data';

/** 定义组件事件 */
const emit = defineEmits<{
  (e: 'success'): void;
}>();

const formData = reactive<InfraCodegenApi.CodegenCreateListReqVO>({
  dataSourceConfigId: 0,
  tableNames: [], // 已选择的表列表
});

/** 表格实例 */
const [Grid] = useVbenVxeGrid({
  formOptions: {
    schema: useImportTableFormSchema(),
    submitOnChange: true,
  },
  gridOptions: {
    columns: useImportTableColumns(),
    height: 600,
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          if (formValues.dataSourceConfigId === undefined) {
            return [];
          }
          formData.dataSourceConfigId = formValues.dataSourceConfigId;
          return await getSchemaTableList({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'name',
      isHover: true,
    },
    toolbarConfig: {
      enabled: false,
    },
    checkboxConfig: {
      highlight: true,
      range: true,
    },
    pagerConfig: {
      enabled: false,
    },
  } as VxeTableGridOptions<InfraCodegenApi.DatabaseTable>,
  gridEvents: {
    checkboxChange: ({
      records,
    }: {
      records: InfraCodegenApi.DatabaseTable[];
    }) => {
      formData.tableNames = records.map((item) => item.name);
    },
  },
});

/** 模态框实例 */
const [Modal, modalApi] = useVbenModal({
  title: '导入表',
  class: 'w-1/2',
  async onConfirm() {
    modalApi.lock();
    // 1.1 获取表单值
    if (formData?.dataSourceConfigId === undefined) {
      message.error('请选择数据源');
      return;
    }
    // 1.2 校验是否选择了表
    if (formData.tableNames.length === 0) {
      message.error('请选择需要导入的表');
      return;
    }

    // 2. 提交请求
    const hideLoading = message.loading({
      content: '导入中...',
      duration: 0,
    });
    try {
      await createCodegenList(formData);
      // 关闭并提示
      await modalApi.close();
      emit('success');
      message.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      hideLoading();
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal>
    <Grid />
  </Modal>
</template>
