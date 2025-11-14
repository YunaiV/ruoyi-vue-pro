<script lang="ts" setup>
import type {
  WorkbenchProjectItem,
  WorkbenchQuickNavItem,
} from '@vben/common-ui';

import type { AnalysisOverviewItem } from './components/data';

import type { WorkbenchQuickDataShowItem } from '#/views/mall/home/components/data';

import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { DocAlert, Page, WorkbenchQuickNav } from '@vben/common-ui';
import { isString, openWindow } from '@vben/utils';

import { getTabsCount } from '#/api/mall/product/spu';
import { getUserCountComparison } from '#/api/mall/statistics/member';
import { getWalletRechargePrice } from '#/api/mall/statistics/pay';
import { getOrderComparison, getOrderCount } from '#/api/mall/statistics/trade';

import AnalysisOverview from './components/analysis-overview.vue';
import MemberFunnelCard from './components/member-funnel-card.vue';
import MemberStatisticsCard from './components/member-statistics-card.vue';
import MemberTerminalCard from './components/member-terminal-card.vue';
import TradeTrendCard from './components/trade-trend-card.vue';
import WorkbenchQuickDataShow from './components/workbench-quick-data-show.vue';

/** 商城首页 */
defineOptions({ name: 'MallHome' });

const loading = ref(true); // 加载中
const orderComparison = ref(); // 交易对照数据
const userComparison = ref(); // 用户对照数据
const data = ref({
  orderUndelivered: 0,
  orderAfterSaleApply: 0,
  orderWaitePickUp: 0,
  withdrawAuditing: 0,
  productForSale: 0,
  productInWarehouse: 0,
  productAlertStock: 0,
  rechargePrice: 0,
});

const dataShow = ref(false);

/** 查询交易对照卡片数据 */
const getOrder = async () => {
  orderComparison.value = await getOrderComparison();
};

/** 查询会员用户数量对照卡片数据 */
const getUserCount = async () => {
  userComparison.value = await getUserCountComparison();
};

/** 查询订单数据 */
const getOrderData = async () => {
  const orderCount = await getOrderCount();
  if (orderCount.undelivered) {
    data.value.orderUndelivered = orderCount.undelivered;
  }
  if (orderCount.afterSaleApply) {
    data.value.orderAfterSaleApply = orderCount.afterSaleApply;
  }
  if (orderCount.pickUp) {
    data.value.orderWaitePickUp = orderCount.pickUp;
  }
  if (orderCount.auditingWithdraw) {
    data.value.withdrawAuditing = orderCount.auditingWithdraw;
  }
};

/** 查询商品数据 */
const getProductData = async () => {
  // TODO: @芋艿：这个接口的返回值，是不是用命名字段更好些？
  const productCount = await getTabsCount();
  data.value.productForSale = productCount['0'] || 0;
  data.value.productInWarehouse = productCount['1'] || 0;
  data.value.productAlertStock = productCount['3'] || 0;
};

/** 查询钱包充值数据 */
const getWalletRechargeData = async () => {
  const paySummary = await getWalletRechargePrice();
  data.value.rechargePrice = paySummary.rechargePrice;
};

/** 初始化 */
onMounted(async () => {
  loading.value = true;
  await Promise.all([
    getOrder(),
    getUserCount(),
    getOrderData(),
    getProductData(),
    getWalletRechargeData(),
  ]);
  loading.value = false;
  dataShow.value = true;
  loadDataShow();
  loadOverview();
});

const overviewItems = ref<AnalysisOverviewItem[]>([]);
const loadOverview = () => {
  overviewItems.value = [
    {
      title: '今日销售额',
      totalTitle: '昨日数据',
      totalValue: orderComparison.value?.reference?.orderPayPrice || 0,
      value: orderComparison.value?.orderPayPrice || 0,
      showGrowthRate: true,
    },
    {
      title: '今日用户访问量',
      totalTitle: '总访问量',
      totalValue: userComparison.value?.reference?.visitUserCount || 0,
      value: userComparison.value?.visitUserCount || 0,
      showGrowthRate: true,
    },
    {
      title: '今日订单量',
      totalTitle: '总订单量',
      totalValue: orderComparison.value?.orderPayCount || 0,
      value: orderComparison.value?.reference?.orderPayCount || 0,
      showGrowthRate: true,
    },
    {
      title: '今日会员注册量',
      totalTitle: '总会员注册量',
      totalValue: userComparison.value?.registerUserCount || 0,
      value: userComparison.value?.reference?.registerUserCount || 0,
      showGrowthRate: true,
    },
  ];
};

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

const quickDataShowItems = ref<WorkbenchQuickDataShowItem[]>();

const loadDataShow = () => {
  quickDataShowItems.value = [
    {
      name: '待发货订单',
      value: data.value.orderUndelivered,
      prefix: '',
      decimals: 0,
      routerName: 'TradeOrder',
    },
    {
      name: '退款中订单',
      value: data.value.orderAfterSaleApply,
      prefix: '',
      decimals: 0,
      routerName: 'TradeAfterSale',
    },
    {
      name: '待核销订单',
      value: data.value.orderWaitePickUp,
      routerName: 'TradeOrder',
      prefix: '',
      decimals: 0,
    },
    {
      name: '库存预警',
      value: data.value.productAlertStock,
      routerName: 'ProductSpu',
      prefix: '',
      decimals: 0,
    },
    {
      name: '上架商品',
      value: data.value.productForSale,
      routerName: 'ProductSpu',
      prefix: '',
      decimals: 0,
    },
    {
      name: '仓库商品',
      value: data.value.productInWarehouse,
      routerName: 'ProductSpu',
      prefix: '',
      decimals: 0,
    },
    {
      name: '提现待审核',
      value: data.value.withdrawAuditing,
      routerName: 'TradeBrokerageWithdraw',
      prefix: '',
      decimals: 0,
    },
    {
      name: '账户充值',
      value: data.value.rechargePrice,
      prefix: '￥',
      decimals: 2,
      routerName: 'PayWalletRecharge',
    },
  ];
};

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
    <div class="mt-5 w-full md:flex">
      <AnalysisOverview
        v-model:model-value="overviewItems"
        class="mt-5 md:mr-4 md:mt-0 md:w-full"
      />
    </div>
    <div class="mt-5 w-full md:flex">
      <WorkbenchQuickNav
        :items="quickNavItems"
        class="mt-5 md:mr-4 md:mt-0 md:w-1/2"
        title="快捷导航"
        @click="navTo"
      />
      <WorkbenchQuickDataShow
        v-if="dataShow"
        v-model:model-value="quickDataShowItems"
        title="运营数据"
        class="mt-5 md:mr-4 md:mt-0 md:w-1/2"
      />
    </div>
    <div class="mb-4 mt-5 w-full md:flex">
      <MemberFunnelCard class="mt-5 md:mr-4 md:mt-0 md:w-2/3" />
      <MemberTerminalCard class="mt-5 md:mr-4 md:mt-0 md:w-1/3" />
    </div>
    <div class="mb-4 mt-5 w-full md:flex">
      <TradeTrendCard class="mt-5 md:mr-4 md:mt-0 md:w-full" />
    </div>
    <div class="mb-4 mt-5 w-full md:flex">
      <MemberStatisticsCard class="mt-5 md:mr-4 md:mt-0 md:w-full" />
    </div>
  </Page>
</template>
