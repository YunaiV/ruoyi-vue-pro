<!-- 设备物模型 -> 运行状态 -> 查看数据（设备的属性值历史）-->
// 重新关闭打开图表,图表不显示可能图例注销失败等大佬修复
<script setup lang="ts">
import type { Dayjs } from 'dayjs';

import type { EchartsUIType } from '@vben/plugins/echarts';

import type { IotDeviceApi } from '#/api/iot/device/device';

import { computed, nextTick, reactive, ref, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { EchartsUI, useEcharts } from '@vben/plugins/echarts';
import { beginOfDay, endOfDay, formatDate, formatDateTime } from '@vben/utils';

import {
  Button,
  Empty,
  message,
  Modal,
  RangePicker,
  Space,
  Spin,
  Table,
  Tag,
} from 'ant-design-vue';
import dayjs from 'dayjs';

import { getHistoryDevicePropertyList } from '#/api/iot/device/device';
import { IoTDataSpecsDataTypeEnum } from '#/views/iot/utils/constants';

/** IoT 设备属性历史数据详情 */
defineOptions({ name: 'DeviceDetailsThingModelPropertyHistory' });

defineProps<{ deviceId: number }>();

const dialogVisible = ref(false); // 弹窗的是否展示
const loading = ref(false);
const exporting = ref(false);
const viewMode = ref<'chart' | 'list'>('chart'); // 视图模式状态
const list = ref<IotDeviceApi.DevicePropertyDetail[]>([]); // 列表的数据
const total = ref(0); // 总数据量
const thingModelDataType = ref<string>(''); // 物模型数据类型
const propertyIdentifier = ref<string>(''); // 属性标识符
const dateRange = ref<[Dayjs, Dayjs]>([
  dayjs().subtract(7, 'day').startOf('day'),
  dayjs().endOf('day'),
]);

const queryParams = reactive({
  deviceId: -1,
  identifier: '',
  times: [
    formatDateTime(beginOfDay(new Date(Date.now() - 3600 * 1000 * 24 * 7))),
    formatDateTime(endOfDay(new Date())),
  ],
});

// Echarts 相关
const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);

// 判断是否为复杂数据类型（struct 或 array）
const isComplexDataType = computed(() => {
  if (!thingModelDataType.value) return false;
  return [
    IoTDataSpecsDataTypeEnum.ARRAY,
    IoTDataSpecsDataTypeEnum.STRUCT,
  ].includes(thingModelDataType.value as any);
});

// 统计数据
const maxValue = computed(() => {
  if (isComplexDataType.value || list.value.length === 0) return '-';
  const values = list.value
    .map((item) => Number(item.value))
    .filter((v) => !Number.isNaN(v));
  return values.length > 0 ? Math.max(...values).toFixed(2) : '-';
});

const minValue = computed(() => {
  if (isComplexDataType.value || list.value.length === 0) return '-';
  const values = list.value
    .map((item) => Number(item.value))
    .filter((v) => !Number.isNaN(v));
  return values.length > 0 ? Math.min(...values).toFixed(2) : '-';
});

const avgValue = computed(() => {
  if (isComplexDataType.value || list.value.length === 0) return '-';
  const values = list.value
    .map((item) => Number(item.value))
    .filter((v) => !Number.isNaN(v));
  if (values.length === 0) return '-';
  const sum = values.reduce((acc, val) => acc + val, 0);
  return (sum / values.length).toFixed(2);
});

// 表格列配置
const tableColumns = computed(() => [
  {
    title: '序号',
    key: 'index',
    width: 80,
    align: 'center' as const,
    customRender: ({ index }: { index: number }) => index + 1,
  },
  {
    title: '时间',
    key: 'updateTime',
    dataIndex: 'updateTime',
    width: 200,
    align: 'center' as const,
  },
  {
    title: '属性值',
    key: 'value',
    dataIndex: 'value',
    align: 'center' as const,
  },
]);

// 分页配置
const paginationConfig = computed(() => ({
  current: 1,
  pageSize: 10,
  total: total.value,
  showSizeChanger: true,
  showQuickJumper: true,
  pageSizeOptions: ['10', '20', '50', '100'],
  showTotal: (total: number) => `共 ${total} 条数据`,
}));

