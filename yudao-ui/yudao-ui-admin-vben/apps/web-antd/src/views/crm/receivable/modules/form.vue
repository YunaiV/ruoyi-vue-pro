<script lang="ts" setup>
import type { CrmReceivableApi } from '#/api/crm/receivable';

import { computed, ref } from 'vue';

import { useVbenForm, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import {
  createReceivable,
  getReceivable,
  updateReceivable,
} from '#/api/crm/receivable';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<CrmReceivableApi.Receivable>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['回款'])
    : $t('ui.actionTitle.create', ['回款']);
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
    const data = (await formApi.getValues()) as CrmReceivableApi.Receivable;
    try {
      await (formData.value?.id
        ? updateReceivable(data)
        : createReceivable(data));
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
    const data = modalApi.getData();
    if (!data) {
      return;
    }
    const { receivable, plan } = data;
    modalApi.lock();
    try {
      if (receivable) {
        formData.value = await getReceivable(receivable.id!);
      } else if (plan) {
        formData.value = plan.id
          ? {
              planId: plan.id,
              price: plan.price,
              returnType: plan.returnType,
              customerId: plan.customerId,
              contractId: plan.contractId,
            }
          : ({
              customerId: plan.customerId,
              contractId: plan.contractId,
            } as any);
      }
      // 设置到 values
      await formApi.setValues(formData.value as any);
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
