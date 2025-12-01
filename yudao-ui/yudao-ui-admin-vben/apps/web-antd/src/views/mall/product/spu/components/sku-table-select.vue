<!-- SKU 选择弹窗组件 -->
<script lang="ts" setup>
import type { MallSpuApi } from '#/api/mall/product/spu';

import { ref } from 'vue';

import { Modal } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getSpu } from '#/api/mall/product/spu';

import { useSkuGridColumns } from './spu-select-data';

interface SpuData {
  spuId: number;
}

const emit = defineEmits<{
  change: [sku: MallSpuApi.Sku];
}>();

const visible = ref(false);
const spuId = ref<number>();

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useSkuGridColumns(),
    height: 400,
    border: true,
    radioConfig: {
      reserve: true,
      highlight: true,
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    pagerConfig: {
      enabled: false,
    },
  },
  gridEvents: {
    radioChange: () => {
      const selectedRow = gridApi.grid.getRadioRecord() as MallSpuApi.Sku;
      if (selectedRow) {
        emit('change', selectedRow);
        // 关闭弹窗
        visible.value = false;
        gridApi.grid.clearRadioRow();
        spuId.value = undefined;
      }
    },
  },
});

/** 关闭弹窗 */
function closeModal() {
  visible.value = false;
  spuId.value = undefined;
}

/** 打开弹窗 */
async function openModal(data?: SpuData) {
  if (!data?.spuId) {
    return;
  }
  spuId.value = data.spuId;
  visible.value = true;
  // 注意：useVbenVxeGrid 关闭分页(pagerConfig.enabled=false)后，proxyConfig.ajax.query 的结果不会传递到 vxe-table
  // 需要手动调用 reloadData 设置表格数据
  if (!spuId.value) {
    gridApi.grid?.reloadData([]);
    return;
  }
  const spu = await getSpu(spuId.value);
  gridApi.grid?.reloadData(spu.skus || []);
}

/** 对外暴露的方法 */
defineExpose({
  open: openModal,
});
</script>

<template>
  <Modal
    v-model:open="visible"
    title="选择规格"
    width="700px"
    :destroy-on-close="true"
    :footer="null"
    @cancel="closeModal"
  >
    <Grid />
  </Modal>
</template>
