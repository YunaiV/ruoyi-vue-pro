<script setup lang="ts">
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ThingModelApi } from '#/api/iot/thingmodel';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import { Button, message } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteThingModel, getThingModelPage } from '#/api/iot/thingmodel';

import { getDataTypeOptionsLabel } from '../utils/constants';
import { useGridColumns, useGridFormSchema } from './data';

defineOptions({ name: 'IoTThingModel' });

const props = defineProps<{
  productId: number;
}>();

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page, form }) => {
          return await getThingModelPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            productId: props.productId,
            ...form,
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
  } as VxeTableGridOptions<ThingModelApi.ThingModel>,
  formOptions: {
    schema: useGridFormSchema(),
  },
});

// 新增功能
const handleCreate = () => {
  // TODO: 打开物模型表单
  console.error('新增功能');
};

// 编辑功能
const handleEdit = (row: any) => {
  // TODO: 打开物模型表单
  console.error('编辑功能:', row);
};

// 删除功能
const handleDelete = async (row: any) => {
  try {
    await deleteThingModel(row.id);
    message.success('删除成功');
    gridApi.reload();
  } catch (error) {
    console.error('删除失败:', error);
  }
};

// 打开 TSL
const handleOpenTSL = () => {
  // TODO: 打开 TSL 弹窗
  console.error('打开 TSL');
};

// 获取数据类型标签
const getDataTypeLabel = (row: any) => {
  return getDataTypeOptionsLabel(row.property?.dataType) || '-';
};
</script>

<template>
  <Page
    description="管理产品的物模型定义，包括属性、服务和事件"
    title="物模型管理"
  >
    <Grid>
      <template #toolbar-tools>
        <Button @click="handleCreate">
          <IconifyIcon icon="ant-design:plus-outlined" class="mr-1" />
          添加功能
        </Button>
        <Button type="primary" @click="handleOpenTSL"> TSL </Button>
      </template>

      <!-- 数据类型列 -->
      <template #dataType="{ row }">
        <span>{{ getDataTypeLabel(row) }}</span>
      </template>

      <!-- 数据定义列 -->
      <template #dataDefinition="{ row }">
        <!-- TODO: 实现数据定义组件 -->
        <span class="text-gray-400">{{ row }}</span>
      </template>

      <!-- 操作列 -->
      <template #actions="{ row }">
        <Button size="small" type="primary" @click="handleEdit(row)">
          编辑
        </Button>
        <Button size="small" danger @click="handleDelete(row)"> 删除 </Button>
      </template>
    </Grid>
  </Page>
</template>
