<script lang="ts" setup>
import type { EchartsUIType } from '@vben/plugins/echarts';

import type { InfraRedisApi } from '#/api/infra/redis';

import { onMounted, ref, watch } from 'vue';

import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

const props = defineProps<{
  redisData?: InfraRedisApi.RedisMonitorInfo;
}>();

const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);

/** 解析内存值，移除单位，转为数字 */
function parseMemoryValue(memStr: string | undefined): number {
  if (!memStr) {
    return 0;
  }
  try {
    // 从字符串中提取数字部分，例如 "1.2M" 中的 1.2
    const str = String(memStr); // 显式转换为字符串类型
    const match = str.match(/^([\d.]+)/);
    return match ? Number.parseFloat(match[1] as string) : 0;
  } catch {
    return 0;
  }
}

/** 渲染内存使用图表 */
function renderMemoryChart() {
  if (!props.redisData?.info) {
    return;
  }

  // 处理数据
  const usedMemory = props.redisData.info.used_memory_human || '0';
  const memoryValue = parseMemoryValue(usedMemory);

  // 渲染图表
  renderEcharts({
    title: {
      text: '内存使用情况',
      left: 'center',
    },
    tooltip: {
      formatter: `{b} <br/>{a} : ${usedMemory}`,
    },
    series: [
      {
        name: '峰值',
        type: 'gauge',
        min: 0,
        max: 100,
        splitNumber: 10,
        color: '#F5C74E',
        radius: '85%',
        center: ['50%', '50%'],
        startAngle: 225,
        endAngle: -45,
        axisLine: {
          lineStyle: {
            color: [
              [0.2, '#7FFF00'],
              [0.8, '#00FFFF'],
              [1, '#FF0000'],
            ],
            width: 10,
          },
        },
        axisTick: {
          length: 5,
          lineStyle: {
            color: '#76D9D7',
          },
        },
        splitLine: {
          length: 20,
          lineStyle: {
            color: '#76D9D7',
          },
        },
        axisLabel: {
          color: '#76D9D7',
          distance: 15,
          fontSize: 15,
        },
        pointer: {
          width: 7,
          show: true,
        },
        detail: {
          show: true,
          offsetCenter: [0, '50%'],
          color: 'inherit',
          fontSize: 30,
          formatter: usedMemory,
        },
        progress: {
          show: true,
        },
        data: [
          {
            value: memoryValue,
            name: '内存消耗',
          },
        ],
      },
    ],
  });
}

/** 监听数据变化，重新渲染图表 */
watch(
  () => props.redisData,
  (newVal) => {
    if (newVal) {
      renderMemoryChart();
    }
  },
  { deep: true },
);

onMounted(() => {
  if (props.redisData) {
    renderMemoryChart();
  }
});
</script>

<template>
  <EchartsUI ref="chartRef" height="420px" />
</template>
