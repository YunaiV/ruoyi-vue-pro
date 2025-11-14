<script lang="ts" setup>
import type { EchartsUIType } from '@vben/plugins/echarts';

import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmStatisticsFunnelApi } from '#/api/crm/statistics/funnel';

import { ref } from 'vue';

import { Page } from '@vben/common-ui';
import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import { Button, ButtonGroup, Tabs } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getChartDatas, getDatas } from '#/api/crm/statistics/funnel';

import { getChartOptions } from './chartOptions';
import { customerSummaryTabs, useGridColumns, useGridFormSchema } from './data';

const activeTabName = ref('funnel');
const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);

const active = ref(true);

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
        query: async ({ page }, formValues) => {
          const res = await getChartDatas(activeTabName.value, formValues);
          await renderEcharts(
            getChartOptions(activeTabName.value, active.value, res),
          );
          return getDatas(activeTabName.value, {
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
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
  } as VxeTableGridOptions<CrmStatisticsFunnelApi.BusinessSummaryByDate>,
});

/** tab 切换 */
async function handleTabChange(key: any) {
  activeTabName.value = key;
  gridApi.setGridOptions({
    columns: useGridColumns(key),
    pagerConfig: {
      enabled: activeTabName.value !== 'funnelRef',
    },
  });
  await gridApi.reload();
}

/** 视角切换 */
function handleActive(value: boolean) {
  active.value = value;
  renderEcharts(
    getChartOptions(
      activeTabName.value,
      active.value,
      gridApi.formApi.getValues(),
    ),
  );
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
        <ButtonGroup>
          <Button
            :type="active ? 'primary' : 'default'"
            v-if="activeTabName === 'funnel'"
            @click="handleActive(true)"
          >
            客户视角
          </Button>
          <Button
            :type="active ? 'default' : 'primary'"
            v-if="activeTabName === 'funnel'"
            @click="handleActive(false)"
          >
            动态视角
          </Button>
        </ButtonGroup>
        <EchartsUI class="mb-20 h-2/5 w-full" ref="chartRef" />
      </template>
    </Grid>
  </Page>
</template>
