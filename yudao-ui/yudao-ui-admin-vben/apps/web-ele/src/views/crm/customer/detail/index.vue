<script setup lang="ts">
import type { CrmCustomerApi } from '#/api/crm/customer';
import type { SystemOperateLogApi } from '#/api/system/operate-log';

import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { confirm, Page, useVbenModal } from '@vben/common-ui';
import { useTabs } from '@vben/hooks';

import { ElCard, ElMessage, ElTabPane, ElTabs } from 'element-plus';

import {
  getCustomer,
  lockCustomer,
  putCustomerPool,
  receiveCustomer,
  updateCustomerDealStatus,
} from '#/api/crm/customer';
import { getOperateLogPage } from '#/api/crm/operateLog';
import { BizTypeEnum } from '#/api/crm/permission';
import { useDescription } from '#/components/description';
import { OperateLog } from '#/components/operate-log';
import { ACTION_ICON, TableAction } from '#/components/table-action';
import { $t } from '#/locales';
import { BusinessDetailsList } from '#/views/crm/business/components';
import { ContactDetailsList } from '#/views/crm/contact/components';
import { ContractDetailsList } from '#/views/crm/contract/components';
import { FollowUp } from '#/views/crm/followup';
import { PermissionList, TransferForm } from '#/views/crm/permission';
import { ReceivableDetailsList } from '#/views/crm/receivable/components';
import { ReceivablePlanDetailsList } from '#/views/crm/receivable/plan/components';

import Form from '../modules/form.vue';
import { useDetailSchema } from './data';
import DistributeForm from './modules/distribute-form.vue';
import Info from './modules/info.vue';

const route = useRoute();
const router = useRouter();
const tabs = useTabs();

const loading = ref(false); // 加载中
const customerId = ref(0); // 客户编号
const customer = ref<CrmCustomerApi.Customer>({} as CrmCustomerApi.Customer); // 客户详情
const activeTabName = ref('1'); // 选中 Tab 名
const logList = ref<SystemOperateLogApi.OperateLog[]>([]); // 操作日志
const permissionListRef = ref<InstanceType<typeof PermissionList>>(); // 团队成员列表 Ref

const [Descriptions] = useDescription({
  border: false,
  column: 4,
  schema: useDetailSchema(),
});

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const [TransferModal, transferModalApi] = useVbenModal({
  connectedComponent: TransferForm,
  destroyOnClose: true,
});

const [DistributeModal, distributeModalApi] = useVbenModal({
  connectedComponent: DistributeForm,
  destroyOnClose: true,
});

/** 加载客户详情 */
async function loadCustomerDetail() {
  loading.value = true;
  try {
    customer.value = await getCustomer(customerId.value);
    // 操作日志
    const res = await getOperateLogPage({
      bizType: BizTypeEnum.CRM_CUSTOMER,
      bizId: customerId.value,
    });
    logList.value = res.list;
  } finally {
    loading.value = false;
  }
}

/** 返回列表页 */
function handleBack() {
  tabs.closeCurrentTab();
  router.push({ name: 'CrmCustomer' });
}

/** 编辑客户 */
function handleEdit() {
  formModalApi.setData({ id: customerId.value }).open();
}

/** 转移线索 */
function handleTransfer() {
  transferModalApi.setData({ id: customerId.value }).open();
}

/** 锁定客户 */
function handleLock(lockStatus: boolean): Promise<boolean | undefined> {
  return new Promise((resolve, reject) => {
    confirm({
      content: `确定锁定客户【${customer.value.name}】吗？`,
    })
      .then(async () => {
        // 锁定客户
        await lockCustomer(customerId.value, lockStatus);
        // 提示并返回成功
        ElMessage.success(lockStatus ? '锁定客户成功' : '解锁客户成功');
        resolve(true);
      })
      .catch(() => {
        reject(new Error('取消操作'));
      });
  });
}

/** 领取客户 */
function handleReceive(): Promise<boolean | undefined> {
  return new Promise((resolve, reject) => {
    confirm({
      content: `确定领取客户【${customer.value.name}】吗？`,
    })
      .then(async () => {
        // 领取客户
        await receiveCustomer([customerId.value]);
        // 提示并返回成功
        ElMessage.success('领取客户成功');
        resolve(true);
      })
      .catch(() => {
        reject(new Error('取消操作'));
      });
  });
}

/** 分配客户 */
function handleDistributeForm() {
  distributeModalApi.setData({ id: customerId.value }).open();
}

/** 客户放入公海 */
function handlePutPool(): Promise<boolean | undefined> {
  return new Promise((resolve, reject) => {
    confirm({
      content: `确定将客户【${customer.value.name}】放入公海吗？`,
    })
      .then(async () => {
        // 放入公海
        await putCustomerPool(customerId.value);
        // 提示并返回成功
        ElMessage.success('放入公海成功');
        resolve(true);
      })
      .catch(() => {
        reject(new Error('取消操作'));
      });
  });
}

