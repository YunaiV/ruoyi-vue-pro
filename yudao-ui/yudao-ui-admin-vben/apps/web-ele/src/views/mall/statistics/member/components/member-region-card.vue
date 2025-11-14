<script lang="ts" setup>
import type { EchartsUIType } from '@vben/plugins/echarts';

import type { MallMemberStatisticsApi } from '#/api/mall/statistics/member';

import { onMounted, reactive, ref } from 'vue';

import { AnalysisChartCard } from '@vben/common-ui';
import { EchartsUI, useEcharts } from '@vben/plugins/echarts';
import { fenToYuan, fenToYuanFormat } from '@vben/utils';

import { ElRow } from 'element-plus';

import * as MemberStatisticsApi from '#/api/mall/statistics/member';

const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);
const areaStatisticsList = ref<MallMemberStatisticsApi.AreaStatistics[]>([]); // 省份会员统计
const areaChartOptions = reactive({
  tooltip: {
    trigger: 'item' as const,
    formatter: (params: any) => {
      return `${params?.data?.areaName || params?.name}<br/>
会员数量：${params?.data?.userCount || 0}<br/>
订单创建数量：${params?.data?.orderCreateUserCount || 0}<br/>
订单支付数量：${params?.data?.orderPayUserCount || 0}<br/>
订单支付金额：${fenToYuan(params?.data?.orderPayPrice || 0)}`;
    },
  },
  visualMap: {
    text: ['高', '低'],
    realtime: false,
    calculable: true,
    top: 'middle',
    inRange: {
      color: ['#fff', '#3b82f6'],
    },
    min: 0,
    max: 0,
  },
  series: [
    {
      name: '会员地域分布',
      type: 'map' as const,
      map: 'china',
      roam: false,
      selectedMode: false,
      data: [] as any[],
    },
  ],
});

/** 按照省份，查询会员统计列表 */
const getMemberAreaStatisticsList = async () => {
  areaStatisticsList.value =
    await MemberStatisticsApi.getMemberAreaStatisticsList();
  let min = 0;
  let max = 0;

  const mapData = areaStatisticsList.value.map((item) => {
    const payUserCount = item?.orderPayUserCount || 0;
    min = Math.min(min, payUserCount);
    max = Math.max(max, payUserCount);
    return { ...item, name: item.areaName, value: payUserCount };
  });

  // 使用类型断言处理赋值
  (areaChartOptions.series[0] as any).data = mapData;
  areaChartOptions.visualMap.min = min;
  areaChartOptions.visualMap.max = max;
};

onMounted(async () => {
  await getMemberAreaStatisticsList();
  renderEcharts(areaChartOptions);
});
</script>

<template>
  <AnalysisChartCard title="会员地域分布">
    <ElRow>
      <ElCol :span="12">
        <EchartsUI ref="chartRef" />
      </ElCol>
      <ElCol :span="12">
        <el-table :data="areaStatisticsList" :height="300">
          <el-table-column
            :sort-method="
              (obj1: any, obj2: any) =>
                obj1.areaName.localeCompare(obj2.areaName, 'zh-CN')
            "
            align="center"
            label="省份"
            min-width="80"
            prop="areaName"
            show-overflow-tooltip
            sortable
          />
          <el-table-column
            align="center"
            label="会员数量"
            min-width="105"
            prop="userCount"
            sortable
          />
          <el-table-column
            align="center"
            label="订单创建数量"
            min-width="135"
            prop="orderCreateUserCount"
            sortable
          />
          <el-table-column
            align="center"
            label="订单支付数量"
            min-width="135"
            prop="orderPayUserCount"
            sortable
          />
          <el-table-column
            :formatter="fenToYuanFormat"
            align="center"
            label="订单支付金额"
            min-width="135"
            prop="orderPayPrice"
            sortable
          />
        </el-table>
      </ElCol>
    </ElRow>
  </AnalysisChartCard>
</template>
