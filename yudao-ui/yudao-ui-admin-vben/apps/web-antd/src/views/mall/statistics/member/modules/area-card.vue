<script lang="ts" setup>
import type { EchartsUIType } from '@vben/plugins/echarts';

import type { MallMemberStatisticsApi } from '#/api/mall/statistics/member';

import { onMounted, ref, shallowRef } from 'vue';

import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import { Card, Spin } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getMemberAreaStatisticsList } from '#/api/mall/statistics/member';

import { getAreaChartOptions, getAreaTableColumns } from './area-chart-options';

/** 会员地域分布卡片 */
defineOptions({ name: 'MemberAreaCard' });

const loading = ref(true);
const areaStatisticsList = shallowRef<
  MallMemberStatisticsApi.AreaStatisticsRespVO[]
>([]);
const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: getAreaTableColumns(),
    height: 300,
    border: true,
    showOverflow: true,
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    pagerConfig: {
      enabled: false,
    },
    toolbarConfig: {
      enabled: false,
    },
  },
});

/** 按照省份，查询会员统计列表 */
async function loadMemberAreaStatisticsList() {
  loading.value = true;
  try {
    const list = await getMemberAreaStatisticsList();
    areaStatisticsList.value = list.map(
      (item: MallMemberStatisticsApi.AreaStatisticsRespVO) => ({
        ...item,
        areaName: areaReplace(item.areaName),
      }),
    );
    // 渲染图表
    const chartOptions = getAreaChartOptions(areaStatisticsList.value);
    await renderEcharts(chartOptions);

    // 加载表格数据
    await gridApi.grid.loadData(areaStatisticsList.value);
  } finally {
    loading.value = false;
  }
}

/** 城市名兼容：ECharts 地名存在差异 */
function areaReplace(areaName: string): string {
  if (!areaName) {
    return areaName;
  }
  return areaName
    .replace('维吾尔自治区', '')
    .replace('壮族自治区', '')
    .replace('回族自治区', '')
    .replace('自治区', '')
    .replace('省', '');
}

/** 初始化 */
onMounted(() => {
  loadMemberAreaStatisticsList();
});
</script>

<template>
  <Card :bordered="false" title="会员地域分布" class="h-full">
    <Spin :spinning="loading">
      <div class="flex gap-4">
        <div class="w-2/5">
          <EchartsUI ref="chartRef" />
        </div>
        <div class="w-3/5">
          <Grid />
        </div>
      </div>
    </Spin>
  </Card>
</template>
