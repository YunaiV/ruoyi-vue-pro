<script lang="ts" setup>
import type { EchartsUIType } from '@vben/plugins/echarts';

import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmStatisticsCustomerApi } from '#/api/crm/statistics/customer';

import { onMounted, ref } from 'vue';

import { ContentWrap, Page } from '@vben/common-ui';
import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import { Tabs } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getDatas } from '#/api/crm/statistics/customer';
import { $t } from '#/locales';

import { getChartOptions } from './chartOptions';
import { customerSummaryTabs, useGridColumns, useGridFormSchema } from './data';

const activeTabName = ref('contractPriceRank');
const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);

const [QueryForm, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  schema: useGridFormSchema(),
  showCollapseButton: true,
  submitButtonOptions: {
    content: $t('common.query'),
  },
  wrapperClass: 'grid-cols-1 md:grid-cols-2',
  handleSubmit: async () => {
    await handleTabChange(activeTabName.value);
  },
});

const [Grid, gridApi] = useVbenVxeGrid({
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
          await renderEcharts(getChartOptions(activeTabName.value, res));
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
  } as VxeTableGridOptions<CrmStatisticsCustomerApi.CustomerSummaryByUserRespVO>,
});

/** tab 切换 */
async function handleTabChange(key: any) {
  activeTabName.value = key;
  gridApi.setGridOptions({
    columns: useGridColumns(key),
  });
  const queryParams = await formApi.getValues();
  const res = await getDatas(activeTabName.value, queryParams);
  await renderEcharts(getChartOptions(activeTabName.value, res));
  await gridApi.grid.reloadData(res);
}

onMounted(() => {
  handleTabChange(activeTabName.value);
});
</script>

<template>
  <Page auto-content-height>
    <ContentWrap>
      <QueryForm />
      <Tabs
        v-model:active-key="activeTabName"
        class="w-full"
        @change="handleTabChange"
      >
        <Tabs.TabPane
          v-for="item in customerSummaryTabs"
          :key="item.key"
          :tab="item.tab"
          :force-render="true"
        />
      </Tabs>
      <EchartsUI class="mb-20 h-full w-full" ref="chartRef" />
      <Grid />
    </ContentWrap>
  </Page>
</template>
