<!-- 合同列表：用于【客户】【商机】【联系人】详情中，展示它们关联的合同列表 -->
<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmContractApi } from '#/api/crm/contract';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { useVbenModal } from '@vben/common-ui';

import { ElButton } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  getContractPageByBusiness,
  getContractPageByCustomer,
} from '#/api/crm/contract';
import { BizTypeEnum } from '#/api/crm/permission';
import { $t } from '#/locales';

import Form from '../modules/form.vue';
import { useDetailListColumns } from './data';

const props = defineProps<{
  bizId: number; // 业务编号
  bizType: number; // 业务类型
}>();

const { push } = useRouter();

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 已选择的合同 */
const checkedRows = ref<CrmContractApi.Contract[]>();
function setCheckedRows({ records }: { records: CrmContractApi.Contract[] }) {
  checkedRows.value = records;
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建合同 */
function handleCreate() {
  formModalApi
    .setData(
      props.bizType === BizTypeEnum.CRM_CUSTOMER
        ? {
            customerId: props.bizId,
          }
        : { businessId: props.bizId },
    )
    .open();
}

/** 查看合同详情 */
function handleDetail(row: CrmContractApi.Contract) {
  push({ name: 'CrmContractDetail', params: { id: row.id } });
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useDetailListColumns(),
    height: 600,
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          if (props.bizType === BizTypeEnum.CRM_CUSTOMER) {
            return await getContractPageByCustomer({
              pageNo: page.currentPage,
              pageSize: page.pageSize,
              customerId: props.bizId,
              ...formValues,
            });
          } else if (props.bizType === BizTypeEnum.CRM_CONTACT) {
            return await getContractPageByBusiness({
              pageNo: page.currentPage,
              pageSize: page.pageSize,
              businessId: props.bizId,
              ...formValues,
            });
          } else {
            return [];
          }
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
  } as VxeTableGridOptions<CrmContractApi.Contract>,
  gridEvents: {
    checkboxAll: setCheckedRows,
    checkboxChange: setCheckedRows,
  },
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
              label: $t('ui.actionTitle.create', ['合同']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['crm:contract:create'],
              onClick: handleCreate,
            },
          ]"
        />
      </template>
      <template #name="{ row }">
        <ElButton type="primary" link @click="handleDetail(row)">
          {{ row.name }}
        </ElButton>
      </template>
    </Grid>
  </div>
</template>
