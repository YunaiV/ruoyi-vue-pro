<script lang="ts" setup>
import type { EchartsUIType } from '@vben/plugins/echarts';

import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmStatisticsCustomerApi } from '#/api/crm/statistics/customer';

import { onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import { Tabs } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  getContractCountPerformance,
  getContractPricePerformance,
  getReceivablePricePerformance,
} from '#/api/crm/statistics/performance';

import { getChartOptions } from './chartOptions';
import { customerSummaryTabs, useGridFormSchema } from './data';

const activeTabName = ref('ContractCountPerformance');
const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
    handleSubmit: async () => {
      await handleTabChange(activeTabName.value);
    },
  },
  gridOptions: {
    columns: [],
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: false,
    },
    proxyConfig: {
      enabled: false,
    },
    data: [],
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      enabled: false,
    },
  } as VxeTableGridOptions<CrmStatisticsCustomerApi.CustomerSummaryByUser>,
});

/** tab 切换 */
async function handleTabChange(key: any) {
  activeTabName.value = key;
  const params = (await gridApi.formApi.getValues()) as any;
  let data: any[] = [];
  const columnsData: any[] = [];
  let tableData: any[] = [];
  switch (key) {
    case 'ContractCountPerformance': {
      tableData = [
        { title: '当月合同数量统计（个）' },
        { title: '上月合同数量统计（个）' },
        { title: '去年当月合同数量统计（个）' },
        { title: '环比增长率（%）' },
        { title: '同比增长率（%）' },
      ];
      data = await getContractCountPerformance(params);
      break;
    }
    case 'ContractPricePerformance': {
      tableData = [
        { title: '当月合同金额统计（元）' },
        { title: '上月合同金额统计（元）' },
        { title: '去年当月合同金额统计（元）' },
        { title: '环比增长率（%）' },
        { title: '同比增长率（%）' },
      ];
      data = await getContractPricePerformance(params);
      break;
    }
    case 'ReceivablePricePerformance': {
      tableData = [
        { title: '当月回款金额统计（元）' },
        { title: '上月回款金额统计（元）' },
        { title: '去年当月回款金额统计（元）' },
        { title: '环比增长率（%）' },
        { title: '同比增长率（%）' },
      ];
      data = await getReceivablePricePerformance(params);
      break;
    }
    default: {
      break;
    }
  }
  const columnObj = {
    title: '日期',
    field: 'title',
    minWidth: 200,
    align: 'left',
  };
  columnsData.splice(0); // 清空数组
  columnsData.push(columnObj);
  data.forEach((item: any, index: number) => {
    const columnObj = { title: item.time, field: `field${index}` };
    columnsData.push(columnObj);
    tableData[0][`field${index}`] = item.currentMonthCount;
    tableData[1][`field${index}`] = item.lastMonthCount;
    tableData[2][`field${index}`] = item.lastYearCount;
    tableData[3][`field${index}`] =
      item.lastMonthCount === 0
        ? 'NULL'
        : (
            ((item.currentMonthCount - item.lastMonthCount) /
              item.lastMonthCount) *
            100
          ).toFixed(2);
    tableData[4][`field${index}`] =
      item.lastYearCount === 0
        ? 'NULL'
        : (
            ((item.currentMonthCount - item.lastYearCount) /
              item.lastYearCount) *
            100
          ).toFixed(2);
  });
  await renderEcharts(getChartOptions(key, data), true);
  await gridApi.grid.reloadColumn(columnsData);
  await gridApi.grid.reloadData(tableData);
}

/** 初始化加载 */
onMounted(() => {
  handleTabChange(activeTabName.value);
});
</script>

<template>
  <Page auto-content-height>
    <Grid>
      <template #top>
        <Tabs v-model:active-key="activeTabName" @change="handleTabChange">
          <Tabs.TabPane
            v-for="item in customerSummaryTabs"
            :key="item.key"
            :tab="item.tab"
            :force-render="true"
          />
        </Tabs>
        <EchartsUI class="mb-20 h-full w-full" ref="chartRef" />
      </template>
    </Grid>
  </Page>
</template>
