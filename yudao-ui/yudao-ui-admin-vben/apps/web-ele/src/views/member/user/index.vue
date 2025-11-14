<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MemberUserApi } from '#/api/member/user';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { isEmpty } from '@vben/utils';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getUserPage } from '#/api/member/user';
import { $t } from '#/locales';

import { CouponSendForm } from '../../mall/promotion/coupon/components';
import { useGridColumns, useGridFormSchema } from './data';
import BalanceForm from './modules/balance-form.vue';
import Form from './modules/form.vue';
import LevelForm from './modules/level-form.vue';
import PointForm from './modules/point-form.vue';

const router = useRouter();

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const [PointFormModal, pointFormModalApi] = useVbenModal({
  connectedComponent: PointForm,
  destroyOnClose: true,
});

const [BalanceFormModal, balanceFormModalApi] = useVbenModal({
  connectedComponent: BalanceForm,
  destroyOnClose: true,
});

const [LevelFormModal, levelFormModalApi] = useVbenModal({
  connectedComponent: LevelForm,
  destroyOnClose: true,
});

const [CouponSendFormModal, couponSendFormModalApi] = useVbenModal({
  connectedComponent: CouponSendForm,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 编辑会员 */
function handleEdit(row: MemberUserApi.User) {
  formModalApi.setData(row).open();
}

/** 修改会员等级 */
function handleUpdateLevel(row: MemberUserApi.User) {
  levelFormModalApi.setData(row).open();
}

/** 修改会员积分 */
function handleUpdatePoint(row: MemberUserApi.User) {
  pointFormModalApi.setData(row).open();
}

/** 修改会员余额 */
function handleUpdateBalance(row: MemberUserApi.User) {
  balanceFormModalApi.setData(row).open();
}

/** 发送优惠券 */
async function handleSendCoupon() {
  couponSendFormModalApi
    .setData({
      userIds: checkedIds.value,
    })
    .open();
}

const checkedIds = ref<number[]>([]);
function handleRowCheckboxChange({
  records,
}: {
  records: MemberUserApi.User[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 查看会员详情 */
function handleViewDetail(row: MemberUserApi.User) {
  router.push({
    name: 'MemberUserDetail',
    query: {
      id: row.id,
    },
  });
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getUserPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
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
  } as VxeTableGridOptions<MemberUserApi.User>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="会员用户、标签、分组"
        url="https://doc.iocoder.cn/member/user/"
      />
    </template>

    <FormModal @success="handleRefresh" />
    <PointFormModal @success="handleRefresh" />
    <BalanceFormModal @success="handleRefresh" />
    <LevelFormModal @success="handleRefresh" />
    <CouponSendFormModal />
    <Grid table-title="会员列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '发送优惠券',
              type: 'primary',
              icon: 'lucide:mouse-pointer-2',
              disabled: isEmpty(checkedIds),
              auth: ['promotion:coupon:send'],
              onClick: handleSendCoupon,
            },
          ]"
        />
      </template>

      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.detail'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.VIEW,
              onClick: handleViewDetail.bind(null, row),
            },
          ]"
          :drop-down-actions="[
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              auth: ['member:user:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: '修改等级',
              type: 'primary',
              link: true,
              auth: ['member:user:update-level'],
              onClick: handleUpdateLevel.bind(null, row),
            },
            {
              label: '修改积分',
              type: 'primary',
              link: true,
              auth: ['member:user:update-point'],
              onClick: handleUpdatePoint.bind(null, row),
            },
            {
              label: '修改余额',
              type: 'primary',
              link: true,
              auth: ['pay:wallet:update-balance'],
              onClick: handleUpdateBalance.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
