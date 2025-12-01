<script lang="ts" setup>
import type { ErpPurchaseReturnApi } from '#/api/erp/purchase/return';

import { computed, nextTick, onMounted, ref, watch } from 'vue';

import {
  erpCountInputFormatter,
  erpPriceInputFormatter,
  erpPriceMultiply,
} from '@vben/utils';

import { Input, InputNumber, Select } from 'ant-design-vue';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getProductSimpleList } from '#/api/erp/product/product';
import { getWarehouseStockCount } from '#/api/erp/stock/stock';
import { getWarehouseSimpleList } from '#/api/erp/stock/warehouse';

import { useFormItemColumns } from '../data';

interface Props {
  items?: ErpPurchaseReturnApi.PurchaseReturnItem[];
  disabled?: boolean;
  discountPercent?: number;
  otherPrice?: number;
}

const props = withDefaults(defineProps<Props>(), {
  items: () => [],
  disabled: false,
  discountPercent: 0,
  otherPrice: 0,
});

const emit = defineEmits([
  'update:items',
  'update:discount-price',
  'update:other-price',
  'update:total-price',
]);

const tableData = ref<ErpPurchaseReturnApi.PurchaseReturnItem[]>([]); // 表格数据
const productOptions = ref<any[]>([]); // 产品下拉选项
const warehouseOptions = ref<any[]>([]); // 仓库下拉选项

/** 获取表格合计数据 */
const summaries = computed(() => {
  return {
    count: tableData.value.reduce((sum, item) => sum + (item.count || 0), 0),
    totalProductPrice: tableData.value.reduce(
      (sum, item) => sum + (item.totalProductPrice || 0),
      0,
    ),
    taxPrice: tableData.value.reduce(
      (sum, item) => sum + (item.taxPrice || 0),
      0,
    ),
    totalPrice: tableData.value.reduce(
      (sum, item) => sum + (item.totalPrice || 0),
      0,
    ),
  };
});

/** 表格配置 */
const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useFormItemColumns(tableData.value, props.disabled),
    data: tableData.value,
    minHeight: 250,
    autoResize: true,
    border: true,
    rowConfig: {
      keyField: 'seq',
      isHover: true,
    },
    pagerConfig: {
      enabled: false,
    },
    toolbarConfig: {
      enabled: false,
    },
  },
});

/** 监听外部传入的列数据 */
watch(
  () => props.items,
  async (items) => {
    if (!items) {
      return;
    }
    items.forEach((item) => initRow(item));
    tableData.value = [...items];
    await nextTick(); // 特殊：保证 gridApi 已经初始化
    await gridApi.grid.reloadData(tableData.value);
    // 更新表格列配置（目的：已入库、已退货动态列）
    const columns = useFormItemColumns(tableData.value, props.disabled);
    await gridApi.grid.reloadColumn(columns || []);
  },
  {
    immediate: true,
  },
);

/** 计算 discountPrice、otherPrice、totalPrice 价格 */
watch(
  () => [tableData.value, props.discountPercent, props.otherPrice],
  () => {
    if (!tableData.value || tableData.value.length === 0) {
      return;
    }
    const totalPrice = tableData.value.reduce(
      (prev, curr) => prev + (curr.totalPrice || 0),
      0,
    );
    const discountPrice =
      props.discountPercent === null
        ? 0
        : erpPriceMultiply(totalPrice, props.discountPercent / 100);
    const discountedPrice = totalPrice - discountPrice!;
    const finalTotalPrice = discountedPrice + (props.otherPrice || 0);

    // 通知父组件更新
    emit('update:discount-price', discountPrice);
    emit('update:other-price', props.otherPrice || 0);
    emit('update:total-price', finalTotalPrice);
  },
  { deep: true },
);

/** 处理删除 */
function handleDelete(row: ErpPurchaseReturnApi.PurchaseReturnItem) {
  // TODO 芋艿
  const index = tableData.value.findIndex((item) => item.seq === row.seq);
  if (index !== -1) {
    tableData.value.splice(index, 1);
  }
  // 通知父组件更新
  emit('update:items', [...tableData.value]);
}

/** 处理仓库变更 */
async function handleWarehouseChange(
  row: ErpPurchaseReturnApi.PurchaseReturnItem,
) {
  const stockCount = await getWarehouseStockCount({
    productId: row.productId!,
    warehouseId: row.warehouseId!,
  });
  row.stockCount = stockCount || 0;
  handleRowChange(row);
}

