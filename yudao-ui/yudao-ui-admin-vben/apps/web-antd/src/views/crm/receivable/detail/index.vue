<script setup lang="ts">
import type { CrmReceivableApi } from '#/api/crm/receivable';
import type { SystemOperateLogApi } from '#/api/system/operate-log';

import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page, useVbenModal } from '@vben/common-ui';
import { useTabs } from '@vben/hooks';

import { Card, Tabs } from 'ant-design-vue';

import { getOperateLogPage } from '#/api/crm/operateLog';
import { BizTypeEnum } from '#/api/crm/permission';
import { getReceivable } from '#/api/crm/receivable';
import { useDescription } from '#/components/description';
import { OperateLog } from '#/components/operate-log';
import { ACTION_ICON, TableAction } from '#/components/table-action';
import { $t } from '#/locales';
import { PermissionList } from '#/views/crm/permission';

import ReceivableForm from '../modules/form.vue';
import { useDetailSchema } from './data';
import Info from './modules/info.vue';

const props = defineProps<{ id?: number }>();

const route = useRoute();
const router = useRouter();
const tabs = useTabs();

const loading = ref(false); // 加载中
const receivableId = ref(0); // 回款编号
const receivable = ref<CrmReceivableApi.Receivable>(
  {} as CrmReceivableApi.Receivable,
); // 回款详情
const logList = ref<SystemOperateLogApi.OperateLog[]>([]); // 操作日志
const permissionListRef = ref<InstanceType<typeof PermissionList>>(); // 团队成员列表 Ref

const [Descriptions] = useDescription({
  bordered: false,
  column: 4,
  schema: useDetailSchema(),
});

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: ReceivableForm,
  destroyOnClose: true,
});

/** 加载回款详情 */
async function loadReceivableDetail() {
  loading.value = true;
  try {
    receivable.value = await getReceivable(receivableId.value);
    // 操作日志
    const res = await getOperateLogPage({
      bizType: BizTypeEnum.CRM_RECEIVABLE,
      bizId: receivableId.value,
    });
    logList.value = res.list;
  } finally {
    loading.value = false;
  }
}

/** 返回列表页 */
function handleBack() {
  tabs.closeCurrentTab();
  router.push({ name: 'CrmReceivable' });
}

/** 编辑收款 */
function handleEdit() {
  formModalApi.setData({ receivable: { id: receivableId.value } }).open();
}

/** 加载数据 */
onMounted(() => {
  receivableId.value = Number(props.id || route.params.id);
  loadReceivableDetail();
});
</script>

<template>
  <Page auto-content-height :title="receivable?.no" :loading="loading">
    <FormModal @success="loadReceivableDetail" />
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
            auth: ['crm:receivable:update'],
            ifShow: permissionListRef?.validateWrite,
            onClick: handleEdit,
          },
        ]"
      />
    </template>
    <Card class="min-h-[10%]">
      <Descriptions :data="receivable" />
    </Card>
    <Card class="mt-4 min-h-[60%]">
      <Tabs>
        <Tabs.TabPane tab="详细资料" key="1" :force-render="true">
          <Info :receivable="receivable" />
        </Tabs.TabPane>
        <Tabs.TabPane tab="操作日志" key="2" :force-render="true">
          <OperateLog :log-list="logList" />
        </Tabs.TabPane>
        <Tabs.TabPane tab="团队成员" key="3" :force-render="true">
          <PermissionList
            ref="permissionListRef"
            :biz-id="receivableId"
            :biz-type="BizTypeEnum.CRM_RECEIVABLE"
            :show-action="true"
            @quit-team="handleBack"
          />
        </Tabs.TabPane>
      </Tabs>
    </Card>
  </Page>
</template>
