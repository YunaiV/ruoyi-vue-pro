<script setup lang="ts">
import type { IotStatisticsApi } from '#/api/iot/statistics';

import { computed, nextTick, onMounted, ref, watch } from 'vue';

import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import { Card, Col, Empty, Row } from 'ant-design-vue';

defineOptions({ name: 'DeviceStateCountCard' });

const props = defineProps<{
  loading?: boolean;
  statsData: IotStatisticsApi.StatisticsSummary;
}>();

const deviceOnlineChartRef = ref();
const deviceOfflineChartRef = ref();
const deviceInactiveChartRef = ref();

const { renderEcharts: renderOnlineChart } = useEcharts(deviceOnlineChartRef);
const { renderEcharts: renderOfflineChart } = useEcharts(deviceOfflineChartRef);
const { renderEcharts: renderInactiveChart } = useEcharts(
  deviceInactiveChartRef,
);

/** 是否有数据 */
const hasData = computed(() => {
  if (!props.statsData) return false;
  return props.statsData.deviceCount !== 0;
});

/** 获取仪表盘配置 */
const getGaugeOption = (value: number, color: string, title: string): any => {
  return {
    series: [
      {
        type: 'gauge',
        startAngle: 225,
        endAngle: -45,
        min: 0,
        max: props.statsData.deviceCount || 100,
        center: ['50%', '50%'],
        radius: '80%',
        progress: {
          show: true,
          width: 12,
          itemStyle: {
            color,
          },
        },
        axisLine: {
          lineStyle: {
            width: 12,
            color: [[1, '#E5E7EB']] as [number, string][],
          },
        },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { show: false },
        pointer: { show: false },
        title: {
          show: true,
          offsetCenter: [0, '80%'],
          fontSize: 14,
          color: '#666',
        },
        detail: {
          valueAnimation: true,
          fontSize: 32,
          fontWeight: 'bold',
          color,
          offsetCenter: [0, '10%'],
          formatter: (val: number) => `${val} 个`,
        },
        data: [{ value, name: title }],
      },
    ],
  };
};

/** 初始化图表 */
function initCharts() {
  if (!hasData.value) return;

  nextTick(() => {
    // 在线设备
    renderOnlineChart(
      getGaugeOption(props.statsData.deviceOnlineCount, '#52c41a', '在线设备'),
    );
    // 离线设备
    renderOfflineChart(
      getGaugeOption(props.statsData.deviceOfflineCount, '#ff4d4f', '离线设备'),
    );
    // 待激活设备
    renderInactiveChart(
      getGaugeOption(
        props.statsData.deviceInactiveCount,
        '#1890ff',
        '待激活设备',
      ),
    );
  });
}

/** 监听数据变化 */
watch(
  () => props.statsData,
  () => {
    initCharts();
  },
  { deep: true },
);

/** 组件挂载时初始化图表 */
onMounted(() => {
  initCharts();
});
</script>

<template>
  <Card title="设备状态统计" :loading="loading" class="chart-card">
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
    <Row v-else class="h-[280px]">
      <Col :span="8" class="flex items-center justify-center">
        <EchartsUI ref="deviceOnlineChartRef" class="h-[250px] w-full" />
      </Col>
      <Col :span="8" class="flex items-center justify-center">
        <EchartsUI ref="deviceOfflineChartRef" class="h-[250px] w-full" />
      </Col>
      <Col :span="8" class="flex items-center justify-center">
        <EchartsUI ref="deviceInactiveChartRef" class="h-[250px] w-full" />
      </Col>
    </Row>
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
