<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallCouponApi } from '#/api/mall/promotion/coupon/coupon';

import { ref } from 'vue';

import { DocAlert, Page } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { ElLoading, ElMessage, ElTabPane, ElTabs } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteCoupon,
  getCouponPage,
} from '#/api/mall/promotion/coupon/coupon';

import { useGridColumns, useGridFormSchema } from './data';

defineOptions({ name: 'PromotionCoupon' });

const activeTab = ref('all');
const statusTabs = ref(getStatusTabs());

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 删除优惠券 */
async function handleDelete(row: MallCouponApi.Coupon) {
  const loadingInstance = ElLoading.service({
    text: '回收中...',
  });
  try {
    await deleteCoupon(row.id!);
    ElMessage.success('回收成功');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 获取状态选项卡配置 */
function getStatusTabs() {
  const tabs = [
    {
      label: '全部',
      value: 'all',
    },
  ];
  const statusOptions = getDictOptions(DICT_TYPE.PROMOTION_COUPON_STATUS);
  for (const option of statusOptions) {
    tabs.push({
      label: option.label,
      value: String(option.value),
    });
  }
  return tabs;
}

/** Tab 切换 */
function handleTabChange(tabName: any) {
  activeTab.value = tabName;
  gridApi.query();
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
          const params = {
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
            // Tab状态过滤
            status:
              activeTab.value === 'all' ? undefined : Number(activeTab.value),
          };
          return await getCouponPage(params);
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
  } as VxeTableGridOptions<MallCouponApi.Coupon>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【营销】优惠劵"
        url="https://doc.iocoder.cn/mall/promotion-coupon/"
      />
    </template>

    <Grid>
      <template #toolbar-actions>
        <ElTabs
          :model-value="activeTab"
          class="w-full"
          @tab-change="handleTabChange"
        >
          <ElTabPane
            v-for="tab in statusTabs"
            :key="tab.value"
            :label="tab.label"
            :name="tab.value"
          />
        </ElTabs>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '回收',
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['promotion:coupon:delete'],
              popConfirm: {
                title:
                  '回收将会收回会员领取的待使用的优惠券，已使用的将无法回收，确定要回收所选优惠券吗？',
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
