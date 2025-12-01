<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallCouponTemplateApi } from '#/api/mall/promotion/coupon/couponTemplate';

import { ref } from 'vue';

import { Modal } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getCouponTemplatePage } from '#/api/mall/promotion/coupon/couponTemplate';

import { useGridColumns, useGridFormSchema } from './select-data';

defineOptions({ name: 'CouponSelect' });

const props = defineProps<{
  takeType?: number; // 领取方式
}>();

const emit = defineEmits<{
  (e: 'change', v: MallCouponTemplateApi.CouponTemplate[]): void;
}>();

const visible = ref(false); // 弹窗显示状态

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 500,
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getCouponTemplatePage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
            canTakeTypes: props.takeType ? [props.takeType] : [],
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MallCouponTemplateApi.CouponTemplate>,
});

/** 打开弹窗 */
async function open() {
  visible.value = true;
  // 重置查询条件并重新加载数据，与老组件行为一致
  await gridApi.query();
}

/** 关闭弹窗 */
function closeModal() {
  visible.value = false;
}

/** 确认选择 */
function handleConfirm() {
  // 从 gridApi 获取选中的记录
  const selectedRecords = (gridApi.grid?.getCheckboxRecords() ||
    []) as MallCouponTemplateApi.CouponTemplate[];
  emit('change', selectedRecords);
  closeModal();
}

/** 对外暴露的方法 */
defineExpose({
  open,
});
</script>

<template>
  <Modal
    v-model:open="visible"
    title="选择优惠券"
    width="65%"
    :destroy-on-close="true"
    @ok="handleConfirm"
    @cancel="closeModal"
  >
    <Grid />
  </Modal>
</template>
