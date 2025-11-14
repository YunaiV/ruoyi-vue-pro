<script setup lang="ts">
import type { IotStatisticsApi } from '#/api/iot/statistics';

import { computed, nextTick, onMounted, ref, watch } from 'vue';

import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import { Card, Empty } from 'ant-design-vue';

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
function initChart() {
  if (!hasData.value) return;

  nextTick(() => {
    const data = Object.entries(
      props.statsData.productCategoryDeviceCounts,
    ).map(([name, value]) => ({ name, value }));

    renderEcharts({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} 个 ({d}%)',
      },
      legend: {
        type: 'scroll',
        orient: 'horizontal',
        bottom: '10px',
        left: 'center',
        icon: 'circle',
        itemWidth: 10,
        itemHeight: 10,
        itemGap: 12,
        textStyle: {
          fontSize: 12,
        },
        pageButtonPosition: 'end',
        pageIconSize: 12,
        pageTextStyle: {
          fontSize: 12,
        },
        pageFormatter: '{current}/{total}',
      },
      series: [
        {
          name: '设备数量',
          type: 'pie',
          radius: ['35%', '55%'],
          center: ['50%', '40%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 8,
            borderColor: '#fff',
            borderWidth: 2,
          },
          label: {
            show: false,
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 16,
              fontWeight: 'bold',
            },
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)',
            },
          },
          labelLine: {
            show: false,
          },
          data,
        },
      ],
    });
  });
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
  <Card title="设备数量统计" :loading="loading" class="chart-card">
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
.chart-card {
  height: 100%;
}

.chart-card :deep(.ant-card-body) {
  padding: 20px;
}
</style>
