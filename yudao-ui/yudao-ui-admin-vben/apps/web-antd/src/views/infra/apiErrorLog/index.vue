<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { InfraApiErrorLogApi } from '#/api/infra/api-error-log';

import { confirm, DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { InfraApiErrorLogProcessStatusEnum } from '@vben/constants';
import { downloadFileFromBlobPart } from '@vben/utils';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  exportApiErrorLog,
  getApiErrorLogPage,
  updateApiErrorLogStatus,
} from '#/api/infra/api-error-log';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Detail from './modules/detail.vue';

const [DetailModal, detailModalApi] = useVbenModal({
  connectedComponent: Detail,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 导出表格 */
async function handleExport() {
  const data = await exportApiErrorLog(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: 'API 错误日志.xls', source: data });
}

/** 查看 API 错误日志详情 */
function handleDetail(row: InfraApiErrorLogApi.ApiErrorLog) {
  detailModalApi.setData(row).open();
}

/** 处理已处理 / 已忽略的操作 */
async function handleProcess(id: number, processStatus: number) {
  await confirm({
    content: `确认标记为${InfraApiErrorLogProcessStatusEnum.DONE ? '已处理' : '已忽略'}?`,
  });
  const hideLoading = message.loading({
    content: '正在处理中...',
    duration: 0,
  });
  try {
    await updateApiErrorLogStatus(id, processStatus);
    message.success($t('ui.actionMessage.operationSuccess'));
    handleRefresh();
  } finally {
    hideLoading();
  }
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
          return await getApiErrorLogPage({
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
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<InfraApiErrorLogApi.ApiErrorLog>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="系统日志" url="https://doc.iocoder.cn/system-log/" />
    </template>

    <DetailModal @success="handleRefresh" />
    <Grid table-title="API 错误日志列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['infra:api-error-log:export'],
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
              auth: ['infra:api-error-log:query'],
              onClick: handleDetail.bind(null, row),
            },
            {
              label: '已处理',
              type: 'link',
              icon: ACTION_ICON.ADD,
              auth: ['infra:api-error-log:update-status'],
              ifShow:
                row.processStatus === InfraApiErrorLogProcessStatusEnum.INIT,
              onClick: handleProcess.bind(
                null,
                row.id,
                InfraApiErrorLogProcessStatusEnum.DONE,
              ),
            },
            {
              label: '已忽略',
              type: 'link',
              icon: ACTION_ICON.DELETE,
              auth: ['infra:api-error-log:update-status'],
              ifShow:
                row.processStatus === InfraApiErrorLogProcessStatusEnum.INIT,
              onClick: handleProcess.bind(
                null,
                row.id,
                InfraApiErrorLogProcessStatusEnum.IGNORE,
              ),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
