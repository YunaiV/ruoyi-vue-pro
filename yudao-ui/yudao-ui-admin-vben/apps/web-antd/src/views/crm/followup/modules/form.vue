<script lang="ts" setup>
import type { CrmFollowUpApi } from '#/api/crm/followup';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { createFollowUpRecord } from '#/api/crm/followup';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);

const bizId = ref<number>();
const bizType = ref<number>();

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 120,
  },
  layout: 'horizontal',
  schema: useFormSchema(bizId),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    // 提交表单
    const data = (await formApi.getValues()) as CrmFollowUpApi.FollowUpRecord;
    try {
      await createFollowUpRecord(data);
      // 关闭并提示
      await modalApi.close();
      emit('success');
      message.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      return;
    }
    // 加载数据
    const data = modalApi.getData<CrmFollowUpApi.FollowUpRecord>();
    if (!data) {
      return;
    }
    if (data.bizId && data.bizType) {
      bizId.value = data.bizId;
      bizType.value = data.bizType;
    }
    modalApi.lock();
    try {
      // 设置到 values
      await formApi.setValues(data);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal title="添加跟进记录" class="w-2/5">
    <Form class="mx-4" />
  </Modal>
</template>
