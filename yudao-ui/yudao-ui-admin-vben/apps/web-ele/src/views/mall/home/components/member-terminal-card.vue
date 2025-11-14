<script lang="ts" setup>
import type { DictDataType } from '@vben/hooks';
import type { EchartsUIType } from '@vben/plugins/echarts';

import type { MallMemberStatisticsApi } from '#/api/mall/statistics/member';

import { onMounted, reactive, ref } from 'vue';

import { AnalysisChartCard } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import * as MemberStatisticsApi from '#/api/mall/statistics/member';

/** 会员终端卡片 */
defineOptions({ name: 'MemberTerminalCard' });
const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);
const loading = ref(true); // 加载中

/** 会员终端统计图配置 */
const terminalChartOptions = reactive({
  tooltip: {
    trigger: 'item' as const,
    confine: true,
    formatter: '{a} <br/>{b} : {c} ({d}%)',
  },
  legend: {
    orient: 'vertical' as const,
    left: 'right' as const,
  },
  roseType: 'area',
  series: [
    {
      name: '会员终端',
      type: 'pie' as const,
      label: {
        show: false,
      },
      labelLine: {
        show: false,
      },
      data: [] as { name: string; value: number }[],
    },
  ],
});

/** 按照终端，查询会员统计列表 */
const getMemberTerminalStatisticsList = async () => {
  loading.value = true;
  const list = await MemberStatisticsApi.getMemberTerminalStatisticsList();
  const dictDataList = getDictOptions(DICT_TYPE.TERMINAL, 'number');
  if (terminalChartOptions.series && terminalChartOptions.series.length > 0) {
    (terminalChartOptions.series[0] as any).data = dictDataList.map(
      (dictData: DictDataType) => {
        const userCount = list.find(
          (item: MallMemberStatisticsApi.TerminalStatistics) =>
            item.terminal === dictData.value,
        )?.userCount;
        return {
          name: dictData.label,
          value: userCount || 0,
        };
      },
    );
  }
  loading.value = false;
};

/** 初始化 */
onMounted(async () => {
  await getMemberTerminalStatisticsList();
  renderEcharts(terminalChartOptions);
});
</script>
<template>
  <AnalysisChartCard title="会员终端">
    <EchartsUI ref="chartRef" />
  </AnalysisChartCard>
</template>
