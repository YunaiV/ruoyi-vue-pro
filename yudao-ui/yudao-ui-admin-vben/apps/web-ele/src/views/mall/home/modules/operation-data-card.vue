<script lang="ts" setup>
import { onActivated, onMounted, reactive } from 'vue';
import { useRouter } from 'vue-router';

import { CountTo } from '@vben/common-ui';

import { ElCard } from 'element-plus';

import { getTabsCount } from '#/api/mall/product/spu';
import { getWalletRechargePrice } from '#/api/mall/statistics/pay';
import { getOrderCount } from '#/api/mall/statistics/trade';

/** 运营数据卡片 */
defineOptions({ name: 'OperationDataCard' });

const router = useRouter();

/** 数据项接口 */
interface DataItem {
  name: string;
  value: number;
  routerName: string;
  prefix?: string;
  decimals?: number;
}

/** 数据 */
const data = reactive({
  orderUndelivered: { name: '待发货订单', value: 0, routerName: 'TradeOrder' },
  orderAfterSaleApply: {
    name: '退款中订单',
    value: 0,
    routerName: 'TradeAfterSale',
  },
  orderWaitePickUp: { name: '待核销订单', value: 0, routerName: 'TradeOrder' },
  productAlertStock: { name: '库存预警', value: 0, routerName: 'ProductSpu' },
  productForSale: { name: '上架商品', value: 0, routerName: 'ProductSpu' },
  productInWarehouse: { name: '仓库商品', value: 0, routerName: 'ProductSpu' },
  withdrawAuditing: {
    name: '提现待审核',
    value: 0,
    routerName: 'TradeBrokerageWithdraw',
  },
  rechargePrice: {
    name: '账户充值',
    value: 0,
    prefix: '￥',
    decimals: 2,
    routerName: 'PayWalletRecharge',
  },
});

/** 查询订单数据 */
async function loadOrderData() {
  const orderCount = await getOrderCount();
  if (orderCount.undelivered) {
    data.orderUndelivered.value = orderCount.undelivered;
  }
  if (orderCount.afterSaleApply) {
    data.orderAfterSaleApply.value = orderCount.afterSaleApply;
  }
  if (orderCount.pickUp) {
    data.orderWaitePickUp.value = orderCount.pickUp;
  }
  if (orderCount.auditingWithdraw) {
    data.withdrawAuditing.value = orderCount.auditingWithdraw;
  }
}

/** 查询商品数据 */
async function loadProductData() {
  const productCount = await getTabsCount();
  data.productForSale.value = productCount['0'] || 0;
  data.productInWarehouse.value = productCount['1'] || 0;
  data.productAlertStock.value = productCount['3'] || 0;
}

/** 查询钱包充值数据 */
async function loadWalletRechargeData() {
  const paySummary = await getWalletRechargePrice();
  data.rechargePrice.value = paySummary.rechargePrice;
}

/** 跳转到对应页面 */
function handleClick(routerName: string) {
  router.push({ name: routerName });
}

/** 激活时 */
onActivated(() => {
  loadOrderData();
  loadProductData();
  loadWalletRechargeData();
});

/** 初始化 */
onMounted(() => {
  loadOrderData();
  loadProductData();
  loadWalletRechargeData();
});
</script>

<template>
  <ElCard :border="false">
    <template #header>
      <div>运营数据</div>
    </template>
    <div class="flex flex-row flex-wrap items-center gap-8 p-4">
      <div
        v-for="(item, key) in data"
        :key="key"
        class="flex h-20 w-[20%] cursor-pointer flex-col items-center justify-center gap-2"
        @click="handleClick(item.routerName)"
      >
        <CountTo
          :decimals="(item as DataItem).decimals ?? 0"
          :end-val="item.value"
          :prefix="(item as DataItem).prefix ?? ''"
          class="text-3xl"
        />
        <span class="text-center">{{ item.name }}</span>
      </div>
    </div>
  </ElCard>
</template>
