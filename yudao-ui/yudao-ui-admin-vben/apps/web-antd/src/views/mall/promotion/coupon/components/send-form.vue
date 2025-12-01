<script lang="ts" setup>
import type { VxeGridProps } from '#/adapter/vxe-table';
import type { MallCouponTemplateApi } from '#/api/mall/promotion/coupon/couponTemplate';

import { useVbenModal } from '@vben/common-ui';
import { CouponTemplateTakeTypeEnum } from '@vben/constants';

import { message } from 'ant-design-vue';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { sendCoupon } from '#/api/mall/promotion/coupon/coupon';
import { getCouponTemplatePage } from '#/api/mall/promotion/coupon/couponTemplate';

import { useFormSchema, useGridColumns } from './send-form-data';

/** 发送优惠券 */
async function handleSendCoupon(row: MallCouponTemplateApi.CouponTemplate) {
  modalApi.lock();
  try {
    await sendCoupon({
      templateId: row.id,
      userIds: modalApi.getData().userIds,
    });
    message.success('发送成功');
    await modalApi.close();
  } finally {
    modalApi.unlock();
  }
}

const [Grid] = useVbenVxeGrid({
  formOptions: {
    schema: useFormSchema(),
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
            canTakeTypes: [CouponTemplateTakeTypeEnum.ADMIN.type],
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
  } as VxeGridProps<MallCouponTemplateApi.CouponTemplate>,
});

const [Modal, modalApi] = useVbenModal({
  showCancelButton: false,
  showConfirmButton: false,
});
</script>

<template>
  <Modal title="发送优惠劵" class="w-1/2">
    <Grid>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '发送',
              type: 'link',
              auth: ['promotion:coupon:send'],
              onClick: () => handleSendCoupon(row),
            },
          ]"
        />
      </template>
    </Grid>
  </Modal>
</template>
