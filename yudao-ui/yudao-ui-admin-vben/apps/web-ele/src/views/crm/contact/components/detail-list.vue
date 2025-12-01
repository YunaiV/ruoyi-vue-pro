<!-- 联系人列表：用于【联系人】【商机】详情中，展示它们关联的联系人列表 -->
<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmContactApi } from '#/api/crm/contact';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { confirm, useVbenModal } from '@vben/common-ui';

import { ElButton, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  createBusinessContactList,
  deleteBusinessContactList,
  getContactPageByBusiness,
  getContactPageByCustomer,
} from '#/api/crm/contact';
import { BizTypeEnum } from '#/api/crm/permission';
import { $t } from '#/locales';

import Form from '../modules/form.vue';
import { useDetailListColumns } from './data';
import ListModal from './detail-list-modal.vue';

const props = defineProps<{
  bizId: number; // 业务编号
  bizType: number; // 业务类型
  businessId?: number; // 特殊：商机编号；在【商机】详情中，可以传递商机编号，默认新建的联系人关联到该商机
  customerId?: number; // 特殊：客户编号；在【商机】详情中，可以传递客户编号，默认新建的联系人关联到该客户
}>();

const { push } = useRouter();

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const [DetailListModal, detailListModalApi] = useVbenModal({
  connectedComponent: ListModal,
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

/** 创建联系人 */
function handleCreate() {
  formModalApi
    .setData({ customerId: props.customerId, businessId: props.businessId })
    .open();
}

/** 关联联系人 */
function handleCreateContact() {
  detailListModalApi.setData({ customerId: props.customerId }).open();
}

/** 解除联系人关联 */
async function handleDeleteContactBusinessList() {
  if (checkedRows.value.length === 0) {
    ElMessage.error('请先选择联系人后操作！');
    return;
  }
  return new Promise((resolve, reject) => {
    confirm({
      content: `确定要将${checkedRows.value.map((item) => item.name).join(',')}解除关联吗？`,
    })
      .then(async () => {
        const res = await deleteBusinessContactList({
          businessId: props.bizId,
          contactIds: checkedRows.value.map((item) => item.id),
        });
        if (res) {
          // 提示并返回成功
          ElMessage.success($t('ui.actionMessage.operationSuccess'));
          handleRefresh();
          resolve(true);
        } else {
          reject(new Error($t('ui.actionMessage.operationFailed')));
        }
      })
      .catch(() => {
        reject(new Error('取消操作'));
      });
  });
}

/** 创建商机联系人关联 */
async function handleCreateBusinessContactList(contactIds: number[]) {
  const data = {
    businessId: props.bizId,
    contactIds,
  } as CrmContactApi.BusinessContactReqVO;
  await createBusinessContactList(data);
  handleRefresh();
}

/** 查看联系人详情 */
function handleDetail(row: CrmContactApi.Contact) {
  push({ name: 'CrmContactDetail', params: { id: row.id } });
}

/** 查看客户详情 */
function handleCustomerDetail(row: CrmContactApi.Contact) {
  push({ name: 'CrmCustomerDetail', params: { id: row.customerId } });
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
            return await getContactPageByCustomer({
              pageNo: page.currentPage,
              pageSize: page.pageSize,
              customerId: props.bizId,
              ...formValues,
            });
          } else if (props.bizType === BizTypeEnum.CRM_BUSINESS) {
            return await getContactPageByBusiness({
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
  } as VxeTableGridOptions<CrmContactApi.Contact>,
  gridEvents: {
    checkboxAll: setCheckedRows,
    checkboxChange: setCheckedRows,
  },
});
</script>

<template>
  <div>
    <FormModal @success="handleRefresh" />
    <DetailListModal
      :customer-id="customerId"
      @success="handleCreateBusinessContactList"
    />
    <Grid>
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['联系人']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              onClick: handleCreate,
            },
            {
              label: '关联',
              icon: ACTION_ICON.ADD,
              type: 'default',
              auth: ['crm:contact:create-business'],
              ifShow: () => !!businessId,
              onClick: handleCreateContact,
            },
            {
              label: '解除关联',
              icon: ACTION_ICON.ADD,
              type: 'default',
              auth: ['crm:contact:create-business'],
              ifShow: () => !!businessId,
              onClick: handleDeleteContactBusinessList,
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
  </div>
</template>
