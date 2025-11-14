<!-- IoT 产品选择器，使用弹窗展示 -->
<script setup lang="ts">
import type { IotProductApi } from '#/api/iot/product/product';

import { reactive, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import { Button, Form, Input, message } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getProductPage } from '#/api/iot/product/product';

defineOptions({ name: 'IoTProductTableSelect' });

const props = withDefaults(defineProps<Props>(), {
  multiple: false,
});

const emit = defineEmits<{
  success: [product: IotProductApi.Product | IotProductApi.Product[]];
}>();

interface Props {
  multiple?: boolean;
}

const [Modal, modalApi] = useVbenModal({
  title: '产品选择器',
  onConfirm: handleConfirm,
});

const selectedProducts = ref<IotProductApi.Product[]>([]);
const selectedRowKeys = ref<number[]>([]);

// 搜索参数
const queryParams = reactive({
  name: '',
  productKey: '',
});

// 配置表格
const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: [
      {
        type: props.multiple ? 'checkbox' : 'radio',
        width: 50,
      },
      {
        field: 'name',
        title: '产品名称',
        minWidth: 150,
      },
      {
        field: 'productKey',
        title: 'ProductKey',
        minWidth: 150,
      },
      {
        field: 'categoryName',
        title: '品类',
        minWidth: 120,
      },
      {
        field: 'deviceType',
        title: '设备类型',
        minWidth: 100,
        cellRender: {
          name: 'CellDict',
          props: { type: 'iot_product_device_type' },
        },
      },
      {
        field: 'createTime',
        title: '创建时间',
        minWidth: 180,
        formatter: 'formatDateTime',
      },
    ],
    checkboxConfig: {
      reserve: true,
      highlight: true,
    },
    radioConfig: {
      reserve: true,
      highlight: true,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }: any) => {
          return await getProductPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...queryParams,
          });
        },
      },
    },
  },
});

// 打开选择器
async function open() {
  selectedProducts.value = [];
  selectedRowKeys.value = [];
  modalApi.open();
  gridApi.reload();
}

// 搜索
function handleSearch() {
  gridApi.reload();
}

// 重置搜索
function handleReset() {
  queryParams.name = '';
  queryParams.productKey = '';
  gridApi.reload();
}

// 确认选择
async function handleConfirm() {
  const grid = gridApi.grid;
  if (!grid) {
    return false;
  }

  if (props.multiple) {
    const checkboxRecords = grid.getCheckboxRecords();
    if (checkboxRecords.length === 0) {
      message.warning('请至少选择一个产品');
      return false;
    }
    emit('success', checkboxRecords);
  } else {
    const radioRecord = grid.getRadioRecord();
    if (!radioRecord) {
      message.warning('请选择一个产品');
      return false;
    }
    emit('success', radioRecord);
  }
  return true;
}

defineExpose({ open });
</script>

<template>
  <Modal class="!w-[900px]">
    <div class="mb-4">
      <Form layout="inline" :model="queryParams">
        <Form.Item label="产品名称">
          <Input
            v-model:value="queryParams.name"
            placeholder="请输入产品名称"
            allow-clear
            class="!w-[200px]"
            @press-enter="handleSearch"
          />
        </Form.Item>
        <Form.Item label="ProductKey">
          <Input
            v-model:value="queryParams.productKey"
            placeholder="请输入产品标识"
            allow-clear
            class="!w-[200px]"
            @press-enter="handleSearch"
          />
        </Form.Item>
        <Form.Item>
          <Button type="primary" @click="handleSearch">
            <template #icon>
              <IconifyIcon icon="ant-design:search-outlined" />
            </template>
            搜索
          </Button>
          <Button class="ml-2" @click="handleReset">
            <template #icon>
              <IconifyIcon icon="ant-design:reload-outlined" />
            </template>
            重置
          </Button>
        </Form.Item>
      </Form>
    </div>

    <Grid />
  </Modal>
</template>