/** 获得设备历史数据 */
async function getList() {
  loading.value = true;
  try {
    const data = await getHistoryDevicePropertyList(queryParams);
    // 后端直接返回数组，不是 { list: [] } 格式
    list.value = (
      Array.isArray(data) ? data : data?.list || []
    ) as IotDeviceApi.DevicePropertyDetail[];
    total.value = list.value.length;

    // 如果是图表模式且不是复杂数据类型，渲染图表
    if (
      viewMode.value === 'chart' &&
      !isComplexDataType.value &&
      list.value.length > 0
    ) {
      // 等待 DOM 更新完成后再渲染图表
      await nextTick();
      await nextTick(); // 双重 nextTick 确保 DOM 完全准备好
      renderChart();
    }
  } catch {
    message.error('获取数据失败');
    list.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

/** 渲染图表 */
function renderChart() {
  if (!list.value || list.value.length === 0) {
    return;
  }

  const chartData = list.value.map((item) => [item.updateTime, item.value]);

  // 使用 setTimeout 延迟渲染，避免 ECharts 主进程冲突
  setTimeout(() => {
    // 检查 chartRef 是否存在且已挂载
    if (!chartRef.value || !chartRef.value.$el) {
      return;
    }

    renderEcharts({
      title: {
        text: '属性值趋势',
        left: 'center',
        textStyle: {
          fontSize: 16,
          fontWeight: 'normal',
        },
      },
      grid: {
        left: 60,
        right: 60,
        bottom: 100,
        top: 80,
        containLabel: true,
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
        },
        formatter: (params: any) => {
          const param = params[0];
          return `
          <div style="padding: 8px;">
            <div style="margin-bottom: 4px; font-weight: bold;">
              ${formatDate(new Date(param.value[0]), 'YYYY-MM-DD HH:mm:ss')}
            </div>
            <div>
              <span style="display:inline-block;margin-right:5px;border-radius:50%;width:10px;height:10px;background-color:${param.color};"></span>
              <span>属性值: <strong>${param.value[1]}</strong></span>
            </div>
          </div>
        `;
        },
      },
      xAxis: {
        type: 'time',
        name: '时间',
        nameTextStyle: {
          padding: [10, 0, 0, 0],
        },
        axisLabel: {
          formatter: (value: number) => {
            return String(formatDate(new Date(value), 'MM-DD HH:mm') || '');
          },
        },
      },
      yAxis: {
        type: 'value',
        name: '属性值',
        nameTextStyle: {
          padding: [0, 0, 10, 0],
        },
      },
      series: [
        {
          name: '属性值',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: {
            width: 2,
            color: '#1890FF',
          },
          itemStyle: {
            color: '#1890FF',
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                {
                  offset: 0,
                  color: 'rgba(24, 144, 255, 0.3)',
                },
                {
                  offset: 1,
                  color: 'rgba(24, 144, 255, 0.05)',
                },
              ],
            },
          },
          data: chartData,
        },
      ],
      dataZoom: [
        {
          type: 'inside',
          start: 0,
          end: 100,
        },
        {
          type: 'slider',
          height: 30,
          bottom: 20,
        },
      ],
    });
  }, 300); // 延迟300ms渲染，确保 DOM 完全准备好
}

/** 打开弹窗 */
async function open(deviceId: number, identifier: string, dataType: string) {
  dialogVisible.value = true;
  queryParams.deviceId = deviceId;
  queryParams.identifier = identifier;
  propertyIdentifier.value = identifier;
  thingModelDataType.value = dataType;

  // 重置时间范围为最近7天
  dateRange.value = [
    dayjs().subtract(7, 'day').startOf('day'),
    dayjs().endOf('day'),
  ];

  // 更新查询参数的时间
  queryParams.times = [
    formatDateTime(dateRange.value[0].toDate()),
    formatDateTime(dateRange.value[1].toDate()),
  ];

  // 如果物模型是 struct、array，需要默认使用 list 模式
  viewMode.value = isComplexDataType.value ? 'list' : 'chart';

  // 等待弹窗完全渲染后再获取数据
  await nextTick();
  await getList();

  // 如果是图表模式，延迟渲染图表
  if (viewMode.value === 'chart' && !isComplexDataType.value) {
    setTimeout(() => {
      renderChart();
    }, 500);
  }
}

/** 时间变化处理 */
function handleTimeChange() {
  if (!dateRange.value || dateRange.value.length !== 2) {
    return;
  }

  queryParams.times = [
    formatDateTime(dateRange.value[0].toDate()),
    formatDateTime(dateRange.value[1].toDate()),
  ];

  getList();
}

/** 刷新数据 */
function handleRefresh() {
  getList();
}

/** 导出数据 */
async function handleExport() {
  if (list.value.length === 0) {
    message.warning('暂无数据可导出');
    return;
  }

  exporting.value = true;
  try {
    // 构建CSV内容
    const headers = ['序号', '时间', '属性值'];
    const csvContent = [
      headers.join(','),
      ...list.value.map((item, index) => {
        return [
          index + 1,
          formatDateTime(new Date(item.updateTime)),
          isComplexDataType.value
            ? `"${JSON.stringify(item.value)}"`
            : item.value,
        ].join(',');
      }),
    ].join('\n');

    // 创建 BOM 头,解决中文乱码
    const BOM = '\uFEFF';
    const blob = new Blob([BOM + csvContent], {
      type: 'text/csv;charset=utf-8',
    });

    // 下载文件
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `设备属性历史_${propertyIdentifier.value}_${formatDate(new Date(), 'YYYYMMDDHHmmss')}.csv`;
    document.body.append(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);

    message.success('导出成功');
  } catch {
    message.error('导出失败');
  } finally {
    exporting.value = false;
  }
}

