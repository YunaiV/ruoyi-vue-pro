<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ErpSaleReturnApi } from '#/api/erp/sale/return';

import { ref } from 'vue';

import { ElButton, ElDialog, ElMessage } from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getSaleReturnPage } from '#/api/erp/sale/return';

import { useSaleReturnGridColumns, useSaleReturnGridFormSchema } from '../data';

const emit = defineEmits<{
  success: [rows: ErpSaleReturnApi.SaleReturn[]];
}>();

const customerId = ref<number>(); // 客户ID
const open = ref<boolean>(false); // 弹窗是否打开
const selectedRows = ref<ErpSaleReturnApi.SaleReturn[]>([]); // 选中的行

/** 表格配置 */
const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useSaleReturnGridFormSchema(),
  },
  gridOptions: {
    columns: useSaleReturnGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getSaleReturnPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            customerId: customerId.value,
            refundEnable: true, // 只查询可退款的
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
  } as VxeTableGridOptions<ErpSaleReturnApi.SaleReturn>,
  gridEvents: {
    checkboxChange: ({
      records,
    }: {
      records: ErpSaleReturnApi.SaleReturn[];
    }) => {
      selectedRows.value = records;
    },
    checkboxAll: ({ records }: { records: ErpSaleReturnApi.SaleReturn[] }) => {
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

/** 确认选择销售退货单 */
function handleOk() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要添加的销售退货单');
    return;
  }
  emit('success', selectedRows.value);
  open.value = false;
}

defineExpose({ open: openModal });
</script>

<template>
  <ElDialog
    v-model="open"
    class="!w-[50vw]"
    title="选择销售退货单"
    @confirm="handleOk"
  >
    <Grid
      class="max-h-[600px]"
      table-title="销售退货单列表(仅展示可退款的单据)"
    />
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="open = false">取消</ElButton>
        <ElButton type="primary" @click="handleOk">确定</ElButton>
      </div>
    </template>
  </ElDialog>
</template>
