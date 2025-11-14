<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallBargainRecordApi } from '#/api/mall/promotion/bargain/bargainRecord';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getBargainRecordPage } from '#/api/mall/promotion/bargain/bargainRecord';

import { useGridColumns, useGridFormSchema } from './data';
import HelpListModal from './modules/list.vue';

defineOptions({ name: 'PromotionBargainRecord' });

const [HelpListModalApi, helpListModalApi] = useVbenModal({
  connectedComponent: HelpListModal,
  destroyOnClose: true,
});

/** 查看助力详情 */
function handleViewHelp(row: MallBargainRecordApi.BargainRecord) {
  helpListModalApi.setData({ recordId: row.id }).open();
}

const [Grid] = useVbenVxeGrid({
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
          return await getBargainRecordPage({
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
  } as VxeTableGridOptions<MallBargainRecordApi.BargainRecord>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【营销】砍价活动"
        url="https://doc.iocoder.cn/mall/promotion-bargain/"
      />
    </template>

    <HelpListModalApi />

    <Grid table-title="砍价记录列表">
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '助力',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.VIEW,
              auth: ['promotion:bargain-help:query'],
              onClick: handleViewHelp.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
