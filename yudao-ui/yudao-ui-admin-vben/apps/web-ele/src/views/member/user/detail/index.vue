<script setup lang="ts">
import type { MemberUserApi } from '#/api/member/user';
import type { PayWalletApi } from '#/api/pay/wallet/balance';

import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

import { Page, useVbenModal } from '@vben/common-ui';
import { useTabs } from '@vben/hooks';

import { ElButton, ElCard, ElMessage, ElTabPane, ElTabs } from 'element-plus';

import { getUser } from '#/api/member/user';
import { getWallet } from '#/api/pay/wallet/balance';
import { $t } from '#/locales';

import Form from '../modules/form.vue';
import AccountInfo from './modules/account-info.vue';
import BalanceList from './modules/balance-list.vue';
import BasicInfo from './modules/basic-info.vue';
import ExperienceRecordList from './modules/experience-record-list.vue';
import PointList from './modules/point-list.vue';
import SignList from './modules/sign-list.vue';
import UserAddressList from './modules/user-address-list.vue';
import UserAfterSaleList from './modules/user-after-sale-list.vue';
import UserBrokerageList from './modules/user-brokerage-list.vue';
import UserCouponList from './modules/user-coupon-list.vue';
import UserFavoriteList from './modules/user-favorite-list.vue';
import UserOrderList from './modules/user-order-list.vue';

const route = useRoute();
const { closeCurrentTab, refreshTab } = useTabs();

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const userId = Number(route.query.id);
const user = ref<MemberUserApi.User>();
const wallet = ref<PayWalletApi.Wallet>();
const activeName = ref('PointList');

/** 获取会员详情 */
async function getUserDetail() {
  if (!userId) {
    ElMessage.error('参数错误，会员编号不能为空！');
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
          <ElButton type="primary" @click="handleEdit">
            {{ $t('common.edit') }}
          </ElButton>
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
      <ElCard title="账户明细">
        <ElTabs v-model="activeName">
          <ElTabPane label="积分" name="PointList">
            <PointList class="h-full" :user-id="userId" />
          </ElTabPane>
          <ElTabPane label="签到" name="SignList">
            <SignList class="h-full" :user-id="userId" />
          </ElTabPane>
          <ElTabPane label="成长值" name="ExperienceRecordList">
            <ExperienceRecordList class="h-full" :user-id="userId" />
          </ElTabPane>
          <ElTabPane label="余额" name="BalanceList">
            <BalanceList class="h-full" :wallet-id="wallet?.id" />
          </ElTabPane>
          <ElTabPane label="收货地址" name="AddressList">
            <UserAddressList class="h-full" :user-id="userId" />
          </ElTabPane>
          <ElTabPane label="订单管理" name="OrderList">
            <!-- Todo: 商城模块 -->
            <div class="h-full">
              <UserOrderList class="h-full" :user-id="userId" />
            </div>
          </ElTabPane>
          <ElTabPane label="售后管理" name="AfterSaleList">
            <!-- Todo: 商城模块 -->
            <div class="h-full">
              <UserAfterSaleList class="h-full" :user-id="userId" />
            </div>
          </ElTabPane>
          <ElTabPane label="收藏记录" name="FavoriteList">
            <!-- Todo: 商城模块 -->
            <div class="h-full">
              <UserFavoriteList class="h-full" :user-id="userId" />
            </div>
          </ElTabPane>
          <ElTabPane label="优惠劵" name="CouponList">
            <!-- Todo: 商城模块 -->
            <div class="h-full">
              <UserCouponList class="h-full" :user-id="userId" />
            </div>
          </ElTabPane>
          <ElTabPane label="推广用户" name="BrokerageList">
            <!-- Todo: 商城模块 -->
            <div class="h-full">
              <UserBrokerageList class="h-full" :user-id="userId" />
            </div>
          </ElTabPane>
        </ElTabs>
      </ElCard>
    </div>
  </Page>
</template>
