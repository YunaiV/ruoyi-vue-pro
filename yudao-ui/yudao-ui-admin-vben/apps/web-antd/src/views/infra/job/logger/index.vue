<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { InfraJobLogApi } from '#/api/infra/job-log';

import { useRoute } from 'vue-router';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { exportJobLog, getJobLogPage } from '#/api/infra/job-log';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Detail from './modules/detail.vue';

const { query } = useRoute();

const [DetailModal, detailModalApi] = useVbenModal({
  connectedComponent: Detail,
  destroyOnClose: true,
});

/** 导出表格 */
async function handleExport() {
  const data = await exportJobLog(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '任务日志.xls', source: data });
}

/** 查看日志详情 */
function handleDetail(row: InfraJobLogApi.JobLog) {
  detailModalApi.setData({ id: row.id }).open();
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getJobLogPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
            jobId: query.id,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<InfraJobLogApi.JobLog>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="定时任务" url="https://doc.iocoder.cn/job/" />
      <DocAlert title="异步任务" url="https://doc.iocoder.cn/async-task/" />
      <DocAlert title="消息队列" url="https://doc.iocoder.cn/message-queue/" />
    </template>

    <DetailModal />
    <Grid table-title="任务日志列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['infra:job:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.detail'),
              type: 'link',
              icon: ACTION_ICON.VIEW,
              auth: ['infra:job:query'],
              onClick: handleDetail.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
