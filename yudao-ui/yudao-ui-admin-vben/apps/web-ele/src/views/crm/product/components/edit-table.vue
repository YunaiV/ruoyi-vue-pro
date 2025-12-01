<script lang="ts" setup>
import type { CrmBusinessApi } from '#/api/crm/business';
import type { CrmContractApi } from '#/api/crm/contract';
import type { CrmProductApi } from '#/api/crm/product';

import { nextTick, onMounted, ref, watch } from 'vue';

import { erpPriceMultiply } from '@vben/utils';

import { ElInputNumber, ElOption, ElSelect } from 'element-plus';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { BizTypeEnum } from '#/api/crm/permission';
import { getProductSimpleList } from '#/api/crm/product';
import { $t } from '#/locales';

import { useProductEditTableColumns } from './data';

const props = defineProps<{
  bizType: BizTypeEnum;
  products?:
    | CrmBusinessApi.BusinessProduct[]
    | CrmContractApi.ContractProduct[];
}>();

const emit = defineEmits(['update:products']);

/** 表格内部数据 */
const tableData = ref<any[]>([]);

/** 添加产品行 */
function handleAdd() {
  gridApi.grid.insertAt(null, -1);
}

/** 删除产品行 */
function handleDelete(row: CrmProductApi.Product) {
  gridApi.grid.remove(row);
}

/** 切换产品时同步基础信息 */
function handleProductChange(productId: any, row: any) {
  const product = productOptions.value.find((p) => p.id === productId);
  if (!product) {
    return;
  }
  row.productUnit = product.unit;
  row.productNo = product.no;
  row.productPrice = product.price;
  row.sellingPrice = product.price;
  row.count = 0;
  row.totalPrice = 0;
  handleUpdateValue(row);
}

/** 金额变动时重新计算合计 */
function handlePriceChange(row: any) {
  row.totalPrice = erpPriceMultiply(row.sellingPrice, row.count) ?? 0;
  handleUpdateValue(row);
}

/** 将最新数据写回并通知父组件 */
function handleUpdateValue(row: any) {
  const index = tableData.value.findIndex((item) => item.id === row.id);
  if (props.bizType === BizTypeEnum.CRM_BUSINESS) {
    row.businessPrice = row.sellingPrice;
  } else if (props.bizType === BizTypeEnum.CRM_CONTRACT) {
    row.contractPrice = row.sellingPrice;
  }
  if (index === -1) {
    row.id = tableData.value.length + 1;
    tableData.value.push(row);
  } else {
    tableData.value[index] = row;
  }
  emit('update:products', [...tableData.value]);
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    editConfig: {
      trigger: 'click',
      mode: 'cell',
    },
    columns: useProductEditTableColumns(),
    data: tableData.value,
    border: true,
    showOverflow: true,
    autoResize: true,
    keepSource: true,
    rowConfig: {
      keyField: 'id',
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
  () => props.products,
  async (products) => {
    if (!products) {
      return;
    }
    await nextTick();
    tableData.value = products;
    if (props.bizType === BizTypeEnum.CRM_BUSINESS) {
      tableData.value.forEach((item) => {
        item.sellingPrice = item.businessPrice;
      });
    } else if (props.bizType === BizTypeEnum.CRM_CONTRACT) {
      tableData.value.forEach((item) => {
        item.sellingPrice = item.contractPrice;
      });
    }
    await gridApi.grid.reloadData(tableData.value);
  },
  {
    immediate: true,
  },
);

/** 初始化 */
const productOptions = ref<CrmProductApi.Product[]>([]); // 产品下拉选项
onMounted(async () => {
  productOptions.value = await getProductSimpleList();
});
</script>

<template>
  <Grid class="w-full">
    <template #productId="{ row }">
      <ElSelect
        v-model="row.productId"
        :field-names="{ label: 'name', value: 'id' }"
        class="w-full"
        @change="handleProductChange($event, row)"
      >
        <ElOption
          v-for="option in productOptions"
          :key="option.id"
          :label="option.name"
          :value="option.id"
        />
      </ElSelect>
    </template>
    <template #sellingPrice="{ row }">
      <ElInputNumber
        v-model="row.sellingPrice"
        :min="0.001"
        :precision="2"
        controls-position="right"
        class="!w-full"
        @change="handlePriceChange(row)"
      />
    </template>
    <template #count="{ row }">
      <ElInputNumber
        v-model="row.count"
        :min="0.001"
        :precision="3"
        controls-position="right"
        class="!w-full"
        @change="handlePriceChange(row)"
      />
    </template>
    <template #bottom>
      <TableAction
        class="mt-4 flex justify-center"
        :actions="[
          {
            label: '添加产品',
            type: 'default',
            onClick: handleAdd,
          },
        ]"
      />
    </template>
    <template #actions="{ row }">
      <TableAction
        :actions="[
          {
            label: $t('common.delete'),
            type: 'danger',
            link: true,
            popConfirm: {
              title: $t('ui.actionMessage.deleteConfirm', [row.name]),
              confirm: handleDelete.bind(null, row),
            },
          },
        ]"
      />
    </template>
  </Grid>
</template>