/** 处理行数据变更 */
function handleRowChange(row: any) {
  // TODO 芋艿
  const index = tableData.value.findIndex((item) => item.seq === row.seq);
  if (index === -1) {
    tableData.value.push(row);
  } else {
    tableData.value[index] = row;
  }
  emit('update:items', [...tableData.value]);
}

/** 初始化行数据 */
function initRow(row: ErpPurchaseReturnApi.PurchaseReturnItem) {
  if (row.productPrice && row.count) {
    row.totalProductPrice = erpPriceMultiply(row.productPrice, row.count) ?? 0;
    row.taxPrice =
      erpPriceMultiply(row.totalProductPrice, (row.taxPercent || 0) / 100) ?? 0;
    row.totalPrice = row.totalProductPrice + row.taxPrice;
  }
}

/** 表单校验 */
function validate() {
  for (let i = 0; i < tableData.value.length; i++) {
    const item = tableData.value[i];
    if (item) {
      if (!item.warehouseId) {
        throw new Error(`第 ${i + 1} 行：仓库不能为空`);
      }
      if (!item.count || item.count <= 0) {
        throw new Error(`第 ${i + 1} 行：产品数量不能为空`);
      }
    }
  }
}

defineExpose({
  validate,
});

/** 初始化 */
onMounted(async () => {
  productOptions.value = await getProductSimpleList();
  warehouseOptions.value = await getWarehouseSimpleList();
});
</script>

<template>
  <Grid class="w-full">
    <template #warehouseId="{ row }">
      <Select
        v-model:value="row.warehouseId"
        :options="warehouseOptions"
        :field-names="{ label: 'name', value: 'id' }"
        placeholder="请选择仓库"
        :disabled="disabled"
        show-search
        class="w-full"
        @change="handleWarehouseChange(row)"
      />
    </template>
    <template #productId="{ row }">
      <Select
        disabled
        v-model:value="row.productId"
        :options="productOptions"
        :field-names="{ label: 'name', value: 'id' }"
        class="w-full"
        placeholder="请选择产品"
        show-search
      />
    </template>
    <template #count="{ row }">
      <InputNumber
        v-if="!disabled"
        v-model:value="row.count"
        :min="0"
        :precision="2"
        @change="handleRowChange(row)"
      />
      <span v-else>{{ erpCountInputFormatter(row.count) || '-' }}</span>
    </template>
    <template #productPrice="{ row }">
      <InputNumber
        v-if="!disabled"
        v-model:value="row.productPrice"
        :min="0"
        :precision="2"
        @change="handleRowChange(row)"
      />
      <span v-else>{{ erpPriceInputFormatter(row.productPrice) || '-' }}</span>
    </template>
    <template #remark="{ row }">
      <Input v-if="!disabled" v-model:value="row.remark" class="w-full" />
      <span v-else>{{ row.remark || '-' }}</span>
    </template>
    <template #taxPercent="{ row }">
      <InputNumber
        v-if="!disabled"
        v-model:value="row.taxPercent"
        :min="0"
        :max="100"
        :precision="2"
        @change="handleRowChange(row)"
      />
      <span v-else>{{ row.taxPercent || '-' }}</span>
    </template>
    <template #actions="{ row }">
      <TableAction
        :actions="[
          {
            label: '删除',
            type: 'link',
            danger: true,
            popConfirm: {
              title: '确认删除该产品吗？',
              confirm: handleDelete.bind(null, row),
            },
          },
        ]"
      />
    </template>

    <template #bottom>
      <div class="mt-2 rounded border border-border bg-muted p-2">
        <div class="flex justify-between text-sm text-muted-foreground">
          <span class="font-medium text-foreground">合计：</span>
          <div class="flex space-x-4">
            <span>数量：{{ erpCountInputFormatter(summaries.count) }}</span>
            <span>
              金额：{{ erpPriceInputFormatter(summaries.totalProductPrice) }}
            </span>
            <span>税额：{{ erpPriceInputFormatter(summaries.taxPrice) }}</span>
            <span>
              税额合计：{{ erpPriceInputFormatter(summaries.totalPrice) }}
            </span>
          </div>
        </div>
      </div>
    </template>
  </Grid>
</template>
