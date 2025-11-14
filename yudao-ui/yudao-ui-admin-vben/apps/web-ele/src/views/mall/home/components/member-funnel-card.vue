<script lang="ts" setup>
import type { MallMemberStatisticsApi } from '#/api/mall/statistics/member';

import { ref } from 'vue';

import { calculateRelativeRate, fenToYuan } from '@vben/utils';

import dayjs from 'dayjs';

import * as MemberStatisticsApi from '#/api/mall/statistics/member';

import AnalysisChartCard from './analysis-chart-card.vue';
import ShortcutDateRangePicker from './shortcut-date-range-picker.vue';

/** 会员概览卡片 */
defineOptions({ name: 'MemberFunnelCard' });

const loading = ref(true); // 加载中
const analyseData = ref<MallMemberStatisticsApi.Analyse>(); // 会员分析数据

/** 查询会员概览数据列表 */
const handleTimeRangeChange = async (
  times: [dayjs.ConfigType, dayjs.ConfigType],
) => {
  loading.value = true;
  // 查询数据
  analyseData.value = await MemberStatisticsApi.getMemberAnalyse({
    times: [dayjs(times[0]).toDate(), dayjs(times[1]).toDate()],
  });
  loading.value = false;
};
</script>
<template>
  <AnalysisChartCard title="会员概览">
    <template #header-suffix>
      <!-- 查询条件 -->
      <ShortcutDateRangePicker @change="handleTimeRangeChange" />
    </template>
    <template #default>
      <div class="min-w-225 py-1.75" v-loading="loading">
        <div class="relative flex h-24">
          <div class="<lg:w-35% <xl:w-55% h-full w-3/4 bg-blue-50">
            <div class="ml-15 flex h-full flex-col justify-center">
              <div class="font-bold">
                注册用户数量：{{
                  analyseData?.comparison?.value?.registerUserCount || 0
                }}
              </div>
              <div class="text-3.5 mt-2">
                环比增长率：{{
                  calculateRelativeRate(
                    analyseData?.comparison?.value?.registerUserCount,
                    analyseData?.comparison?.reference?.registerUserCount,
                  )
                }}%
              </div>
            </div>
          </div>
          <div
            class="trapezoid1 text-3.5 flex h-full flex-col items-center justify-center bg-blue-500 text-white"
          >
            <span class="text-6 font-bold">{{
              analyseData?.visitUserCount || 0
            }}</span>
            <span>访客</span>
          </div>
        </div>
        <div class="relative flex h-24">
          <div class="<lg:w-35% <xl:w-55% flex h-full w-3/4 bg-cyan-50">
            <div class="ml-15 flex h-full flex-col justify-center">
              <div class="font-bold">
                活跃用户数量：{{
                  analyseData?.comparison?.value?.visitUserCount || 0
                }}
              </div>
              <div class="text-3.5 mt-2">
                环比增长率：{{
                  calculateRelativeRate(
                    analyseData?.comparison?.value?.visitUserCount,
                    analyseData?.comparison?.reference?.visitUserCount,
                  )
                }}%
              </div>
            </div>
          </div>
          <div
            class="trapezoid2 flex flex-col items-center justify-center bg-cyan-500 text-white"
          >
            <span class="text-6 font-bold">{{
              analyseData?.orderUserCount || 0
            }}</span>
            <span>下单</span>
          </div>
        </div>
        <div class="relative flex h-24">
          <div class="<lg:w-35% <xl:w-55% flex w-3/4 bg-slate-50">
            <div class="ml-15 flex h-full flex-row gap-x-16">
              <div class="flex flex-col justify-center">
                <div class="font-bold">
                  充值用户数量：{{
                    analyseData?.comparison?.value?.rechargeUserCount || 0
                  }}
                </div>
                <div class="text-3.5 mt-2">
                  环比增长率：{{
                    calculateRelativeRate(
                      analyseData?.comparison?.value?.rechargeUserCount,
                      analyseData?.comparison?.reference?.rechargeUserCount,
                    )
                  }}%
                </div>
              </div>
              <div class="flex flex-col justify-center">
                <div class="font-bold">
                  客单价：{{ fenToYuan(analyseData?.atv || 0) }}
                </div>
              </div>
            </div>
          </div>
          <div
            class="trapezoid3 flex flex-col items-center justify-center bg-slate-500 text-white"
          >
            <span class="text-6 font-bold">{{
              analyseData?.payUserCount || 0
            }}</span>
            <span>成交用户</span>
          </div>
        </div>
      </div>
    </template>
  </AnalysisChartCard>
</template>
<style lang="scss" scoped>
.trapezoid1 {
  z-index: 1;
  width: 19.25rem;
  margin-top: 0.381rem;
  margin-left: -9.625rem;
  font-size: 0.875rem;
  transform: perspective(5em) rotateX(-11deg);
}

.trapezoid2 {
  z-index: 1;
  width: 14rem;
  height: 6.25rem;
  margin-top: 0.425rem;
  margin-left: -7rem;
  font-size: 0.875rem;
  transform: perspective(7em) rotateX(-20deg);
}

.trapezoid3 {
  z-index: 1;
  width: 9rem;
  height: 5.75rem;
  margin-top: 0.8125rem;
  margin-left: -4.5rem;
  font-size: 0.875rem;
  transform: perspective(3em) rotateX(-13deg);
}
</style>
