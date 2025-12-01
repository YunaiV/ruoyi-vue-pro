<!-- 商机列表：用于【客户】【联系人】详情中，展示其关联的商机列表 -->
<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmBusinessApi } from '#/api/crm/business';
import type { CrmContactApi } from '#/api/crm/contact';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { confirm, useVbenModal } from '@vben/common-ui';

import { ElButton, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  getBusinessPageByContact,
  getBusinessPageByCustomer,
} from '#/api/crm/business';
import {
  createContactBusinessList,
  deleteContactBusinessList,
} from '#/api/crm/contact';
import { BizTypeEnum } from '#/api/crm/permission';
import { $t } from '#/locales';

import Form from '../modules/form.vue';
import { useBusinessDetailListColumns } from './data';
import ListModal from './detail-list-modal.vue';

const props = defineProps<{
  bizId: number; // 业务编号
  bizType: number; // 业务类型
  contactId?: number; // 特殊：联系人编号；在【联系人】详情中，可以传递联系人编号，默认新建的商机关联到该联系人
  customerId?: number; // 关联联系人与商机时，需要传入 customerId 进行筛选
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

const checkedRows = ref<CrmBusinessApi.Business[]>([]);
function setCheckedRows({ records }: { records: CrmBusinessApi.Business[] }) {
  checkedRows.value = records;
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建商机 */
function handleCreate() {
  formModalApi
    .setData({ customerId: props.customerId, contactId: props.contactId })
    .open();
}

/** 关联商机 */
function handleCreateBusiness() {
  detailListModalApi.setData({ customerId: props.customerId }).open();
}

/** 解除商机关联 */
async function handleDeleteContactBusinessList() {
  if (checkedRows.value.length === 0) {
    ElMessage.error('请先选择商机后操作！');
    return;
  }
  return new Promise((resolve, reject) => {
    confirm({
      content: `确定要将${checkedRows.value.map((item) => item.name).join(',')}解除关联吗？`,
    })
      .then(async () => {
        const res = await deleteContactBusinessList({
          contactId: props.bizId,
          businessIds: checkedRows.value.map((item) => item.id),
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

/** 查看商机详情 */
function handleDetail(row: CrmBusinessApi.Business) {
  push({ name: 'CrmBusinessDetail', params: { id: row.id } });
}

/** 查看客户详情 */
function handleCustomerDetail(row: CrmBusinessApi.Business) {
  push({ name: 'CrmCustomerDetail', params: { id: row.customerId } });
}

/** 创建联系人关联的商机 */
async function handleCreateContactBusinessList(businessIds: number[]) {
  const data = {
    contactId: props.bizId,
    businessIds,
  } as CrmContactApi.ContactBusinessReqVO;
  await createContactBusinessList(data);
  handleRefresh();
}

/** 商机关联表格 */
const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useBusinessDetailListColumns(),
    height: 600,
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          if (props.bizType === BizTypeEnum.CRM_CUSTOMER) {
            return await getBusinessPageByCustomer({
              pageNo: page.currentPage,
              pageSize: page.pageSize,
              customerId: props.customerId,
              ...formValues,
            });
          } else if (props.bizType === BizTypeEnum.CRM_CONTACT) {
            return await getBusinessPageByContact({
              pageNo: page.currentPage,
              pageSize: page.pageSize,
              contactId: props.contactId,
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
  } as VxeTableGridOptions<CrmBusinessApi.Business>,
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
      @success="handleCreateContactBusinessList"
    />
    <Grid>
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['商机']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['crm:business:create'],
              onClick: handleCreate,
            },
            {
              label: '关联',
              icon: ACTION_ICON.ADD,
              type: 'default',
              auth: ['crm:contact:create-business'],
              ifShow: () => !!contactId,
              onClick: handleCreateBusiness,
            },
            {
              label: '解除关联',
              icon: ACTION_ICON.ADD,
              type: 'default',
              auth: ['crm:contact:create-business'],
              ifShow: () => !!contactId,
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
