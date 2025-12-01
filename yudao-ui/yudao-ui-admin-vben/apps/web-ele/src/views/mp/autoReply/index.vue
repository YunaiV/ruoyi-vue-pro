<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MpAutoReplyApi } from '#/api/mp/autoReply';

import { computed, nextTick, ref } from 'vue';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { AutoReplyMsgType } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';

import { ElLoading, ElMessage, ElRow, ElTabPane, ElTabs } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteAutoReply,
  getAutoReply,
  getAutoReplyPage,
} from '#/api/mp/autoReply';
import { $t } from '#/locales';
import { WxAccountSelect } from '#/views/mp/components';

import { useGridColumns, useGridFormSchema } from './data';
import ReplyContent from './modules/content.vue';
import Form from './modules/form.vue';

defineOptions({ name: 'MpAutoReply' });

const msgType = ref<string>(String(AutoReplyMsgType.Keyword)); // 消息类型

const showCreateButton = computed(() => {
  if (Number(msgType.value) !== AutoReplyMsgType.Follow) {
    return true;
  }
  try {
    const tableData = gridApi.grid?.getTableData();
    return (tableData?.tableData?.length || 0) <= 0;
  } catch {
    return true;
  }
}); // 计算是否显示新增按钮：关注时回复类型只有在没有数据时才显示

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 公众号变化时查询数据 */
function handleAccountChange(accountId: number) {
  gridApi.formApi.setValues({ accountId });
  gridApi.formApi.submitForm();
}

/** 切换回复类型 */
async function onTabChange(tabName: string) {
  msgType.value = tabName;
  await nextTick();
  // 更新 columns
  const columns = useGridColumns(Number(msgType.value) as AutoReplyMsgType);
  if (columns) {
    // 使用 setGridOptions 更新列配置
    gridApi.setGridOptions({ columns });
    // 等待列配置更新完成
    await nextTick();
  }
  // 查询数据
  await gridApi.query();
}

/** 新增自动回复 */
async function handleCreate() {
  const formValues = await gridApi.formApi.getValues();
  formModalApi
    .setData({
      msgType: Number(msgType.value) as AutoReplyMsgType,
      accountId: formValues.accountId,
    })
    .open();
}

/** 修改自动回复 */
async function handleEdit(row: MpAutoReplyApi.AutoReply) {
  const data = await getAutoReply(row.id!);
  formModalApi
    .setData({
      msgType: Number(msgType.value) as AutoReplyMsgType,
      accountId: row.accountId,
      row: data,
    })
    .open();
}

/** 删除自动回复 */
async function handleDelete(row: MpAutoReplyApi.AutoReply) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', ['自动回复']),
  });
  try {
    await deleteAutoReply(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(Number(msgType.value) as AutoReplyMsgType),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getAutoReplyPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            type: Number(msgType.value) as AutoReplyMsgType,
            ...formValues,
          });
        },
      },
      autoLoad: false,
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MpAutoReplyApi.AutoReply>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="自动回复" url="https://doc.iocoder.cn/mp/auto-reply/" />
    </template>

    <FormModal @success="handleRefresh" />
    <Grid>
      <template #form-accountId>
        <WxAccountSelect @change="handleAccountChange" />
      </template>
      <template #toolbar-actions>
        <ElTabs
          v-model="msgType"
          class="w-full"
          @tab-change="(activeName) => onTabChange(activeName as string)"
        >
          <ElTabPane :name="String(AutoReplyMsgType.Follow)">
            <template #label>
              <ElRow align="middle">
                <IconifyIcon icon="lucide:star" class="mr-[2px]" /> 关注时回复
              </ElRow>
            </template>
          </ElTabPane>
          <ElTabPane :name="String(AutoReplyMsgType.Message)">
            <template #label>
              <ElRow align="middle">
                <IconifyIcon
                  icon="lucide:message-circle-more"
                  class="mr-[2px]"
                />
                消息回复
              </ElRow>
            </template>
          </ElTabPane>
          <ElTabPane :name="String(AutoReplyMsgType.Keyword)">
            <template #label>
              <ElRow align="middle">
                <IconifyIcon icon="lucide:newspaper" class="mr-[2px]" />
                关键词回复
              </ElRow>
            </template>
          </ElTabPane>
        </ElTabs>
      </template>
      <template #toolbar-tools>
        <TableAction
          v-if="showCreateButton"
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['自动回复']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['mp:auto-reply:create'],
              onClick: handleCreate,
            },
          ]"
        />
      </template>
      <template #replyContent="{ row }">
        <ReplyContent :row="row" />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['mp:auto-reply:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['mp:auto-reply:delete'],
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
