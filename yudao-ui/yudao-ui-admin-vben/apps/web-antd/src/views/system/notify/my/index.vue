<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemNotifyMessageApi } from '#/api/system/notify/message';

import { ref } from 'vue';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { isEmpty } from '@vben/utils';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  getMyNotifyMessagePage,
  updateAllNotifyMessageRead,
  updateNotifyMessageRead,
} from '#/api/system/notify/message';

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

/** 查看站内信详情 */
function handleDetail(row: SystemNotifyMessageApi.NotifyMessage) {
  detailModalApi.setData(row).open();
}

/** 标记一条站内信已读 */
async function handleRead(row: SystemNotifyMessageApi.NotifyMessage) {
  const hideLoading = message.loading({
    content: '正在标记已读...',
    duration: 0,
  });
  try {
    await updateNotifyMessageRead([row.id]);
    message.success('标记已读成功');
    handleRefresh();
    // 打开详情
    handleDetail(row);
  } finally {
    hideLoading();
  }
}

/** 标记选中的站内信为已读 */
async function handleMarkRead() {
  const rows = gridApi.grid.getCheckboxRecords();
  if (!rows || rows.length === 0) {
    message.warning('请选择需要标记的站内信');
    return;
  }

  const ids = rows.map((row: SystemNotifyMessageApi.NotifyMessage) => row.id);
  const hideLoading = message.loading({
    content: '正在标记已读...',
    duration: 0,
  });
  try {
    await updateNotifyMessageRead(ids);
    checkedIds.value = [];
    message.success('标记已读成功');
    await gridApi.grid.setAllCheckboxRow(false);
    handleRefresh();
  } finally {
    hideLoading();
  }
}

const checkedIds = ref<number[]>([]);
function handleRowCheckboxChange({
  records,
}: {
  records: SystemNotifyMessageApi.NotifyMessage[];
}) {
  checkedIds.value = records.map((item) => item.id);
}

/** 标记所有站内信为已读 */
async function handleMarkAllRead() {
  const hideLoading = message.loading({
    content: '正在标记全部已读...',
    duration: 0,
  });
  try {
    await updateAllNotifyMessageRead();
    message.success('全部标记已读成功');
    checkedIds.value = [];
    await gridApi.grid.setAllCheckboxRow(false);
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
          return await getMyNotifyMessagePage({
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
    checkboxConfig: {
      checkMethod: (params: { row: SystemNotifyMessageApi.NotifyMessage }) =>
        !params.row.readStatus,
      highlight: true,
    },
  } as VxeTableGridOptions<SystemNotifyMessageApi.NotifyMessage>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>
<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="站内信配置" url="https://doc.iocoder.cn/notify/" />
    </template>

    <DetailModal @success="handleRefresh" />
    <Grid table-title="我的站内信">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '标记已读',
              type: 'primary',
              icon: ACTION_ICON.ADD,
              disabled: isEmpty(checkedIds),
              onClick: handleMarkRead,
            },
            {
              label: '全部已读',
              type: 'primary',
              icon: ACTION_ICON.ADD,
              onClick: handleMarkAllRead,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '查看',
              type: 'link',
              ifShow: row.readStatus,
              icon: ACTION_ICON.VIEW,
              onClick: handleDetail.bind(null, row),
            },
            {
              label: '已读',
              type: 'link',
              ifShow: !row.readStatus,
              icon: ACTION_ICON.ADD,
              onClick: handleRead.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
