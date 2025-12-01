<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ErpPurchaseOrderApi } from '#/api/erp/purchase/order';

import { ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { ElButton, ElDialog, ElInput, ElMessage } from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getPurchaseOrderPage } from '#/api/erp/purchase/order';

import { useOrderGridColumns, useOrderGridFormSchema } from '../data';

defineProps({
  orderNo: {
    type: String,
    default: () => undefined,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits<{
  'update:order': [order: ErpPurchaseOrderApi.PurchaseOrder];
}>();

const order = ref<ErpPurchaseOrderApi.PurchaseOrder>(); // 选择的采购订单
const open = ref<boolean>(false); // 选择采购订单弹窗是否打开

/** 表格配置 */
const [Grid] = useVbenVxeGrid({
  formOptions: {
    schema: useOrderGridFormSchema(),
  },
  gridOptions: {
    columns: useOrderGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getPurchaseOrderPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            inEnable: true,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    radioConfig: {
      trigger: 'row',
      highlight: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<ErpPurchaseOrderApi.PurchaseOrder>,
  gridEvents: {
    radioChange: ({ row }: { row: ErpPurchaseOrderApi.PurchaseOrder }) => {
      handleSelectOrder(row);
    },
  },
});

/** 选择采购订单 */
function handleSelectOrder(selectOrder: ErpPurchaseOrderApi.PurchaseOrder) {
  order.value = selectOrder;
}

/** 确认选择采购订单 */
function handleOk() {
  if (!order.value) {
    ElMessage.warning('请选择一个采购订单');
    return;
  }
  emit('update:order', order.value);
  open.value = false;
}
</script>

<template>
  <div>
    <ElInput
      readonly
      :model-value="orderNo"
      :disabled="disabled"
      @click="() => !disabled && (open = true)"
    >
      <template #append>
        <div>
          <IconifyIcon
            class="h-full w-6 cursor-pointer"
            icon="ant-design:setting-outlined"
            :style="{ cursor: disabled ? 'not-allowed' : 'pointer' }"
            @click="() => !disabled && (open = true)"
          />
        </div>
      </template>
    </ElInput>
    <ElDialog
      v-model="open"
      title="选择关联订单"
      width="50%"
      @close="open = false"
      :append-to-body="true"
    >
      <Grid class="max-h-[600px]" table-title="采购订单列表(仅展示可退货)" />
      <template #footer>
        <ElButton @click="open = false">取消</ElButton>
        <ElButton type="primary" @click="handleOk">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>
