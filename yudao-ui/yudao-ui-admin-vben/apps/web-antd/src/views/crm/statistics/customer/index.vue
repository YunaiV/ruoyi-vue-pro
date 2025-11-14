<script lang="ts" setup>
import type { EchartsUIType } from '@vben/plugins/echarts';

import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmStatisticsCustomerApi } from '#/api/crm/statistics/customer';

import { ref } from 'vue';

import { Page } from '@vben/common-ui';
import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import { Tabs } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getChartDatas, getDatas } from '#/api/crm/statistics/customer';

import { getChartOptions } from './chartOptions';
import { customerSummaryTabs, useGridColumns, useGridFormSchema } from './data';

const activeTabName = ref('customerSummary');
const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(activeTabName.value),
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: false,
    },
    proxyConfig: {
      ajax: {
        query: async (_, formValues) => {
          const res = await getChartDatas(activeTabName.value, formValues);
          await renderEcharts(getChartOptions(activeTabName.value, res));
          return getDatas(activeTabName.value, formValues);
        },
      },
    },
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
  gridApi.setGridOptions({
    columns: useGridColumns(key),
  });
  await gridApi.reload();
}
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
