<script lang="ts" setup>
import type { EchartsUIType } from '@vben/plugins/echarts';

import { onMounted, ref } from 'vue';

import { getDictOptions } from '@vben/hooks';
import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import { ElCard } from 'element-plus';

import { getMemberTerminalStatisticsList } from '#/api/mall/statistics/member';

import { getTerminalChartOptions } from './terminal-chart-options';

/** 会员终端卡片 */
defineOptions({ name: 'MemberTerminalCard' });

const loading = ref(true);
const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);

/** 按照终端，查询会员统计列表 */
const loadMemberTerminalStatisticsList = async () => {
  loading.value = true;
  try {
    const list = await getMemberTerminalStatisticsList();
    const dictDataList = getDictOptions('terminal', 'number');
    const chartData = dictDataList.map((dictData: any) => {
      const userCount = list.find(
        (item: any) => item.terminal === dictData.value,
      )?.userCount;
      return {
        name: dictData.label,
        value: userCount || 0,
      };
    });
    // 更新 Echarts 数据
    await renderEcharts(getTerminalChartOptions(chartData));
  } finally {
    loading.value = false;
  }
};

/** 初始化 */
onMounted(() => {
  loadMemberTerminalStatisticsList();
});
</script>

<template>
  <ElCard class="h-full">
    <template #header>
      <span>会员终端</span>
    </template>
    <div v-loading="loading">
      <EchartsUI ref="chartRef" />
    </div>
  </ElCard>
</template>
