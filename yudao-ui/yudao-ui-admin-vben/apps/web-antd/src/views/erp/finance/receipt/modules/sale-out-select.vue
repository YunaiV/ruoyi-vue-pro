<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ErpSaleOutApi } from '#/api/erp/sale/out';

import { ref } from 'vue';

import { message, Modal } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getSaleOutPage } from '#/api/erp/sale/out';

import { useSaleOutGridColumns, useSaleOutGridFormSchema } from '../data';

const emit = defineEmits<{
  success: [rows: ErpSaleOutApi.SaleOut[]];
}>();

const customerId = ref<number>(); // 客户ID
const open = ref<boolean>(false); // 弹窗是否打开
const selectedRows = ref<ErpSaleOutApi.SaleOut[]>([]); // 选中的行

/** 表格配置 */
const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useSaleOutGridFormSchema(),
  },
  gridOptions: {
    columns: useSaleOutGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getSaleOutPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            customerId: customerId.value,
            receiptEnable: true, // 只查询可收款的
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    checkboxConfig: {
      highlight: true,
      range: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<ErpSaleOutApi.SaleOut>,
  gridEvents: {
    checkboxChange: ({ records }: { records: ErpSaleOutApi.SaleOut[] }) => {
      selectedRows.value = records;
    },
    checkboxAll: ({ records }: { records: ErpSaleOutApi.SaleOut[] }) => {
      selectedRows.value = records;
    },
  },
});

/** 打开弹窗 */
function openModal(id: number) {
  // 重置数据
  customerId.value = id;
  open.value = true;
  selectedRows.value = [];
  // 查询列表
  gridApi.formApi?.resetForm();
  gridApi.formApi?.setValues({ customerId: id });
  gridApi.query();
}

/** 确认选择销售出库单 */
function handleOk() {
  if (selectedRows.value.length === 0) {
    message.warning('请选择要添加的销售出库单');
    return;
  }
  emit('success', selectedRows.value);
  open.value = false;
}

defineExpose({ open: openModal });
</script>

<template>
  <Modal
    class="!w-[50vw]"
    v-model:open="open"
    title="选择销售出库单"
    @ok="handleOk"
  >
    <Grid
      class="max-h-[600px]"
      table-title="销售出库单列表(仅展示可收款的单据)"
    />
  </Modal>
</template>
