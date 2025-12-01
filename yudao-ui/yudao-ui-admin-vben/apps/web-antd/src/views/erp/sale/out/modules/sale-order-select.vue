<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ErpSaleOrderApi } from '#/api/erp/sale/order';

import { ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { Input, message, Modal } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getSaleOrderPage } from '#/api/erp/sale/order';

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
  'update:order': [order: ErpSaleOrderApi.SaleOrder];
}>();

const order = ref<ErpSaleOrderApi.SaleOrder>(); // 选择的采购订单
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
          return await getSaleOrderPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            outEnable: true,
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
  } as VxeTableGridOptions<ErpSaleOrderApi.SaleOrder>,
  gridEvents: {
    radioChange: ({ row }: { row: ErpSaleOrderApi.SaleOrder }) => {
      handleSelectOrder(row);
    },
  },
});

/** 选择销售订单 */
function handleSelectOrder(selectOrder: ErpSaleOrderApi.SaleOrder) {
  order.value = selectOrder;
}

/** 确认选择销售订单 */
function handleOk() {
  if (!order.value) {
    message.warning('请选择一个销售订单');
    return;
  }
  emit('update:order', order.value);
  open.value = false;
}
</script>

<template>
  <div>
    <Input
      readonly
      :value="orderNo"
      :disabled="disabled"
      @click="() => !disabled && (open = true)"
    >
      <template #addonAfter>
        <div>
          <IconifyIcon
            class="h-full w-6 cursor-pointer"
            icon="ant-design:setting-outlined"
            :style="{ cursor: disabled ? 'not-allowed' : 'pointer' }"
            @click="() => !disabled && (open = true)"
          />
        </div>
      </template>
    </Input>
    <Modal
      class="!w-[50vw]"
      v-model:open="open"
      title="选择关联订单"
      @ok="handleOk"
    >
      <Grid class="max-h-[600px]" table-title="销售订单列表(仅展示可出库)" />
    </Modal>
  </div>
</template>
