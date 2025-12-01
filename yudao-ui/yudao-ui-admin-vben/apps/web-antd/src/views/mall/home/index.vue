<script lang="ts" setup>
import type { DataComparisonRespVO } from '#/api/mall/statistics/common';
import type { MallMemberStatisticsApi } from '#/api/mall/statistics/member';
import type { MallTradeStatisticsApi } from '#/api/mall/statistics/trade';

import { onMounted, ref } from 'vue';

import { DocAlert, Page } from '@vben/common-ui';
import { fenToYuan } from '@vben/utils';

import { Col, Row } from 'ant-design-vue';

import { getUserCountComparison } from '#/api/mall/statistics/member';
import { getOrderComparison } from '#/api/mall/statistics/trade';

import MemberFunnelCard from '../statistics/member/modules/funnel-card.vue';
import MemberTerminalCard from '../statistics/member/modules/terminal-card.vue';
import ComparisonCard from './modules/comparison-card.vue';
import MemberStatisticsCard from './modules/member-statistics-card.vue';
import OperationDataCard from './modules/operation-data-card.vue';
import ShortcutCard from './modules/shortcut-card.vue';
import TradeTrendCard from './modules/trade-trend-card.vue';

/** 商城首页 */
defineOptions({ name: 'MallHome' });

const loading = ref(true); // 加载中
const orderComparison =
  ref<DataComparisonRespVO<MallTradeStatisticsApi.TradeOrderSummaryRespVO>>(); // 交易对照数据
const userComparison =
  ref<DataComparisonRespVO<MallMemberStatisticsApi.MemberCountRespVO>>(); // 用户对照数据

/** 查询交易对照卡片数据 */
async function loadOrderComparison() {
  orderComparison.value = await getOrderComparison();
}

/** 查询会员用户数量对照卡片数据 */
async function loadUserCountComparison() {
  userComparison.value = await getUserCountComparison();
}

/** 初始化 */
onMounted(async () => {
  loading.value = true;
  await Promise.all([loadOrderComparison(), loadUserCountComparison()]);
  loading.value = false;
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="商城手册（功能开启）"
        url="https://doc.iocoder.cn/mall/build/"
      />
    </template>

    <div class="flex flex-col gap-4">
      <!-- 数据对照 -->
      <Row :gutter="16">
        <Col :md="6" :sm="12" :xs="24">
          <ComparisonCard
            tag="今日"
            title="销售额"
            prefix="￥"
            :decimals="2"
            :value="fenToYuan(orderComparison?.value?.orderPayPrice || 0)"
            :reference="
              fenToYuan(orderComparison?.reference?.orderPayPrice || 0)
            "
          />
        </Col>
        <Col :md="6" :sm="12" :xs="24">
          <ComparisonCard
            tag="今日"
            title="用户访问量"
            :value="userComparison?.value?.visitUserCount || 0"
            :reference="userComparison?.reference?.visitUserCount || 0"
          />
        </Col>
        <Col :md="6" :sm="12" :xs="24">
          <ComparisonCard
            tag="今日"
            title="订单量"
            :value="orderComparison?.value?.orderPayCount || 0"
            :reference="orderComparison?.reference?.orderPayCount || 0"
          />
        </Col>
        <Col :md="6" :sm="12" :xs="24">
          <ComparisonCard
            tag="今日"
            title="新增用户"
            :value="userComparison?.value?.registerUserCount || 0"
            :reference="userComparison?.reference?.registerUserCount || 0"
          />
        </Col>
      </Row>
      <!-- 快捷入口和运营数据 -->
      <Row :gutter="16">
        <Col :md="12" :xs="24">
          <ShortcutCard />
        </Col>
        <Col :md="12" :xs="24">
          <OperationDataCard />
        </Col>
      </Row>
      <!-- 会员概览和会员终端 -->
      <Row :gutter="16">
        <Col :md="18" :sm="24" :xs="24">
          <MemberFunnelCard />
        </Col>
        <Col :md="6" :sm="24" :xs="24">
          <MemberTerminalCard />
        </Col>
      </Row>
      <!-- 交易量趋势 -->
      <TradeTrendCard />
      <!-- 会员统计 -->
      <MemberStatisticsCard />
    </div>
  </Page>
</template>
