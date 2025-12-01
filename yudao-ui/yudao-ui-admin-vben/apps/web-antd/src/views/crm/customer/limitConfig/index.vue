<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmCustomerLimitConfigApi } from '#/api/crm/customer/limitConfig';

import { ref } from 'vue';

import { DocAlert, Page, useVbenModal } from '@vben/common-ui';

import { message, Tabs } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteCustomerLimitConfig,
  getCustomerLimitConfigPage,
  LimitConfType,
} from '#/api/crm/customer/limitConfig';
import { $t } from '#/locales';

import { useGridColumns } from './data';
import Form from './modules/form.vue';

const configType = ref(LimitConfType.CUSTOMER_QUANTITY_LIMIT);

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 处理配置类型的切换 */
function handleChangeConfigType(key: number | string) {
  configType.value = key as LimitConfType;
  gridApi.setGridOptions({
    columns: useGridColumns(configType.value),
  });
  handleRefresh();
}

/** 创建规则 */
function handleCreate(type: LimitConfType) {
  formModalApi.setData({ type }).open();
}

/** 编辑规则 */
function handleEdit(
  row: CrmCustomerLimitConfigApi.CustomerLimitConfig,
  type: LimitConfType,
) {
  formModalApi.setData({ id: row.id, type }).open();
}

/** 删除规则 */
async function handleDelete(
  row: CrmCustomerLimitConfigApi.CustomerLimitConfig,
) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.id]),
    duration: 0,
  });
  try {
    await deleteCustomerLimitConfig(row.id!);
    message.success($t('ui.actionMessage.deleteSuccess', [row.id]));
    handleRefresh();
  } finally {
    hideLoading();
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useGridColumns(configType.value),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getCustomerLimitConfigPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            type: configType.value,
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
  } as VxeTableGridOptions<CrmCustomerLimitConfigApi.CustomerLimitConfig>,
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="【客户】客户管理、公海客户"
        url="https://doc.iocoder.cn/crm/customer/"
      />
      <DocAlert
        title="【通用】数据权限"
        url="https://doc.iocoder.cn/crm/permission/"
      />
    </template>

    <FormModal @success="handleRefresh" />
    <Grid>
      <template #toolbar-actions>
        <Tabs class="w-full" @change="handleChangeConfigType">
          <Tabs.TabPane
            tab="拥有客户数限制"
            :key="LimitConfType.CUSTOMER_QUANTITY_LIMIT"
          />
          <Tabs.TabPane
            tab="锁定客户数限制"
            :key="LimitConfType.CUSTOMER_LOCK_LIMIT"
          />
        </Tabs>
      </template>
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['规则']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['crm:customer-limit-config:create'],
              onClick: handleCreate.bind(null, configType),
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
              auth: ['crm:customer-limit-config:update'],
              onClick: handleEdit.bind(null, row, configType),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['crm:customer-limit-config:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.id]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
