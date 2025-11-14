<script lang="ts" setup>
import type { EchartsUIType } from '@vben/plugins/echarts';

import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmStatisticsCustomerApi } from '#/api/crm/statistics/customer';

import { ref } from 'vue';

import { Page } from '@vben/common-ui';
import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import { Tabs } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getDatas } from '#/api/crm/statistics/portrait';

import { getChartOptions } from './chartOptions';
import { customerSummaryTabs, useGridColumns, useGridFormSchema } from './data';

const activeTabName = ref('area');
const leftChartRef = ref<EchartsUIType>();
const rightChartRef = ref<EchartsUIType>();
const { renderEcharts: renderLeftEcharts } = useEcharts(leftChartRef);
const { renderEcharts: renderRightEcharts } = useEcharts(rightChartRef);

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
          const res = await getDatas(activeTabName.value, formValues);
          await renderLeftEcharts(getChartOptions(activeTabName.value, res).left);
          await renderRightEcharts(getChartOptions(activeTabName.value, res).right);
          return res;
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
        <div class="mt-5 flex">
          <EchartsUI class="m-4 w-1/2" ref="leftChartRef" />
          <EchartsUI class="m-4 w-1/2" ref="rightChartRef" />
        </div>
      </template>
    </Grid>
  </Page>
</template>
