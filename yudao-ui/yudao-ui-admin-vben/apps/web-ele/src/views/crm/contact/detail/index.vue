<script setup lang="ts">
import type { CrmContactApi } from '#/api/crm/contact';
import type { SystemOperateLogApi } from '#/api/system/operate-log';

import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page, useVbenModal } from '@vben/common-ui';
import { useTabs } from '@vben/hooks';

import { ElCard, ElTabPane, ElTabs } from 'element-plus';

import { getContact } from '#/api/crm/contact';
import { getOperateLogPage } from '#/api/crm/operateLog';
import { BizTypeEnum } from '#/api/crm/permission';
import { useDescription } from '#/components/description';
import { OperateLog } from '#/components/operate-log';
import { ACTION_ICON, TableAction } from '#/components/table-action';
import { $t } from '#/locales';
import { BusinessDetailsList } from '#/views/crm/business/components';
import { FollowUp } from '#/views/crm/followup';
import { PermissionList, TransferForm } from '#/views/crm/permission';

import Form from '../modules/form.vue';
import { useDetailSchema } from './data';
import Info from './modules/info.vue';

const route = useRoute();
const router = useRouter();
const tabs = useTabs();

const loading = ref(false); // 加载中
const contactId = ref(0); // 联系人编号
const contact = ref<CrmContactApi.Contact>({} as CrmContactApi.Contact); // 联系人详情
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

/** 加载联系人详情 */
async function getContactDetail() {
  loading.value = true;
  try {
    contact.value = await getContact(contactId.value);
    // 操作日志
    const res = await getOperateLogPage({
      bizType: BizTypeEnum.CRM_CONTACT,
      bizId: contactId.value,
    });
    logList.value = res.list;
  } finally {
    loading.value = false;
  }
}

/** 返回列表页 */
function handleBack() {
  tabs.closeCurrentTab();
  router.push({ name: 'CrmContact' });
}

/** 编辑联系人 */
function handleEdit() {
  formModalApi.setData({ id: contactId.value }).open();
}

/** 转移联系人 */
function handleTransfer() {
  transferModalApi.setData({ id: contactId.value }).open();
}

/** 加载数据 */
onMounted(() => {
  contactId.value = Number(route.params.id);
  getContactDetail();
});
</script>

<template>
  <Page auto-content-height :title="contact?.name" :loading="loading">
    <FormModal @success="getContactDetail" />
    <TransferModal @success="getContactDetail" />
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
            auth: ['crm:contact:update'],
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
    <ElCard class="min-h-[10%]">
      <Descriptions :data="contact" />
    </ElCard>
    <ElCard class="mt-4 min-h-[60%]">
      <ElTabs v-model:model-value="activeTabName">
        <ElTabPane label="跟进记录" name="1">
          <FollowUp :biz-id="contactId" :biz-type="BizTypeEnum.CRM_CONTACT" />
        </ElTabPane>
        <ElTabPane label="详细资料" name="2">
          <Info :contact="contact" />
        </ElTabPane>
        <ElTabPane label="操作日志" name="3">
          <OperateLog :log-list="logList" />
        </ElTabPane>
        <ElTabPane label="团队成员" name="4">
          <PermissionList
            ref="permissionListRef"
            :biz-id="contactId"
            :biz-type="BizTypeEnum.CRM_CONTACT"
            :show-action="true"
            @quit-team="handleBack"
          />
        </ElTabPane>
        <ElTabPane label="商机" name="5">
          <BusinessDetailsList
            :biz-id="contactId"
            :biz-type="BizTypeEnum.CRM_CONTACT"
            :contact-id="contactId"
            :customer-id="contact.customerId"
          />
        </ElTabPane>
      </ElTabs>
    </ElCard>
  </Page>
</template>
