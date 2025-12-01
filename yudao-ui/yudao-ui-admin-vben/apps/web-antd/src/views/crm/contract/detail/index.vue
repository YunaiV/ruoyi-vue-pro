<script setup lang="ts">
import type { CrmContractApi } from '#/api/crm/contract';
import type { SystemOperateLogApi } from '#/api/system/operate-log';

import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page, useVbenModal } from '@vben/common-ui';
import { useTabs } from '@vben/hooks';

import { Card, Tabs } from 'ant-design-vue';

import { getContract } from '#/api/crm/contract';
import { getOperateLogPage } from '#/api/crm/operateLog';
import { BizTypeEnum } from '#/api/crm/permission';
import { useDescription } from '#/components/description';
import { OperateLog } from '#/components/operate-log';
import { ACTION_ICON, TableAction } from '#/components/table-action';
import { $t } from '#/locales';
import { FollowUp } from '#/views/crm/followup';
import { PermissionList, TransferForm } from '#/views/crm/permission';
import { ProductDetailsList } from '#/views/crm/product/components';
import { ReceivableDetailsList } from '#/views/crm/receivable/components';
import { ReceivablePlanDetailsList } from '#/views/crm/receivable/plan/components';

import Form from '../modules/form.vue';
import { useDetailSchema } from './data';
import ContractDetailsInfo from './modules/info.vue';

const props = defineProps<{ id?: number }>();

const route = useRoute();
const router = useRouter();
const tabs = useTabs();

const loading = ref(false); // 加载中
const contractId = ref(0); // 合同编号
const contract = ref<CrmContractApi.Contract>({} as CrmContractApi.Contract); // 合同详情
const logList = ref<SystemOperateLogApi.OperateLog[]>([]); // 操作日志
const permissionListRef = ref<InstanceType<typeof PermissionList>>(); // 团队成员列表 Ref

const [Descriptions] = useDescription({
  bordered: false,
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

/** 加载合同详情 */
async function loadContractDetail() {
  loading.value = true;
  try {
    contract.value = await getContract(contractId.value);
    // 操作日志
    const res = await getOperateLogPage({
      bizType: BizTypeEnum.CRM_CONTRACT,
      bizId: contractId.value,
    });
    logList.value = res.list;
  } finally {
    loading.value = false;
  }
}

/** 返回列表页 */
function handleBack() {
  tabs.closeCurrentTab();
  router.push({ name: 'CrmContract' });
}

/** 编辑合同 */
function handleEdit() {
  formModalApi.setData({ id: contractId.value }).open();
}

/** 转移合同 */
function handleTransfer() {
  transferModalApi.setData({ bizType: BizTypeEnum.CRM_CONTRACT }).open();
}

/** 加载数据 */
onMounted(() => {
  contractId.value = Number(props.id || route.params.id);
  loadContractDetail();
});
</script>

<template>
  <Page auto-content-height :title="contract?.name" :loading="loading">
    <FormModal @success="loadContractDetail" />
    <TransferModal @success="loadContractDetail" />
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
            auth: ['crm:contract:update'],
            ifShow: permissionListRef?.validateWrite,
            onClick: handleEdit,
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
    <Card class="min-h-[10%]">
      <Descriptions :data="contract" />
    </Card>
    <Card class="mt-4 min-h-[60%]">
      <Tabs>
        <Tabs.TabPane tab="跟进记录" key="1" :force-render="true">
          <FollowUp :biz-id="contractId" :biz-type="BizTypeEnum.CRM_CONTRACT" />
        </Tabs.TabPane>
        <Tabs.TabPane tab="基本信息" key="2" :force-render="true">
          <ContractDetailsInfo :contract="contract" />
        </Tabs.TabPane>
        <Tabs.TabPane tab="产品" key="3" :force-render="true">
          <ProductDetailsList
            :biz-id="contractId"
            :biz-type="BizTypeEnum.CRM_CONTRACT"
          />
        </Tabs.TabPane>
        <Tabs.TabPane
          tab="回款"
          key="4"
          :force-render="true"
          v-if="contract.customerId"
        >
          <ReceivablePlanDetailsList
            :contract-id="contractId"
            :customer-id="contract.customerId"
          />
          <ReceivableDetailsList
            :contract-id="contractId"
            :customer-id="contract.customerId"
          />
        </Tabs.TabPane>
        <Tabs.TabPane tab="团队成员" key="5" :force-render="true">
          <PermissionList
            ref="permissionListRef"
            :biz-id="contractId"
            :biz-type="BizTypeEnum.CRM_CONTRACT"
            :show-action="true"
            @quit-team="handleBack"
          />
        </Tabs.TabPane>
        <Tabs.TabPane tab="操作日志" key="6" :force-render="true">
          <OperateLog :log-list="logList" />
        </Tabs.TabPane>
      </Tabs>
    </Card>
  </Page>
</template>