/** 关闭弹窗 */
function handleClose() {
  dialogVisible.value = false;
  list.value = [];
  total.value = 0;
}

/** 格式化复杂数据类型 */
function formatComplexValue(value: any) {
  if (typeof value === 'object') {
    return JSON.stringify(value);
  }
  return String(value);
}

/** 监听视图模式变化，重新渲染图表 */
watch(viewMode, async (newMode) => {
  if (
    newMode === 'chart' &&
    !isComplexDataType.value &&
    list.value.length > 0
  ) {
    // 等待 DOM 显示完成
    await nextTick();
    await nextTick();

    // 延迟渲染图表
    setTimeout(() => {
      renderChart();
    }, 300);
  }
});

defineExpose({ open }); // 提供 open 方法，用于打开弹窗
</script>
<template>
  <Modal
    v-model:open="dialogVisible"
    title="查看数据"
    width="1200px"
    :destroy-on-close="true"
    @cancel="handleClose"
  >
    <div class="property-history-container">
      <!-- 工具栏 -->
      <div class="toolbar-wrapper mb-4">
        <Space :size="12" class="w-full" wrap>
          <!-- 时间选择 -->
          <RangePicker
            v-model:value="dateRange"
            :show-time="{ format: 'HH:mm:ss' }"
            format="YYYY-MM-DD HH:mm:ss"
            :placeholder="['开始时间', '结束时间']"
            class="!w-[400px]"
            @change="handleTimeChange"
          />

          <!-- 刷新按钮 -->
          <Button @click="handleRefresh" :loading="loading">
            <template #icon>
              <IconifyIcon icon="ant-design:reload-outlined" />
            </template>
            刷新
          </Button>

          <!-- 导出按钮 -->
          <Button
            @click="handleExport"
            :loading="exporting"
            :disabled="list.length === 0"
          >
            <template #icon>
              <IconifyIcon icon="ant-design:export-outlined" />
            </template>
            导出
          </Button>

          <!-- 视图切换 -->
          <Button.Group class="ml-auto">
            <Button
              :type="viewMode === 'chart' ? 'primary' : 'default'"
              @click="viewMode = 'chart'"
              :disabled="isComplexDataType"
            >
              <template #icon>
                <IconifyIcon icon="ant-design:line-chart-outlined" />
              </template>
              图表
            </Button>
            <Button
              :type="viewMode === 'list' ? 'primary' : 'default'"
              @click="viewMode = 'list'"
            >
              <template #icon>
                <IconifyIcon icon="ant-design:table-outlined" />
              </template>
              列表
            </Button>
          </Button.Group>
        </Space>

        <!-- 数据统计信息 -->
        <div v-if="list.length > 0" class="mt-3 text-sm text-gray-600">
          <Space :size="16">
            <span>共 {{ total }} 条数据</span>
            <span v-if="viewMode === 'chart' && !isComplexDataType">
              最大值: {{ maxValue }} | 最小值: {{ minValue }} | 平均值:
              {{ avgValue }}
            </span>
          </Space>
        </div>
      </div>

      <!-- 数据展示区域 -->
      <Spin :spinning="loading" :delay="200">
        <!-- 图表模式 -->
        <div v-show="viewMode === 'chart'" class="chart-container">
          <Empty
            v-if="list.length === 0"
            :image="Empty.PRESENTED_IMAGE_SIMPLE"
            description="暂无数据"
            class="py-20"
          />
          <div v-else>
            <EchartsUI ref="chartRef" height="500px" />
          </div>
        </div>

        <!-- 表格模式 -->
        <div v-show="viewMode === 'list'" class="table-container">
          <Table
            :data-source="list"
            :columns="tableColumns"
            :pagination="paginationConfig"
            :scroll="{ y: 500 }"
            row-key="updateTime"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'updateTime'">
                {{ formatDate(new Date(record.updateTime)) }}
              </template>
              <template v-else-if="column.key === 'value'">
                <Tag v-if="isComplexDataType" color="processing">
                  {{ formatComplexValue(record.value) }}
                </Tag>
                <span v-else class="font-medium">{{ record.value }}</span>
              </template>
            </template>
          </Table>
        </div>
      </Spin>
    </div>

    <template #footer>
      <Button @click="handleClose">关闭</Button>
    </template>
  </Modal>
</template>

<style scoped lang="scss">
.property-history-container {
  max-height: 70vh;
  overflow: auto;

  .toolbar-wrapper {
    padding: 16px;
    background-color: hsl(var(--card) / 90%);
    border: 1px solid hsl(var(--border) / 60%);
    border-radius: 8px;
  }

  .chart-container,
  .table-container {
    padding: 16px;
    background-color: hsl(var(--card));
    border: 1px solid hsl(var(--border) / 60%);
    border-radius: 8px;
  }
}
</style>
