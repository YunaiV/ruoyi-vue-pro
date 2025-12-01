<!-- 联系人列表的选择：用于【商机】详情中，选择它要关联的联系人 -->
<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmContactApi } from '#/api/crm/contact';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { useVbenModal } from '@vben/common-ui';

import { ElButton, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getContactPageByCustomer } from '#/api/crm/contact';
import { $t } from '#/locales';

import Form from '../modules/form.vue';
import { useDetailListColumns } from './data';

const props = defineProps<{
  customerId?: number; // 关联联系人与商机时，需要传入 customerId 进行筛选
}>();

const emit = defineEmits(['success']);

const { push } = useRouter();

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const checkedRows = ref<CrmContactApi.Contact[]>([]);
function setCheckedRows({ records }: { records: CrmContactApi.Contact[] }) {
  checkedRows.value = records;
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建商机 */
function handleCreate() {
  formModalApi.setData({ customerId: props.customerId }).open();
}

/** 查看商机详情 */
function handleDetail(row: CrmContactApi.Contact) {
  push({ name: 'CrmContactDetail', params: { id: row.id } });
}

/** 查看客户详情 */
function handleCustomerDetail(row: CrmContactApi.Contact) {
  push({ name: 'CrmCustomerDetail', params: { id: row.customerId } });
}

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    if (checkedRows.value.length === 0) {
      ElMessage.error('请先选择联系人后操作！');
      return;
    }
    modalApi.lock();
    // 提交表单
    try {
      const contactIds = checkedRows.value.map((item) => item.id);
      // 关闭并提示
      await modalApi.close();
      emit('success', contactIds, checkedRows.value);
    } finally {
      modalApi.unlock();
    }
  },
});

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: [
      {
        fieldName: 'name',
        label: '联系人名称',
        component: 'Input',
      },
    ],
  },
  gridOptions: {
    columns: useDetailListColumns(),
    height: 600,
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getContactPageByCustomer({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            customerId: props.customerId,
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
  } as VxeTableGridOptions<CrmContactApi.Contact>,
  gridEvents: {
    checkboxAll: setCheckedRows,
    checkboxChange: setCheckedRows,
  },
});
</script>

<template>
  <Modal title="关联联系人" class="w-2/5">
    <FormModal @success="handleRefresh" />
    <Grid>
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['联系人']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['crm:contact:create'],
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
      <template #customerName="{ row }">
        <ElButton type="primary" link @click="handleCustomerDetail(row)">
          {{ row.customerName }}
        </ElButton>
      </template>
    </Grid>
  </Modal>
</template>
