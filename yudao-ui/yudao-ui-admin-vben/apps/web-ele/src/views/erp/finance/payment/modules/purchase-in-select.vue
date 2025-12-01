<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ErpPurchaseInApi } from '#/api/erp/purchase/in';

import { ref } from 'vue';

import { ElButton, ElDialog, ElMessage } from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getPurchaseInPage } from '#/api/erp/purchase/in';

import { usePurchaseInGridColumns, usePurchaseInGridFormSchema } from '../data';

const emit = defineEmits<{
  success: [rows: ErpPurchaseInApi.PurchaseIn[]];
}>();

const supplierId = ref<number>(); // 供应商 ID
const open = ref<boolean>(false); // 弹窗是否打开
const selectedRows = ref<ErpPurchaseInApi.PurchaseIn[]>([]); // 选中的行

/** 表格配置 */
const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: usePurchaseInGridFormSchema(),
  },
  gridOptions: {
    columns: usePurchaseInGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getPurchaseInPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            supplierId: supplierId.value,
            paymentEnable: true, // 只查询可付款的
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
  } as VxeTableGridOptions<ErpPurchaseInApi.PurchaseIn>,
  gridEvents: {
    checkboxChange: ({
      records,
    }: {
      records: ErpPurchaseInApi.PurchaseIn[];
    }) => {
      selectedRows.value = records;
    },
    checkboxAll: ({ records }: { records: ErpPurchaseInApi.PurchaseIn[] }) => {
      selectedRows.value = records;
    },
  },
});

/** 打开弹窗 */
function openModal(id: number) {
  // 重置数据
  supplierId.value = id;
  open.value = true;
  selectedRows.value = [];
  // 查询列表
  gridApi.formApi?.resetForm();
  gridApi.formApi?.setValues({ supplierId: id });
  gridApi.query();
}

/** 确认选择采购入库单 */
function handleOk() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要添加的采购入库单');
    return;
  }
  emit('success', selectedRows.value);
  open.value = false;
}

defineExpose({ open: openModal });
</script>

<template>
  <ElDialog
    class="!w-[50vw]"
    v-model="open"
    title="选择采购入库单"
    @confirm="handleOk"
    :append-to-body="true"
  >
    <Grid
      class="max-h-[600px]"
      table-title="采购入库单列表(仅展示可付款的单据)"
    />
    <template #footer>
      <ElButton @click="open = false">取消</ElButton>
      <ElButton type="primary" @click="handleOk">确定</ElButton>
    </template>
  </ElDialog>
</template>
