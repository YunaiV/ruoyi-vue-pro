<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmFollowUpApi } from '#/api/crm/followup';

import { watch } from 'vue';
import { useRouter } from 'vue-router';

import { useVbenModal } from '@vben/common-ui';

import { Button, message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteFollowUpRecord,
  getFollowUpRecordPage,
} from '#/api/crm/followup';
import { $t } from '#/locales';

import { useGridColumns } from './data';
import FollowUpRecordForm from './modules/form.vue';

/** 跟进记录列表 */
defineOptions({ name: 'FollowUpRecord' });

const props = defineProps<{
  bizId: number;
  bizType: number;
}>();

const { push } = useRouter();

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 添加跟进记录 */
function handleCreate() {
  formModalApi.setData({ bizId: props.bizId, bizType: props.bizType }).open();
}

/** 删除跟进记录 */
async function handleDelete(row: CrmFollowUpApi.FollowUpRecord) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.id]),
    duration: 0,
  });
  try {
    await deleteFollowUpRecord(row.id);
    message.success($t('ui.actionMessage.deleteSuccess', [row.id]));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

/** 打开联系人详情 */
function openContactDetail(id: number) {
  push({ name: 'CrmContactDetail', params: { id } });
}

/** 打开商机详情 */
function openBusinessDetail(id: number) {
  push({ name: 'CrmBusinessDetail', params: { id } });
}

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: FollowUpRecordForm,
  destroyOnClose: true,
});

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useGridColumns(props.bizType),
    height: 600,
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }) => {
          return await getFollowUpRecordPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            bizType: props.bizType,
            bizId: props.bizId,
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
    },
  } as VxeTableGridOptions<CrmFollowUpApi.FollowUpRecord>,
});

/** 监听业务 ID 变化 */
watch(
  () => props.bizId,
  () => {
    gridApi.query();
  },
);
</script>

<template>
  <div>
    <FormModal @success="handleRefresh" />
    <Grid>
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '写跟进',
              type: 'primary',
              icon: ACTION_ICON.EDIT,
              onClick: handleCreate,
            },
          ]"
        />
      </template>
      <template #contacts="{ row }">
        <Button
          v-for="contact in row.contacts || []"
          :key="`contact-${contact.id}`"
          type="link"
          class="ml-2"
          @click="openContactDetail(contact.id)"
        >
          {{ contact.name }}
        </Button>
      </template>
      <template #businesses="{ row }">
        <Button
          v-for="business in row.businesses || []"
          :key="`business-${business.id}`"
          type="link"
          class="ml-2"
          @click="openBusinessDetail(business.id)"
        >
          {{ business.name }}
        </Button>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.id]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </div>
</template>
