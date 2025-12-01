<script lang="ts" setup>
import type { EchartsUIType } from '@vben/plugins/echarts';

import { ref } from 'vue';

import { ContentWrap, Page } from '@vben/common-ui';
import { EchartsUI, useEcharts } from '@vben/plugins/echarts';
import { addTime, betweenDay, formatDate } from '@vben/utils';

import { ElCard, ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  getInterfaceSummary,
  getUpstreamMessage,
  getUserCumulate,
  getUserSummary,
} from '#/api/mp/statistics';
import { WxAccountSelect } from '#/views/mp/components';

import {
  interfaceSummaryOption,
  upstreamMessageOption,
  userCumulateOption,
  userSummaryOption,
} from './chart-options';
import { useGridFormSchema } from './data';

const userSummaryRef = ref<EchartsUIType>();
const { renderEcharts: renderUserSummaryEcharts } = useEcharts(userSummaryRef);

const userCumulateRef = ref<EchartsUIType>();
const { renderEcharts: renderUserCumulateEcharts } =
  useEcharts(userCumulateRef);

const upstreamMessageRef = ref<EchartsUIType>();
const { renderEcharts: renderUpstreamMessageEcharts } =
  useEcharts(upstreamMessageRef);

const interfaceSummaryRef = ref<EchartsUIType>();
const { renderEcharts: renderInterfaceSummaryEcharts } =
  useEcharts(interfaceSummaryRef);

/** 加载数据 */
async function getSummary(values: Record<string, any>) {
  const accountId = values.accountId;
  if (!accountId) {
    ElMessage.warning('请先选择公众号');
    return;
  }
  const dateRange = values.dateRange;
  if (!dateRange) {
    ElMessage.warning('请先选择时间范围');
    return;
  }
  // 必须选择 7 天内，因为公众号有时间跨度限制为 7
  if (betweenDay(dateRange[0], dateRange[1]) >= 7) {
    ElMessage.error('时间间隔 7 天以内，请重新选择');
    return;
  }
  // 处理日期
  const days = betweenDay(dateRange[0], dateRange[1]);
  const dates = Array.from(
    { length: days },
    (_, index) =>
      formatDate(addTime(dateRange[0], index), 'YYYY-MM-DD') as string,
  );

  // 用户增减数据
  const userSummaryData = await getUserSummary({
    accountId,
    date: dateRange,
  });
  await renderUserSummaryEcharts(userSummaryOption(userSummaryData, dates));
  // 累计用户数据
  const userCumulateData = await getUserCumulate({
    accountId,
    date: dateRange,
  });
  await renderUserCumulateEcharts(userCumulateOption(userCumulateData, dates));
  // 消息发送概况数据
  const upstreamMessageData = await getUpstreamMessage({
    accountId,
    date: dateRange,
  });
  await renderUpstreamMessageEcharts(
    upstreamMessageOption(upstreamMessageData, dates),
  );
  // 接口分析数据
  const interfaceSummaryData = await getInterfaceSummary({
    accountId,
    date: dateRange,
  });
  await renderInterfaceSummaryEcharts(
    interfaceSummaryOption(interfaceSummaryData, dates),
  );
}

/** 公众号变化时查询数据 */
function handleAccountChange(accountId: number) {
  queryFormApi.setValues({ accountId });
  queryFormApi.submitForm();
}

const [QueryForm, queryFormApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  layout: 'horizontal',
  schema: useGridFormSchema(),
  wrapperClass: 'grid-cols-1 md:grid-cols-2',
  handleSubmit: getSummary,
});
</script>

<template>
  <Page auto-content-height>
    <ContentWrap class="h-full w-full">
      <QueryForm>
        <template #accountId>
          <WxAccountSelect @change="handleAccountChange" />
        </template>
      </QueryForm>

      <div class="flex h-1/3 w-full gap-4">
        <ElCard class="h-full w-1/2">
          <template #header>
            <span>用户增减数据</span>
          </template>
          <EchartsUI ref="userSummaryRef" />
        </ElCard>
        <ElCard class="h-full w-1/2">
          <template #header>
            <span>累计用户数据</span>
          </template>
          <EchartsUI ref="userCumulateRef" />
        </ElCard>
      </div>
      <div class="mt-4 flex h-1/3 w-full gap-4">
        <ElCard class="h-full w-1/2">
          <template #header>
            <span>消息发送概况数据</span>
          </template>
          <EchartsUI ref="upstreamMessageRef" />
        </ElCard>
        <ElCard class="h-full w-1/2">
          <template #header>
            <span>接口分析数据</span>
          </template>
          <EchartsUI ref="interfaceSummaryRef" />
        </ElCard>
      </div>
    </ContentWrap>
  </Page>
</template>
