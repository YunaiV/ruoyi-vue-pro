<script setup lang="ts">
import type { CrmReceivablePlanApi } from '#/api/crm/receivable/plan';
import type { SystemOperateLogApi } from '#/api/system/operate-log';

import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page, useVbenModal } from '@vben/common-ui';
import { useTabs } from '@vben/hooks';

import { Card, Tabs } from 'ant-design-vue';

import { ACTION_ICON, TableAction } from '#/adapter/vxe-table';
import { getOperateLogPage } from '#/api/crm/operateLog';
import { BizTypeEnum } from '#/api/crm/permission';
import { getReceivablePlan } from '#/api/crm/receivable/plan';
import { useDescription } from '#/components/description';
import { OperateLog } from '#/components/operate-log';
import { $t } from '#/locales';
import { PermissionList } from '#/views/crm/permission';
import { ReceivablePlanDetailsInfo } from '#/views/crm/receivable/plan/components';

import ReceivablePlanForm from '../modules/form.vue';
import { useDetailSchema } from './data';

const route = useRoute();
const router = useRouter();
const tabs = useTabs();

const loading = ref(false); // 加载中
const receivablePlanId = ref(0); // 回款计划编号
const receivablePlan = ref<CrmReceivablePlanApi.Plan>(
  {} as CrmReceivablePlanApi.Plan,
);
const logList = ref<SystemOperateLogApi.OperateLog[]>([]); // 操作日志
const permissionListRef = ref<InstanceType<typeof PermissionList>>(); // 团队成员列表 Ref
const validateWrite = () => permissionListRef.value?.validateWrite; // 校验编辑权限

const [Descriptions] = useDescription({
  bordered: false,
  column: 4,
  schema: useDetailSchema(),
});

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: ReceivablePlanForm,
  destroyOnClose: true,
});

/** 加载回款计划详情 */
async function getReceivablePlanDetail() {
  loading.value = true;
  try {
    receivablePlan.value = await getReceivablePlan(receivablePlanId.value);
    // 操作日志
    const res = await getOperateLogPage({
      bizType: BizTypeEnum.CRM_RECEIVABLE_PLAN,
      bizId: receivablePlanId.value,
    });
    logList.value = res.list;
  } finally {
    loading.value = false;
  }
}

/** 返回列表页 */
function handleBack() {
  tabs.closeCurrentTab();
  router.push({ name: 'CrmReceivablePlan' });
}

/** 编辑收款 */
function handleEdit() {
  formModalApi.setData({ id: receivablePlanId.value }).open();
}

/** 加载数据 */
onMounted(() => {
  receivablePlanId.value = Number(route.params.id);
  getReceivablePlanDetail();
});
</script>

<template>
  <Page
    auto-content-height
    :title="`第 ${receivablePlan?.period} 期`"
    :loading="loading"
  >
    <FormModal @success="getReceivablePlanDetail" />
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
            disabled: !validateWrite(),
            onClick: handleEdit,
            auth: ['crm:receivable-plan:update'],
          },
        ]"
      />
    </template>
    <Card class="min-h-[10%]">
      <Descriptions :data="receivablePlan" />
    </Card>
    <Card class="mt-4 min-h-[60%]">
      <Tabs>
        <Tabs.TabPane tab="详细资料" key="1" :force-render="true">
          <ReceivablePlanDetailsInfo :receivable-plan="receivablePlan" />
        </Tabs.TabPane>
        <Tabs.TabPane tab="操作日志" key="2" :force-render="true">
          <OperateLog :log-list="logList" />
        </Tabs.TabPane>
        <Tabs.TabPane tab="团队成员" key="3" :force-render="true">
          <PermissionList
            ref="permissionListRef"
            :biz-id="receivablePlanId"
            :biz-type="BizTypeEnum.CRM_RECEIVABLE_PLAN"
            :show-action="true"
            @quit-team="handleBack"
          />
        </Tabs.TabPane>
      </Tabs>
    </Card>
  </Page>
</template>
