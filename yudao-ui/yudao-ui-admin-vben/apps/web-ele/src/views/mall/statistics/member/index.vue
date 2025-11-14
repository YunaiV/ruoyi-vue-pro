<script lang="ts" setup>
import type { MallMemberStatisticsApi } from '#/api/mall/statistics/member'; // 会员统计数据
import type { AnalysisOverviewIconItem } from '#/views/mall/home/components/data';

import { onMounted, ref } from 'vue';

import { DocAlert, Page } from '@vben/common-ui';
import { fenToYuan } from '@vben/utils';

import * as MemberStatisticsApi from '#/api/mall/statistics/member'; // 会员统计数据
import AnalysisOverviewIcon from '#/views/mall/home/components/analysis-overview-icon.vue';
import MemberFunnelCard from '#/views/mall/home/components/member-funnel-card.vue';
import MemberTerminalCard from '#/views/mall/home/components/member-terminal-card.vue';

import MemberRegionCard from './components/member-region-card.vue';
import MemberSexCard from './components/member-sex-card.vue';

const summary = ref<MallMemberStatisticsApi.Summary>();

const overviewItems = ref<AnalysisOverviewIconItem[]>([]);
const loadOverview = async () => {
  summary.value = await MemberStatisticsApi.getMemberSummary();
  overviewItems.value = [
    {
      icon: 'fa-solid:users', // 自定义立体用户群组图标 - 累计会员数
      title: '累计会员数',
      value: summary.value?.userCount || 0,
      iconBgColor: 'text-blue-500',
      iconColor: 'bg-blue-100',
    },
    {
      icon: 'fa-solid:user', // 自定义立体信用卡图标 - 累计充值人数
      title: '累计充值人数',
      value: summary.value?.rechargeUserCount || 0,
      iconBgColor: 'text-purple-500',
      iconColor: 'bg-purple-100',
    },
    {
      icon: 'fa-solid:money-check-alt', // 自定义立体钞票图标 - 累计充值金额
      title: '累计充值金额',
      value: Number(fenToYuan(summary.value?.rechargePrice || 0)),
      iconBgColor: 'text-yellow-500',
      iconColor: 'bg-yellow-100',
      prefix: '￥',
      decimals: 2,
    },
    {
      icon: 'fa-solid:yen-sign', // 自定义立体用户添加图标 - 今日会员注册量
      title: '累计消费金额',
      value: Number(fenToYuan(summary.value?.expensePrice || 0)),
      iconBgColor: 'text-green-500',
      iconColor: 'bg-green-100',
      prefix: '￥',
      decimals: 2,
    },
  ];
};

onMounted(async () => {
  loadOverview();
});
</script>

<template>
  <Page>
    <DocAlert
      title="【统计】会员、商品、交易统计"
      url="https://doc.iocoder.cn/mall/statistics/"
    />
    <div class="mt-5 w-full md:flex">
      <AnalysisOverviewIcon
        v-model:model-value="overviewItems"
        class="mt-5 md:mr-4 md:mt-0 md:w-full"
      />
    </div>
    <div class="mb-4 mt-5 w-full md:flex">
      <MemberFunnelCard class="mt-5 md:mr-4 md:mt-0 md:w-2/3" />
      <MemberTerminalCard class="mt-5 md:mr-4 md:mt-0 md:w-1/3" />
    </div>
    <div class="mb-4 mt-5 w-full md:flex">
      <MemberRegionCard class="mt-5 md:mr-4 md:mt-0 md:w-2/3" />
      <MemberSexCard class="mt-5 md:mr-4 md:mt-0 md:w-1/3" />
    </div>
  </Page>
</template>
