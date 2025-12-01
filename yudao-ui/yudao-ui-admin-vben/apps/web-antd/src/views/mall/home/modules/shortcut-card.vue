<script lang="ts" setup>
import { useRouter } from 'vue-router';

import { IconifyIcon } from '@vben/icons';

import { Card } from 'ant-design-vue';

/** 快捷入口卡片 */
defineOptions({ name: 'ShortcutCard' });

const router = useRouter();

/** 菜单列表 */
const menuList = [
  {
    name: '用户管理',
    icon: 'lucide:user',
    bgColor: 'bg-red-400',
    routerName: 'MemberUser',
  },
  {
    name: '商品管理',
    icon: 'fluent-mdl2:product',
    bgColor: 'bg-orange-400',
    routerName: 'ProductSpu',
  },
  {
    name: '订单管理',
    icon: 'lucide:list',
    bgColor: 'bg-yellow-500',
    routerName: 'TradeOrder',
  },
  {
    name: '售后管理',
    icon: 'ri:refund-2-line',
    bgColor: 'bg-green-600',
    routerName: 'TradeAfterSale',
  },
  {
    name: '分销管理',
    icon: 'fa-solid:project-diagram',
    bgColor: 'bg-cyan-500',
    routerName: 'TradeBrokerageUser',
  },
  {
    name: '优惠券',
    icon: 'lucide:ticket',
    bgColor: 'bg-blue-500',
    routerName: 'PromotionCoupon',
  },
  {
    name: '拼团活动',
    icon: 'lucide:users',
    bgColor: 'bg-purple-500',
    routerName: 'PromotionBargainActivity',
  },
  {
    name: '佣金提现',
    icon: 'vaadin:money-withdraw',
    bgColor: 'bg-rose-500',
    routerName: 'TradeBrokerageWithdraw',
  },
];

/** 跳转到菜单对应页面 */
function handleMenuClick(routerName: string) {
  router.push({ name: routerName });
}
</script>

<template>
  <Card :bordered="false" title="快捷入口">
    <div class="flex flex-row flex-wrap gap-8 p-4">
      <div
        v-for="menu in menuList"
        :key="menu.name"
        class="flex h-20 w-[20%] cursor-pointer flex-col items-center justify-center gap-2"
        @click="handleMenuClick(menu.routerName)"
      >
        <div
          :class="menu.bgColor"
          class="flex h-12 w-12 items-center justify-center rounded text-white"
        >
          <IconifyIcon :icon="menu.icon" class="text-2xl" />
        </div>
        <span>{{ menu.name }}</span>
      </div>
    </div>
  </Card>
</template>
