<!-- SKU 选择弹窗组件 -->
<script lang="ts" setup>
import type { VxeGridProps } from '#/adapter/vxe-table';
import type { MallSpuApi } from '#/api/mall/product/spu';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { fenToYuan } from '@vben/utils';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getSpu } from '#/api/mall/product/spu';

interface SpuData {
  spuId: number;
}

const emit = defineEmits<{
  change: [sku: MallSpuApi.Sku];
}>();

const spuId = ref<number>();

/** 表格列配置 */
const gridColumns = computed<VxeGridProps['columns']>(() => [
  {
    type: 'radio',
    width: 55,
  },
  {
    field: 'picUrl',
    title: '图片',
    width: 100,
    align: 'center',
    cellRender: {
      name: 'CellImage',
    },
  },
  {
    field: 'properties',
    title: '规格',
    minWidth: 120,
    align: 'center',
    formatter: ({ cellValue }) => {
      return (
        cellValue?.map((p: MallSpuApi.Property) => p.valueName)?.join(' ') ||
        '-'
      );
    },
  },
  {
    field: 'price',
    title: '销售价(元)',
    width: 120,
    align: 'center',
    formatter: ({ cellValue }) => {
      return fenToYuan(cellValue);
    },
  },
]);

/** 处理选中 */
function handleRadioChange() {
  const selectedRow = gridApi.grid.getRadioRecord() as MallSpuApi.Sku;
  if (selectedRow) {
    emit('change', selectedRow);
    modalApi.close();
  }
}

// TODO @芋艿：要不要直接非 pager？
const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: gridColumns.value,
    height: 400,
    border: true,
    showOverflow: true,
    radioConfig: {
      reserve: true,
    },
    proxyConfig: {
      ajax: {
        query: async () => {
          if (!spuId.value) {
            return { list: [], total: 0 };
          }
          const spu = await getSpu(spuId.value);
          return {
            list: spu.skus || [],
            total: spu.skus?.length || 0,
          };
        },
      },
    },
  },
  gridEvents: {
    radioChange: handleRadioChange,
  },
});

const [Modal, modalApi] = useVbenModal({
  destroyOnClose: true,
  onOpenChange: async (isOpen: boolean) => {
    if (!isOpen) {
      gridApi.grid.clearRadioRow();
      spuId.value = undefined;
      return;
    }
    const data = modalApi.getData<SpuData>();
    if (!data?.spuId) {
      return;
    }
    spuId.value = data.spuId;
    await gridApi.query();
  },
});
</script>

<template>
  <Modal class="w-[700px]" title="选择规格">
    <Grid />
  </Modal>
</template>
