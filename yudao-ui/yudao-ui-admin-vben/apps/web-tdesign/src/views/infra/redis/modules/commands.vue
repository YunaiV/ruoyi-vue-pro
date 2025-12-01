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

/** 渲染命令统计图表 */
function renderCommandStats() {
  if (!props.redisData?.commandStats) {
    return;
  }

  // 处理数据
  const commandStats = [] as any[];
  const nameList = [] as string[];
  props.redisData.commandStats.forEach((row) => {
    commandStats.push({
      name: row.command,
      value: row.calls,
    });
    nameList.push(row.command);
  });

  // 渲染图表
  renderEcharts({
    title: {
      text: '命令统计',
      left: 'center',
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b} : {c} ({d}%)',
    },
    legend: {
      type: 'scroll',
      orient: 'vertical',
      right: 30,
      top: 10,
      bottom: 20,
      data: nameList,
      textStyle: {
        color: '#a1a1a1',
      },
    },
    series: [
      {
        name: '命令',
        type: 'pie',
        radius: [20, 120],
        center: ['40%', '60%'],
        data: commandStats,
        roseType: 'radius',
        label: {
          show: true,
        },
        emphasis: {
          label: {
            show: true,
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
      },
    ],
  });
}

/** 监听数据变化，重新渲染图表 */
watch(
  () => props.redisData,
  (newVal) => {
    if (newVal) {
      renderCommandStats();
    }
  },
  { deep: true },
);

onMounted(() => {
  if (props.redisData) {
    renderCommandStats();
  }
});
</script>

<template>
  <EchartsUI ref="chartRef" height="420px" />
</template>
