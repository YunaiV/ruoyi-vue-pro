<script lang="ts" setup>
import type { EchartsUIType } from '@vben/plugins/echarts';

import type { MallMemberStatisticsApi } from '#/api/mall/statistics/member';

import { onMounted, ref } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import { Card, Spin } from 'ant-design-vue';

import { getMemberSexStatisticsList } from '#/api/mall/statistics/member';

import { getSexChartOptions } from './sex-chart-options';

/** 会员性别比例卡片 */
defineOptions({ name: 'MemberSexCard' });

const loading = ref(true);
const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);

/** 按照性别，查询会员统计列表 */
async function loadMemberSexStatisticsList() {
  loading.value = true;
  try {
    const list = await getMemberSexStatisticsList();
    const dictDataList = getDictOptions(DICT_TYPE.SYSTEM_USER_SEX, 'number');
    dictDataList.push({ label: '未知', value: null } as any);
    const chartData = dictDataList.map((dictData: any) => {
      const userCount = list.find(
        (item: MallMemberStatisticsApi.SexStatisticsRespVO) =>
          item.sex === dictData.value,
      )?.userCount;
      return {
        name: dictData.label,
        value: userCount || 0,
      };
    });

    // 渲染图表
    await renderEcharts(getSexChartOptions(chartData));
  } finally {
    loading.value = false;
  }
}

/** 初始化 */
onMounted(() => {
  loadMemberSexStatisticsList();
});
</script>

<template>
  <Card :bordered="false" title="会员性别比例" class="h-full">
    <Spin :spinning="loading">
      <EchartsUI ref="chartRef" />
    </Spin>
  </Card>
</template>
