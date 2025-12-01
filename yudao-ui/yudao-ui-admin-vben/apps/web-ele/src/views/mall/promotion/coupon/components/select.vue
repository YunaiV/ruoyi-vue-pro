<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallCouponTemplateApi } from '#/api/mall/promotion/coupon/couponTemplate';

import { useVbenModal } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getCouponTemplatePage } from '#/api/mall/promotion/coupon/couponTemplate';

import { useGridColumns, useGridFormSchema } from './select-data';

defineOptions({ name: 'CouponSelect' });

const props = defineProps<{
  takeType?: number; // 领取方式
}>();

const emit = defineEmits(['success']);

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

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    // 从 gridApi 获取选中的记录
    const selectedRecords = (gridApi.grid?.getCheckboxRecords() ||
      []) as MallCouponTemplateApi.CouponTemplate[];
    await modalApi.close();
    emit('success', selectedRecords);
  },
});
</script>

<template>
  <Modal title="选择优惠券" class="w-2/3">
    <Grid />
  </Modal>
</template>
