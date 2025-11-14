<script setup lang="ts">
import type { DictDataType } from '@vben/hooks';
import type { EchartsUIType } from '@vben/plugins/echarts';

import type { MallMemberStatisticsApi } from '#/api/mall/statistics/member';

import { onMounted, reactive, ref } from 'vue';

import { AnalysisChartCard } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import * as MemberStatisticsApi from '#/api/mall/statistics/member';

const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);
const sexChartOptions = reactive({
  tooltip: {
    trigger: 'item' as const,
    confine: true,
    formatter: '{a} <br/>{b} : {c} ({d}%)',
  },
  legend: {
    orient: 'vertical' as const,
    left: 'right',
  },
  roseType: 'area',
  series: [
    {
      name: '会员性别',
      type: 'pie' as const,
      label: {
        show: false,
      },
      labelLine: {
        show: false,
      },
      data: [] as any[],
    },
  ],
});

/** 按照性别，查询会员统计列表 */
const getMemberSexStatisticsList = async () => {
  const list = await MemberStatisticsApi.getMemberSexStatisticsList();
  const dictDataList = getDictOptions(DICT_TYPE.SYSTEM_USER_SEX, 'number');
  dictDataList.push({ label: '未知', value: null } as any);
  (sexChartOptions.series[0] as any).data = dictDataList.map(
    (dictData: DictDataType) => {
      const userCount = list.find(
        (item: MallMemberStatisticsApi.SexStatistics) =>
          item.sex === dictData.value,
      )?.userCount;
      return {
        name: dictData.label,
        value: userCount || 0,
      };
    },
  );
};

onMounted(async () => {
  await getMemberSexStatisticsList();
  renderEcharts(sexChartOptions);
});
</script>

<template>
  <AnalysisChartCard title="会员性别分布">
    <EchartsUI ref="chartRef" />
  </AnalysisChartCard>
</template>
