<script lang="ts" setup>
import type { EchartsUIType } from '@vben/plugins/echarts';

import type {
  VxeGridListeners,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { CrmStatisticsFunnelApi } from '#/api/crm/statistics/funnel';

import { reactive, ref } from 'vue';

import { ContentWrap, Page } from '@vben/common-ui';
import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import { ElButton, ElButtonGroup, ElTabPane, ElTabs } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getChartDatas, getDatas } from '#/api/crm/statistics/funnel';
import { $t } from '#/locales';

import { getChartOptions } from './chartOptions';
import { customerSummaryTabs, useGridColumns, useGridFormSchema } from './data';

const activeTabName = ref('funnel');
const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);

const active = ref(true);
const pagerVO = reactive({
  total: 0,
  pageNo: 1,
  pageSize: 10,
});

const gridEvents: VxeGridListeners = {
  async pageChange({ pageSize, currentPage }) {
    pagerVO.pageNo = currentPage;
    pagerVO.pageSize = pageSize;
    await handleTabChange(activeTabName.value);
  },
};
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
  } as VxeTableGridOptions<CrmStatisticsFunnelApi.BusinessSummaryByDateRespVO>,
});

/** tab 切换 */
async function handleTabChange(key: any) {
  activeTabName.value = key;
  gridApi.setGridOptions({
    columns: useGridColumns(key),
    height: '400px',
    keepSource: true,
    pagerConfig: {
      enabled: activeTabName.value !== 'funnel',
    },
  });
  const queryParams = await formApi.getValues();
  const res = await getChartDatas(activeTabName.value, queryParams);
  await renderEcharts(getChartOptions(activeTabName.value, active.value, res));
  const data: any = await getDatas(activeTabName.value, queryParams);
  await gridApi.grid.reloadData(
    activeTabName.value === 'funnel' ? data : data.list,
  );
}

/** 视角切换 */
async function handleActive(value: boolean) {
  active.value = value;
  const queryParams = await formApi.getValues();
  renderEcharts(
    getChartOptions(activeTabName.value, active.value, queryParams),
  );
}
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
      <ElButtonGroup>
        <ElButton
          :type="active ? 'primary' : 'default'"
          v-if="activeTabName === 'funnel'"
          @click="handleActive(true)"
        >
          客户视角
        </ElButton>
        <ElButton
          :type="active ? 'default' : 'primary'"
          v-if="activeTabName === 'funnel'"
          @click="handleActive(false)"
        >
          动态视角
        </ElButton>
      </ElButtonGroup>
      <EchartsUI class="mb-20 h-2/5 w-full" ref="chartRef" />
      <Grid v-on="gridEvents" />
    </ContentWrap>
  </Page>
</template>