/** 更新成交状态操作 */
async function handleUpdateDealStatus(): Promise<boolean | undefined> {
  return new Promise((resolve, reject) => {
    const dealStatus = !customer.value.dealStatus;
    confirm({
      content: `确定更新成交状态为【${dealStatus ? '已成交' : '未成交'}】吗？`,
    })
      .then(async () => {
        // 更新成交状态
        await updateCustomerDealStatus(customerId.value, dealStatus);
        // 提示并返回成功
        ElMessage.success('更新成交状态成功');
        resolve(true);
      })
      .catch(() => {
        reject(new Error('取消操作'));
      });
  });
}

/** 加载数据 */
onMounted(() => {
  customerId.value = Number(route.params.id);
  loadCustomerDetail();
});
</script>

<template>
  <Page auto-content-height :title="customer?.name" :loading="loading">
    <FormModal @success="loadCustomerDetail" />
    <TransferModal @success="loadCustomerDetail" />
    <DistributeModal @success="loadCustomerDetail" />
    <template #extra>
      <TableAction
        :actions="[
          {
            label: '返回',
            type: 'default',
            icon: 'lucide:arrow-left',
            onClick: handleBack,
          },
          {
            label: $t('ui.actionTitle.edit'),
            type: 'primary',
            icon: ACTION_ICON.EDIT,
            auth: ['crm:customer:update'],
            ifShow: permissionListRef?.validateWrite,
            onClick: handleEdit,
          },
          {
            label: '转移',
            type: 'primary',
            ifShow: permissionListRef?.validateOwnerUser,
            onClick: handleTransfer,
          },
          {
            label: '更改成交状态',
            type: 'default',
            ifShow: permissionListRef?.validateWrite,
            onClick: handleUpdateDealStatus,
          },
          {
            label: '锁定',
            type: 'default',
            ifShow:
              !customer.lockStatus && permissionListRef?.validateOwnerUser,
            onClick: handleLock.bind(null, true),
          },
          {
            label: '解锁',
            type: 'default',
            ifShow: customer.lockStatus && permissionListRef?.validateOwnerUser,
            onClick: handleLock.bind(null, false),
          },
          {
            label: '领取',
            type: 'primary',
            ifShow: !customer.ownerUserId,
            onClick: handleReceive,
          },
          {
            label: '分配',
            type: 'default',
            ifShow: !customer.ownerUserId,
            onClick: handleDistributeForm,
          },
          {
            label: '放入公海',
            type: 'default',
            ifShow:
              !!customer.ownerUserId && permissionListRef?.validateOwnerUser,
            onClick: handlePutPool,
          },
        ]"
      />
    </template>
    <ElCard class="min-h-[10%]">
      <Descriptions :data="customer" />
    </ElCard>
    <ElCard class="mt-4 min-h-[60%]">
      <ElTabs v-model:model-value="activeTabName">
        <ElTabPane label="跟进记录" name="1">
          <FollowUp :biz-id="customerId" :biz-type="BizTypeEnum.CRM_CUSTOMER" />
        </ElTabPane>
        <ElTabPane label="基本信息" name="2">
          <Info :customer="customer" />
        </ElTabPane>
        <ElTabPane label="联系人" name="3">
          <ContactDetailsList
            :biz-id="customerId"
            :biz-type="BizTypeEnum.CRM_CUSTOMER"
            :customer-id="customerId"
          />
        </ElTabPane>
        <ElTabPane label="团队成员" name="4">
          <PermissionList
            ref="permissionListRef"
            :biz-id="customerId"
            :biz-type="BizTypeEnum.CRM_CUSTOMER"
            :show-action="true"
            @quit-team="handleBack"
          />
        </ElTabPane>
        <ElTabPane label="商机" name="5">
          <BusinessDetailsList
            :biz-id="customerId"
            :biz-type="BizTypeEnum.CRM_CUSTOMER"
            :customer-id="customerId"
          />
        </ElTabPane>
        <ElTabPane label="合同" name="6">
          <ContractDetailsList
            :biz-id="customerId"
            :biz-type="BizTypeEnum.CRM_CUSTOMER"
          />
        </ElTabPane>
        <ElTabPane label="回款" name="7">
          <ReceivablePlanDetailsList :customer-id="customerId" />
          <ReceivableDetailsList :customer-id="customerId" />
        </ElTabPane>
        <ElTabPane label="操作日志" name="8">
          <OperateLog :log-list="logList" />
        </ElTabPane>
      </ElTabs>
    </ElCard>
  </Page>
</template>
