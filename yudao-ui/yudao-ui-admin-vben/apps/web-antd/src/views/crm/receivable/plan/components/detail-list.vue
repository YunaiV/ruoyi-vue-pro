<!-- 回款计划列表：用于【客户】【合同】详情中，展示它们关联的回款计划列表 -->
<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmReceivablePlanApi } from '#/api/crm/receivable/plan';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteReceivablePlan,
  getReceivablePlanPageByCustomer,
} from '#/api/crm/receivable/plan';
import { $t } from '#/locales';

import Form from '../modules/form.vue';
import { useDetailListColumns } from './data';

const props = defineProps<{
  contractId?: number; // 合同编号
  customerId?: number; // 客户编号
}>();

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建回款计划 */
function handleCreate() {
  formModalApi
    .setData({
      contractId: props.contractId,
      customerId: props.customerId,
    })
    .open();
}

/** 编辑回款计划 */
function handleEdit(row: CrmReceivablePlanApi.Plan) {
  formModalApi.setData({ receivablePlan: row }).open();
}

/** 删除回款计划 */
async function handleDelete(row: CrmReceivablePlanApi.Plan) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [`第${row.period}期`]),
    duration: 0,
  });
  try {
    await deleteReceivablePlan(row.id!);
    message.success(
      $t('ui.actionMessage.deleteSuccess', [`第${row.period}期`]),
    );
    handleRefresh();
  } finally {
    hideLoading();
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useDetailListColumns(),
    height: 500,
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }) => {
          const queryParams: CrmReceivablePlanApi.PlanPageParam = {
            pageNo: page.currentPage,
            pageSize: page.pageSize,
          };
          if (props.customerId && !props.contractId) {
            queryParams.customerId = props.customerId;
          } else if (props.customerId && props.contractId) {
            // 如果是合同的话客户编号也需要带上因为权限基于客户
            queryParams.customerId = props.customerId;
            queryParams.contractId = props.contractId;
          }
          return await getReceivablePlanPageByCustomer(queryParams);
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
  } as VxeTableGridOptions<CrmReceivablePlanApi.Plan>,
});
</script>

<template>
  <div>
    <FormModal @success="handleRefresh" />
    <Grid>
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['回款计划']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['crm:receivable-plan:create'],
              onClick: handleCreate,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['crm:receivable-plan:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['crm:receivable-plan:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [
                  `第${row.period}期`,
                ]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </div>
</template>
