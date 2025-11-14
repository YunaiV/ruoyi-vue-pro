<script lang="ts" setup>
import type {
  AnalysisOverviewItem,
  WorkbenchProjectItem,
  WorkbenchQuickNavItem,
} from '@vben/common-ui';

import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import {
  AnalysisOverview,
  DocAlert,
  Page,
  WorkbenchQuickNav,
} from '@vben/common-ui';
import {
  SvgBellIcon,
  SvgCakeIcon,
  SvgCardIcon,
  SvgDownloadIcon,
} from '@vben/icons';
import { isString, openWindow } from '@vben/utils';

import { getUserCountComparison } from '#/api/mall/statistics/member';
import { getOrderComparison } from '#/api/mall/statistics/trade';

/** 商城首页 */
defineOptions({ name: 'MallHome' });

const loading = ref(true); // 加载中
const orderComparison = ref(); // 交易对照数据
const userComparison = ref(); // 用户对照数据

/** 查询交易对照卡片数据 */
const getOrder = async () => {
  orderComparison.value = await getOrderComparison();
};

/** 查询会员用户数量对照卡片数据 */
const getUserCount = async () => {
  userComparison.value = await getUserCountComparison();
};

/** 初始化 */
onMounted(async () => {
  loading.value = true;
  await Promise.all([getOrder(), getUserCount()]);
  loading.value = false;
});

const overviewItems: AnalysisOverviewItem[] = [
  {
    icon: SvgCardIcon,
    title: '今日销售额',
    totalTitle: '昨日数据',
    totalValue: orderComparison.value?.reference?.orderPayPrice || 0,
    value: orderComparison.value?.orderPayPrice || 0,
  },
  {
    icon: SvgCakeIcon,
    title: '今日用户访问量',
    totalTitle: '总访问量',
    totalValue: userComparison.value?.reference?.visitUserCount || 0,
    value: userComparison.value?.visitUserCount || 0,
  },
  {
    icon: SvgDownloadIcon,
    title: '今日订单量',
    totalTitle: '总订单量',
    totalValue: orderComparison.value?.orderPayCount || 0,
    value: orderComparison.value?.reference?.orderPayCount || 0,
  },
  {
    icon: SvgBellIcon,
    title: '今日会员注册量',
    totalTitle: '总会员注册量',
    totalValue: userComparison.value?.registerUserCount || 0,
    value: userComparison.value?.reference?.registerUserCount || 0,
  },
];

// 同样，这里的 url 也可以使用以 http 开头的外部链接
const quickNavItems: WorkbenchQuickNavItem[] = [
  {
    color: '#1fdaca',
    icon: 'ep:user-filled',
    title: '用户管理',
    url: 'MemberUser',
  },
  {
    color: '#ff6b6b',
    icon: 'fluent-mdl2:product',
    title: '商品管理',
    url: 'ProductSpu',
  },
  {
    color: '#7c3aed',
    icon: 'ep:list',
    title: '订单管理',
    url: 'TradeOrder',
  },
  {
    color: '#3fb27f',
    icon: 'ri:refund-2-line',
    title: '售后管理',
    url: 'TradeAfterSale',
  },
  {
    color: '#4daf1bc9',
    icon: 'fa-solid:project-diagram',
    title: '分销管理',
    url: 'TradeBrokerageUser',
  },
  {
    color: '#1a73e8',
    icon: 'ep:ticket',
    title: '优惠券',
    url: 'PromotionCoupon',
  },
  {
    color: '#4daf1bc9',
    icon: 'fa:group',
    title: '拼团活动',
    url: 'PromotionBargainActivity',
  },
  {
    color: '#1a73e8',
    icon: 'vaadin:money-withdraw',
    title: '佣金提现',
    url: 'TradeBrokerageWithdraw',
  },
  {
    color: '#1a73e8',
    icon: 'vaadin:money-withdraw',
    title: '数据统计',
    url: 'TradeBrokerageWithdraw',
  },
];

const router = useRouter();
function navTo(nav: WorkbenchProjectItem | WorkbenchQuickNavItem) {
  if (nav.url?.startsWith('http')) {
    openWindow(nav.url);
    return;
  }
  if (nav.url?.startsWith('/')) {
    router.push(nav.url).catch((error) => {
      console.error('Navigation failed:', error);
    });
  } else if (isString(nav.url)) {
    router.push({ name: nav.url });
  } else {
    console.warn(`Unknown URL for navigation item: ${nav.title} -> ${nav.url}`);
  }
}
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="商城手册（功能开启）"
        url="https://doc.iocoder.cn/mall/build/"
      />
    </template>
    <AnalysisOverview :items="overviewItems" />
    <div class="mt-5 w-full lg:w-2/5">
      <WorkbenchQuickNav
        :items="quickNavItems"
        class="mt-5 lg:mt-0"
        title="快捷导航"
        @click="navTo"
      />
    </div>
  </Page>
</template>
