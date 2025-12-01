<script setup lang="ts">
import type { CrmBusinessApi } from '#/api/crm/business';
import type { SystemOperateLogApi } from '#/api/system/operate-log';

import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page, useVbenModal } from '@vben/common-ui';
import { useTabs } from '@vben/hooks';

import { ElCard, ElTabPane, ElTabs } from 'element-plus';

import { getBusiness } from '#/api/crm/business';
import { getOperateLogPage } from '#/api/crm/operateLog';
import { BizTypeEnum } from '#/api/crm/permission';
import { useDescription } from '#/components/description';
import { OperateLog } from '#/components/operate-log';
import { ACTION_ICON, TableAction } from '#/components/table-action';
import { $t } from '#/locales';
import { ContactDetailsList } from '#/views/crm/contact/components';
import { ContractDetailsList } from '#/views/crm/contract/components';
import { FollowUp } from '#/views/crm/followup';
import { PermissionList, TransferForm } from '#/views/crm/permission';
import { ProductDetailsList } from '#/views/crm/product/components';

import Form from '../modules/form.vue';
import { useDetailSchema } from './data';
import BusinessDetailsInfo from './modules/info.vue';
import UpStatusForm from './modules/status-form.vue';

const route = useRoute();
const router = useRouter();
const tabs = useTabs();

const loading = ref(false); // 加载中
const businessId = ref(0); // 商机编号
const business = ref<CrmBusinessApi.Business>({} as CrmBusinessApi.Business); // 商机详情
const logList = ref<SystemOperateLogApi.OperateLog[]>([]);
const permissionListRef = ref<InstanceType<typeof PermissionList>>(); // 团队成员列表 Ref
const activeTabName = ref('1'); // 选中 Tab 名

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

const [UpStatusModal, upStatusModalApi] = useVbenModal({
  connectedComponent: UpStatusForm,
  destroyOnClose: true,
});

/** 加载详情 */
async function getBusinessDetail() {
  loading.value = true;
  try {
    business.value = await getBusiness(businessId.value);
    // 操作日志
    const res = await getOperateLogPage({
      bizType: BizTypeEnum.CRM_BUSINESS,
      bizId: businessId.value,
    });
    logList.value = res.list;
  } finally {
    loading.value = false;
  }
}

/** 返回列表页 */
function handleBack() {
  tabs.closeCurrentTab();
  router.push({ name: 'CrmBusiness' });
}

/** 编辑商机 */
function handleEdit() {
  formModalApi.setData({ id: businessId.value }).open();
}

/** 转移商机 */
function handleTransfer() {
  transferModalApi.setData({ bizType: BizTypeEnum.CRM_BUSINESS }).open();
}

/** 更新商机状态操作 */
async function handleUpdateStatus() {
  upStatusModalApi.setData(business.value).open();
}

/** 加载数据 */
onMounted(() => {
  businessId.value = Number(route.params.id);
  getBusinessDetail();
});
</script>

<template>
  <Page auto-content-height :title="business?.name" :loading="loading">
    <FormModal @success="getBusinessDetail" />
    <TransferModal @success="getBusinessDetail" />
    <UpStatusModal @success="getBusinessDetail" />
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
            auth: ['crm:business:update'],
            ifShow: permissionListRef?.validateWrite,
            onClick: handleEdit,
          },
          {
            label: '变更商机状态',
            type: 'primary',
            ifShow: permissionListRef?.validateWrite,
            onClick: handleUpdateStatus,
          },
          {
            label: '转移',
            type: 'primary',
            ifShow: permissionListRef?.validateOwnerUser,
            onClick: handleTransfer,
          },
        ]"
      />
    </template>
    <ElCard class="min-h-[10%]">
      <Descriptions :data="business" />
    </ElCard>
    <ElCard class="mt-4 min-h-[60%]">
      <ElTabs v-model:model-value="activeTabName">
        <ElTabPane label="跟进记录" name="1">
          <FollowUp :biz-id="businessId" :biz-type="BizTypeEnum.CRM_BUSINESS" />
        </ElTabPane>
        <ElTabPane label="详细资料" name="2">
          <BusinessDetailsInfo :business="business" />
        </ElTabPane>
        <ElTabPane label="联系人" name="3">
          <ContactDetailsList
            :biz-id="businessId"
            :biz-type="BizTypeEnum.CRM_BUSINESS"
            :business-id="businessId"
            :customer-id="business.customerId"
          />
        </ElTabPane>
        <ElTabPane label="产品" name="4">
          <ProductDetailsList
            :biz-id="businessId"
            :biz-type="BizTypeEnum.CRM_BUSINESS"
            :business="business"
          />
        </ElTabPane>
        <ElTabPane label="合同" name="5">
          <ContractDetailsList
            :biz-id="businessId"
            :biz-type="BizTypeEnum.CRM_BUSINESS"
          />
        </ElTabPane>
        <ElTabPane label="操作日志" name="6">
          <OperateLog :log-list="logList" />
        </ElTabPane>
        <ElTabPane label="团队成员" name="7">
          <PermissionList
            ref="permissionListRef"
            :biz-id="businessId"
            :biz-type="BizTypeEnum.CRM_BUSINESS"
            :show-action="true"
            @quit-team="handleBack"
          />
        </ElTabPane>
      </ElTabs>
    </ElCard>
  </Page>
</template>
