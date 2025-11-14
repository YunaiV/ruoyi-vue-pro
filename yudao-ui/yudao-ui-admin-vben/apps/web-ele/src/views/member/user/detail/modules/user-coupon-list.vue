<script setup lang="ts">
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallCouponApi } from '#/api/mall/promotion/coupon/coupon';

import { ref, watch } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { ElLoading, ElMessage, ElTabPane, ElTabs } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteCoupon,
  getCouponPage,
} from '#/api/mall/promotion/coupon/coupon';
import { getRangePickerDefaultProps } from '#/utils';

const props = defineProps<{
  userId: number;
}>();

// 添加当前选中的状态
const activeStatus = ref<number | string>('all');

/** 删除按钮操作 */
const handleDelete = async (row: MallCouponApi.Coupon) => {
  const loadingInstance = ElLoading.service({
    text: '回收将会收回会员领取的待使用的优惠券，已使用的将无法回收，确定要回收所选优惠券吗？',
  });
  try {
    await deleteCoupon(row.id as number);
    ElMessage.success('回收成功');
    // 重新加载列表
    gridApi.query();
  } finally {
    loadingInstance.close();
  }
};

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: [
      {
        fieldName: 'createTime',
        label: '创建时间',
        component: 'RangePicker',
        componentProps: {
          ...getRangePickerDefaultProps(),
          clearable: true,
        },
      },
    ],
  },
  gridOptions: {
    columns: [
      {
        field: 'name',
        title: '优惠劵',
      },
      {
        field: 'discountType',
        title: 'discountType',
        cellRender: {
          name: 'CellDict',
          props: { type: DICT_TYPE.PROMOTION_DISCOUNT_TYPE },
        },
      },
      {
        field: 'takeType',
        title: '领取方式',
        cellRender: {
          name: 'CellDict',
          props: { type: DICT_TYPE.PROMOTION_COUPON_TAKE_TYPE },
        },
      },
      {
        field: 'status',
        title: '状态',
        cellRender: {
          name: 'CellDict',
          props: { type: DICT_TYPE.PROMOTION_COUPON_STATUS },
        },
      },
      {
        field: 'createTime',
        title: '领取时间',
        formatter: 'formatDateTime',
      },
      {
        field: 'useTime',
        title: '使用时间',
        formatter: 'formatDateTime',
      },
      {
        title: '操作',
        width: 180,
        fixed: 'right',
        slots: { default: 'actions' },
      },
    ],
    keepSource: true,
    pagerConfig: {
      pageSize: 10,
    },
    expandConfig: {
      trigger: 'row',
      expandAll: true,
      padding: true,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getCouponPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            userId: props.userId,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
    },
    toolbarConfig: {
      refresh: true,
      search: true,
      slots: {
        buttons: 'customTop',
      },
    },
  } as VxeTableGridOptions<MallCouponApi.Coupon>,
  separator: false,
});

// 监听标签页变化，更新表单状态值并触发查询
watch(activeStatus, (val) => {
  // 使用formApi获取表单对象
  if (gridApi.formApi) {
    // 设置状态值
    gridApi.formApi.setFieldValue(
      'status',
      val === 'all' ? undefined : Number(val),
    );

    // 触发查询
    gridApi.query({ status: val === 'all' ? undefined : Number(val) });
  }
});
</script>

<template>
  <Grid>
    <template #customTop>
      <ElTabs v-model="activeStatus">
        <ElTabPane label="全部" name="all" />
        <ElTabPane
          v-for="item in getDictOptions(
            DICT_TYPE.PROMOTION_COUPON_STATUS,
            'number',
          )"
          :key="String(item.value)"
          :label="item.label"
          :name="String(item.value)"
        />
      </ElTabs>
    </template>
    <template #actions="{ row }">
      <TableAction
        :actions="[
          {
            label: '回收',
            link: true,
            icon: ACTION_ICON.EDIT,
            auth: ['promotion:coupon:delete'],
            onClick: handleDelete.bind(null, row),
          },
        ]"
      />
    </template>
  </Grid>
</template>
