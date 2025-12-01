<script setup lang="ts">
import type { MemberUserApi } from '#/api/member/user';
import type { PayWalletApi } from '#/api/pay/wallet/balance';

import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

import { Page, useVbenModal } from '@vben/common-ui';
import { useTabs } from '@vben/hooks';

import { Button, Card, message, TabPane, Tabs } from 'ant-design-vue';

import { getUser } from '#/api/member/user';
import { getWallet } from '#/api/pay/wallet/balance';
import { $t } from '#/locales';

import Form from '../modules/form.vue';
import AccountInfo from './modules/account-info.vue';
import AddressList from './modules/address-list.vue';
import AfterSaleList from './modules/after-sale-list.vue';
import BalanceList from './modules/balance-list.vue';
import BasicInfo from './modules/basic-info.vue';
import BrokerageList from './modules/brokerage-list.vue';
import CouponList from './modules/coupon-list.vue';
import ExperienceRecordList from './modules/experience-record-list.vue';
import FavoriteList from './modules/favorite-list.vue';
import OrderList from './modules/order-list.vue';
import PointList from './modules/point-list.vue';
import SignList from './modules/sign-list.vue';

const route = useRoute();
const { closeCurrentTab, refreshTab } = useTabs();

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const userId = Number(route.query.id);
const user = ref<MemberUserApi.User>();
const wallet = ref<PayWalletApi.Wallet>();

/** 获取会员详情 */
async function getUserDetail() {
  if (!userId) {
    message.error('参数错误，会员编号不能为空！');
    await closeCurrentTab();
    return;
  }
  user.value = await getUser(userId);
  wallet.value = (await getWallet({ userId })) || {
    balance: 0,
    totalExpense: 0,
    totalRecharge: 0,
  };
}

/** 编辑会员 */
function handleEdit() {
  formModalApi.setData(user.value).open();
}

/** 初始化 */
onMounted(async () => {
  await getUserDetail();
});
</script>
<template>
  <Page auto-content-height>
    <FormModal @success="refreshTab" />
    <div class="flex">
      <BasicInfo v-if="user" class="w-3/5" :user="user" mode="member">
        <template #title> 基本信息 </template>
        <template #extra>
          <Button type="primary" @click="handleEdit">
            {{ $t('common.edit') }}
          </Button>
        </template>
      </BasicInfo>
      <AccountInfo
        v-if="user && wallet"
        class="ml-4 w-2/5"
        :user="user"
        :wallet="wallet"
      >
        <template #title> 账户信息 </template>
      </AccountInfo>
    </div>
    <div class="mt-4">
      <Card title="账户明细">
        <Tabs>
          <TabPane tab="积分" key="PointList">
            <PointList class="h-full" :user-id="userId" />
          </TabPane>
          <TabPane tab="签到" key="SignList">
            <SignList class="h-full" :user-id="userId" />
          </TabPane>
          <TabPane tab="成长值" key="ExperienceRecordList">
            <ExperienceRecordList class="h-full" :user-id="userId" />
          </TabPane>
          <TabPane tab="余额" key="BalanceList">
            <BalanceList class="h-full" :wallet-id="wallet?.id" />
          </TabPane>
          <TabPane tab="收货地址" key="AddressList">
            <AddressList class="h-full" :user-id="userId" />
          </TabPane>
          <TabPane tab="订单管理" key="OrderList">
            <OrderList class="h-full" :user-id="userId" />
          </TabPane>
          <TabPane tab="售后管理" key="AfterSaleList">
            <AfterSaleList class="h-full" :user-id="userId" />
          </TabPane>
          <TabPane tab="收藏记录" key="FavoriteList">
            <FavoriteList class="h-full" :user-id="userId" />
          </TabPane>
          <TabPane tab="优惠劵" key="CouponList">
            <CouponList class="h-full" :user-id="userId" />
          </TabPane>
          <TabPane tab="推广用户" key="BrokerageList">
            <BrokerageList class="h-full" :user-id="userId" />
          </TabPane>
        </Tabs>
      </Card>
    </div>
  </Page>
</template>
