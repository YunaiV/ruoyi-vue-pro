<script lang="ts" setup>
import type { CrmReceivablePlanApi } from '#/api/crm/receivable/plan';

import { computed, ref } from 'vue';

import { useVbenForm, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import {
  createReceivablePlan,
  getReceivablePlan,
  updateReceivablePlan,
} from '#/api/crm/receivable/plan';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<CrmReceivablePlanApi.Plan>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['回款计划'])
    : $t('ui.actionTitle.create', ['回款计划']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  wrapperClass: 'grid-cols-2',
  layout: 'horizontal',
  schema: useFormSchema(),
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
    const data = (await formApi.getValues()) as CrmReceivablePlanApi.Plan;
    try {
      await (formData.value?.id
        ? updateReceivablePlan(data)
        : createReceivablePlan(data));
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
      formData.value = undefined;
      return;
    }
    // 加载数据
    const data = modalApi.getData<CrmReceivablePlanApi.Plan>();
    if (!data || !data.id) {
      // 设置到 values
      await formApi.setValues(data);
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getReceivablePlan(data.id);
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-2/5">
    <Form class="mx-4" />
  </Modal>
</template>
