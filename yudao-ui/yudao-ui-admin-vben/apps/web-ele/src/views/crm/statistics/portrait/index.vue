<script lang="ts" setup>
import type { EchartsUIType } from '@vben/plugins/echarts';

import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmStatisticsCustomerApi } from '#/api/crm/statistics/customer';

import { onMounted, ref } from 'vue';

import { ContentWrap, Page } from '@vben/common-ui';
import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import { ElTabPane, ElTabs } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getDatas } from '#/api/crm/statistics/portrait';
import { $t } from '#/locales';

import { getChartOptions } from './chartOptions';
import { customerSummaryTabs, useGridColumns, useGridFormSchema } from './data';

const activeTabName = ref('area');
const leftChartRef = ref<EchartsUIType>();
const rightChartRef = ref<EchartsUIType>();
const { renderEcharts: renderLeftEcharts } = useEcharts(leftChartRef);
const { renderEcharts: renderRightEcharts } = useEcharts(rightChartRef);

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
  await renderLeftEcharts(getChartOptions(activeTabName.value, res).left);
  await renderRightEcharts(getChartOptions(activeTabName.value, res).right);
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
      <ElTabs
        v-model="activeTabName"
        class="w-full"
        @tab-change="handleTabChange"
      >
        <ElTabPane
          v-for="item in customerSummaryTabs"
          :key="item.key"
          :label="item.tab"
          :name="item.key"
        />
      </ElTabs>
      <div class="mt-5 flex">
        <EchartsUI class="m-4 w-1/2" ref="leftChartRef" />
        <EchartsUI class="m-4 w-1/2" ref="rightChartRef" />
      </div>
      <Grid v-show="activeTabName !== 'area'" />
    </ContentWrap>
  </Page>
</template>
