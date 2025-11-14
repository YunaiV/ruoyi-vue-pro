<script setup lang="ts">
import type { Dayjs } from 'dayjs';

import type { IotStatisticsApi } from '#/api/iot/statistics';

import { computed, nextTick, onMounted, reactive, ref } from 'vue';

import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import { Button, Card, DatePicker, Empty, Space } from 'ant-design-vue';
import dayjs from 'dayjs';

import { getDeviceMessageSummaryByDate } from '#/api/iot/statistics';

defineOptions({ name: 'MessageTrendCard' });

const { RangePicker } = DatePicker;

const messageChartRef = ref();
const { renderEcharts } = useEcharts(messageChartRef);

const loading = ref(false);
const messageData = ref<IotStatisticsApi.DeviceMessageSummaryByDate[]>([]);
const activeTimeRange = ref('7d'); // 当前选中的时间范围
const dateRange = ref<[Dayjs, Dayjs] | undefined>(undefined);

const queryParams = reactive<IotStatisticsApi.DeviceMessageReq>({
  interval: 1, // 按天
  times: [],
});

// 是否有数据
const hasData = computed(() => {
  return messageData.value && messageData.value.length > 0;
});

// 设置时间范围
function setTimeRange(range: string) {
  activeTimeRange.value = range;
  dateRange.value = undefined; // 清空自定义时间选择

  let start: Dayjs;
  const end = dayjs();

  switch (range) {
    case '1h': {
      start = dayjs().subtract(1, 'hour');
      queryParams.interval = 1; // 按分钟
      break;
    }
    case '7d': {
      start = dayjs().subtract(7, 'day');
      queryParams.interval = 1; // 按天
      break;
    }
    case '24h': {
      start = dayjs().subtract(24, 'hour');
      queryParams.interval = 1; // 按小时
      break;
    }
    default: {
      start = dayjs().subtract(7, 'day');
      queryParams.interval = 1;
    }
  }

  queryParams.times = [
    start.format('YYYY-MM-DD HH:mm:ss'),
    end.format('YYYY-MM-DD HH:mm:ss'),
  ];

  fetchMessageData();
}

// 处理自定义日期选择
function handleDateChange() {
  if (dateRange.value && dateRange.value.length === 2) {
    activeTimeRange.value = ''; // 清空快捷选择
    queryParams.interval = 1; // 按天
    queryParams.times = [
      dateRange.value[0].startOf('day').format('YYYY-MM-DD HH:mm:ss'),
      dateRange.value[1].endOf('day').format('YYYY-MM-DD HH:mm:ss'),
    ];
    fetchMessageData();
  }
}

// 获取消息统计数据
async function fetchMessageData() {
  if (!queryParams.times || queryParams.times.length !== 2) return;

  loading.value = true;
  try {
    messageData.value = await getDeviceMessageSummaryByDate(queryParams);
    await nextTick();
    initChart();
  } catch (error) {
    console.error('获取消息统计数据失败:', error);
    messageData.value = [];
  } finally {
    loading.value = false;
  }
}

// 初始化图表
function initChart() {
  if (!hasData.value) return;

  const times = messageData.value.map((item) => item.time);
  const upstreamData = messageData.value.map((item) => item.upstreamCount);
  const downstreamData = messageData.value.map((item) => item.downstreamCount);

  renderEcharts({
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985',
        },
      },
    },
    legend: {
      data: ['上行消息', '下行消息'],
      top: '5%',
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true,
    },
    xAxis: [
      {
        type: 'category',
        boundaryGap: false,
        data: times,
      },
    ],
    yAxis: [
      {
        type: 'value',
        name: '消息数量',
      },
    ],
    series: [
      {
        name: '上行消息',
        type: 'line',
        smooth: true,
        areaStyle: {
          opacity: 0.3,
        },
        emphasis: {
          focus: 'series',
        },
        data: upstreamData,
        itemStyle: {
          color: '#1890ff',
        },
      },
      {
        name: '下行消息',
        type: 'line',
        smooth: true,
        areaStyle: {
          opacity: 0.3,
        },
        emphasis: {
          focus: 'series',
        },
        data: downstreamData,
        itemStyle: {
          color: '#52c41a',
        },
      },
    ],
  });
}

// 组件挂载时查询数据
onMounted(() => {
  setTimeRange('7d'); // 默认显示近一周数据
});
</script>

<template>
  <Card class="chart-card" :loading="loading">
    <template #title>
      <div class="flex flex-wrap items-center justify-between gap-2">
        <span class="text-base font-medium">上下行消息量统计</span>
        <Space :size="8">
          <Button
            :type="activeTimeRange === '1h' ? 'primary' : 'default'"
            size="small"
            @click="setTimeRange('1h')"
          >
            最近1小时
          </Button>
          <Button
            :type="activeTimeRange === '24h' ? 'primary' : 'default'"
            size="small"
            @click="setTimeRange('24h')"
          >
            最近24小时
          </Button>
          <Button
            :type="activeTimeRange === '7d' ? 'primary' : 'default'"
            size="small"
            @click="setTimeRange('7d')"
          >
            近一周
          </Button>
          <RangePicker
            v-model:value="dateRange"
            format="YYYY-MM-DD"
            :placeholder="['开始时间', '结束时间']"
            @change="handleDateChange"
            size="small"
            style="width: 240px"
          />
        </Space>
      </div>
    </template>

    <div v-if="loading" class="flex h-[350px] items-center justify-center">
      <Empty description="加载中..." />
    </div>
    <div
      v-else-if="!hasData"
      class="flex h-[350px] items-center justify-center"
    >
      <Empty description="暂无数据" />
    </div>
    <div v-else>
      <EchartsUI ref="messageChartRef" class="h-[350px] w-full" />
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

.chart-card :deep(.ant-card-head) {
  border-bottom: 1px solid #f0f0f0;
}
</style>
