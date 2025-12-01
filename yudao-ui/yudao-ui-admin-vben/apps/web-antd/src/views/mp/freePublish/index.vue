<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MpFreePublishApi } from '#/api/mp/freePublish';

import { DocAlert, Page } from '@vben/common-ui';

import { Image, message, Typography } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteFreePublish, getFreePublishPage } from '#/api/mp/freePublish';
import { $t } from '#/locales';
import { WxAccountSelect } from '#/views/mp/components';

import { useGridColumns, useGridFormSchema } from './data';

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 公众号变化时查询数据 */
function handleAccountChange(accountId: number) {
  gridApi.formApi.setValues({ accountId });
  gridApi.formApi.submitForm();
}

/** 删除文章 */
async function handleDelete(row: MpFreePublishApi.FreePublish) {
  const formValues = await gridApi.formApi.getValues();
  const accountId = formValues.accountId;
  if (!accountId) {
    message.warning('请先选择公众号');
    return;
  }
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting'),
    duration: 0,
  });
  try {
    await deleteFreePublish(accountId, row.articleId!);
    message.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
    submitOnChange: true,
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          if (!formValues.accountId) {
            return {
              list: [],
              total: 0,
            };
          }
          const res = await getFreePublishPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
          // 将 thumbUrl 转成 picUrl，保证 wx-news 组件可以预览封面
          res.list.forEach((record: any) => {
            const newsList = record.content?.newsItem;
            if (newsList) {
              newsList.forEach((item: any) => {
                item.picUrl = item.thumbUrl || item.picUrl;
              });
            }
          });
          return {
            list: res.list as unknown as MpFreePublishApi.FreePublish[],
            total: res.total,
          };
        },
      },
    },
    rowConfig: {
      keyField: 'articleId',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<FreePublish.FreePublish>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="公众号图文" url="https://doc.iocoder.cn/mp/article/" />
    </template>
    <Grid table-title="发表记录">
      <template #form-accountId>
        <WxAccountSelect @change="handleAccountChange" />
      </template>
      <template #cover="{ row }">
        <div
          v-if="row.content?.newsItem && row.content.newsItem.length > 0"
          class="flex flex-col items-center justify-center gap-1"
        >
          <Image
            v-for="(item, index) in row.content.newsItem"
            :key="index"
            :src="item.picUrl || item.thumbUrl"
            class="h-36 !w-[300px] rounded object-cover"
            :alt="`文章 ${index + 1} 封面图`"
          />
        </div>
        <span v-else class="text-gray-400">-</span>
      </template>
      <template #title="{ row }">
        <div
          v-if="row.content?.newsItem && row.content.newsItem.length > 0"
          class="space-y-1"
        >
          <div
            v-for="(item, index) in row.content.newsItem"
            :key="index"
            class="flex h-36 items-center justify-center"
          >
            <Typography.Link :href="(item as any).url" target="_blank">
              {{ item.title }}
            </Typography.Link>
          </div>
        </div>
        <span v-else class="text-gray-400">-</span>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '删除',
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['mp:free-publish:delete'],
              popConfirm: {
                title: '是否确认删除此数据?',
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>

<style lang="scss" scoped>
:deep(.vxe-table--body-wrapper) {
  .vxe-table--body {
    .vxe-body--column {
      .vxe-cell {
        height: auto !important;
        padding: 0;
      }
    }
  }
}
</style>
