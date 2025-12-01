<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallCommentApi } from '#/api/mall/product/comment';

import { h } from 'vue';

import { confirm, DocAlert, Page, prompt, useVbenModal } from '@vben/common-ui';

import { message, Rate, Textarea } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  getCommentPage,
  replyComment,
  updateCommentVisible,
} from '#/api/mall/product/comment';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建评价 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 回复评价 */
function handleReply(row: MallCommentApi.Comment) {
  prompt({
    component: () => {
      return h(Textarea, {
        placeholder: '请输入回复内容',
      });
    },
    content: row.content
      ? `用户评论：${row.content}\n请输入回复内容：`
      : '请输入回复内容：',
    title: '回复评论',
    modelPropName: 'value',
  }).then(async (val) => {
    if (val) {
      await replyComment({
        id: row.id!,
        replyContent: val,
      });
      handleRefresh();
    }
  });
}

/** 更新状态 */
async function handleStatusChange(
  newStatus: boolean,
  row: MallCommentApi.Comment,
): Promise<boolean | undefined> {
  return new Promise((resolve, reject) => {
    const text = newStatus ? '展示' : '隐藏';
    confirm({
      content: `确认要${text}该评论吗？`,
    })
      .then(async () => {
        // 更新状态
        await updateCommentVisible({
          id: row.id!,
          visible: newStatus,
        });
        // 提示并返回成功
        message.success(`${text}成功`);
        resolve(true);
      })
      .catch(() => {
        reject(new Error('取消操作'));
      });
  });
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(handleStatusChange),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getCommentPage({
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
  } as VxeTableGridOptions<MallCommentApi.Comment>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【商品】商品评价"
        url="https://doc.iocoder.cn/mall/product-comment/"
      />
    </template>
    <FormModal @success="handleRefresh" />
    <Grid table-title="评论列表">
      <template #descriptionScores="{ row }">
        <Rate v-model:value="row.descriptionScores" :disabled="true" />
      </template>
      <template #benefitScores="{ row }">
        <Rate v-model:value="row.benefitScores" :disabled="true" />
      </template>
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['虚拟评论']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['product:comment:create'],
              onClick: handleCreate,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '回复',
              type: 'link',
              auth: ['product:comment:update'],
              onClick: handleReply.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
