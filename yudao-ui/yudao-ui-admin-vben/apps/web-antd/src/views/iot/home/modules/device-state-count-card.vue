<script setup lang="ts">
import type { IotStatisticsApi } from '#/api/iot/statistics';

import { computed, nextTick, onMounted, ref, watch } from 'vue';

import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import { Card, Col, Empty, Row } from 'ant-design-vue';

import { getDeviceStateGaugeChartOptions } from '../chart-options';

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

/** 初始化图表 */
async function initCharts() {
  if (!hasData.value) {
    return;
  }

  await nextTick();
  const max = props.statsData.deviceCount || 100;
  // 在线设备
  await renderOnlineChart(
    getDeviceStateGaugeChartOptions(
      props.statsData.deviceOnlineCount,
      max,
      '#52c41a',
      '在线设备',
    ),
  );
  // 离线设备
  await renderOfflineChart(
    getDeviceStateGaugeChartOptions(
      props.statsData.deviceOfflineCount,
      max,
      '#ff4d4f',
      '离线设备',
    ),
  );
  // 待激活设备
  await renderInactiveChart(
    getDeviceStateGaugeChartOptions(
      props.statsData.deviceInactiveCount,
      max,
      '#1890ff',
      '待激活设备',
    ),
  );
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
  <Card title="设备状态统计" :loading="loading" class="h-full">
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
:deep(.ant-card-body) {
  padding: 20px;
}
</style>
