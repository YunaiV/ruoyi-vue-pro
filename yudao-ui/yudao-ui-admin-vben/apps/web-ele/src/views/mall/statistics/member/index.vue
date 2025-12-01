<script lang="ts" setup>
import type { MallMemberStatisticsApi } from '#/api/mall/statistics/member';

import { onMounted, ref } from 'vue';

import { DocAlert, Page, SummaryCard } from '@vben/common-ui';
import { fenToYuan } from '@vben/utils';

import { ElCol, ElRow } from 'element-plus';

import { getMemberSummary } from '#/api/mall/statistics/member';

import MemberAreaCard from './modules/area-card.vue';
import MemberFunnelCard from './modules/funnel-card.vue';
import MemberSexCard from './modules/sex-card.vue';
import MemberTerminalCard from './modules/terminal-card.vue';

/** 会员统计 */
defineOptions({ name: 'MemberStatistics' });

const loading = ref(true); // 加载中
const summary = ref<MallMemberStatisticsApi.SummaryRespVO>(); // 会员统计数据

/** 查询会员统计 */
async function loadMemberSummary() {
  summary.value = await getMemberSummary();
}

/** 初始化 */
onMounted(async () => {
  loading.value = true;
  try {
    await loadMemberSummary();
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【统计】会员、商品、交易统计"
        url="https://doc.iocoder.cn/mall/statistics/"
      />
    </template>

    <div class="flex flex-col gap-4">
      <!-- 统计卡片 -->
      <ElRow :gutter="16">
        <ElCol :md="6" :sm="12" :xs="24">
          <SummaryCard
            title="累计会员数"
            :value="summary?.userCount || 0"
            icon="fa-solid:users"
            icon-color="text-blue-500"
            icon-bg-color="bg-blue-100"
          />
        </ElCol>
        <ElCol :md="6" :sm="12" :xs="24">
          <SummaryCard
            title="累计充值人数"
            :value="summary?.rechargeUserCount || 0"
            icon="fa-solid:user"
            icon-color="text-purple-500"
            icon-bg-color="bg-purple-100"
          />
        </ElCol>
        <ElCol :md="6" :sm="12" :xs="24">
          <SummaryCard
            title="累计充值金额"
            :value="Number(fenToYuan(summary?.rechargePrice || 0))"
            :decimals="2"
            prefix="￥"
            icon="fa-solid:money-check-alt"
            icon-color="text-yellow-500"
            icon-bg-color="bg-yellow-100"
          />
        </ElCol>
        <ElCol :md="6" :sm="12" :xs="24">
          <SummaryCard
            title="累计消费金额"
            :value="Number(fenToYuan(summary?.expensePrice || 0))"
            :decimals="2"
            prefix="￥"
            icon="fa-solid:yen-sign"
            icon-color="text-green-500"
            icon-bg-color="bg-green-100"
          />
        </ElCol>
      </ElRow>
      <!-- 会员概览和会员终端 -->
      <ElRow :gutter="16">
        <ElCol :md="18" :sm="24" :xs="24">
          <MemberFunnelCard />
        </ElCol>
        <ElCol :md="6" :sm="24" :xs="24">
          <MemberTerminalCard />
        </ElCol>
      </ElRow>
      <!-- 会员地域分布和性别比例 -->
      <ElRow :gutter="16">
        <ElCol :md="18" :sm="24" :xs="24">
          <MemberAreaCard />
        </ElCol>
        <ElCol :md="6" :sm="24" :xs="24">
          <MemberSexCard />
        </ElCol>
      </ElRow>
    </div>
  </Page>
</template>
