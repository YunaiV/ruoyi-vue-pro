<script setup lang="ts">
import type { IotStatisticsApi } from '#/api/iot/statistics';

import { computed, nextTick, onMounted, ref, watch } from 'vue';

import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import { Card, Empty } from 'ant-design-vue';

import { getDeviceCountPieChartOptions } from '../chart-options';

defineOptions({ name: 'DeviceCountCard' });

const props = defineProps<{
  loading?: boolean;
  statsData: IotStatisticsApi.StatisticsSummary;
}>();

const deviceCountChartRef = ref();
const { renderEcharts } = useEcharts(deviceCountChartRef);

/** 是否有数据 */
const hasData = computed(() => {
  if (!props.statsData) return false;
  const categories = Object.entries(
    props.statsData.productCategoryDeviceCounts || {},
  );
  return categories.length > 0 && props.statsData.deviceCount !== 0;
});

/** 初始化图表 */
async function initChart() {
  if (!hasData.value) {
    return;
  }

  await nextTick();
  const data = Object.entries(props.statsData.productCategoryDeviceCounts).map(
    ([name, value]) => ({ name, value }),
  );
  await renderEcharts(getDeviceCountPieChartOptions(data));
}

/** 监听数据变化 */
watch(
  () => props.statsData,
  () => {
    initChart();
  },
  { deep: true },
);

/** 组件挂载时初始化图表 */
onMounted(() => {
  initChart();
});
</script>

<template>
  <Card title="设备数量统计" :loading="loading" class="h-full">
    <div
      v-if="loading && !hasData"
      class="flex h-[300px] items-center justify-center"
    >
      <Empty description="加载中..." />
    </div>
    <div
      v-else-if="!hasData"
      class="flex h-[300px] items-center justify-center"
    >
      <Empty description="暂无数据" />
    </div>
    <div v-else>
      <EchartsUI ref="deviceCountChartRef" class="h-[400px] w-full" />
    </div>
  </Card>
</template>

<style scoped>
:deep(.ant-card-body) {
  padding: 20px;
}
</style>
